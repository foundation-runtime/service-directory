/**
 * Copyright 2014 Cisco Systems, Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.cisco.oss.foundation.directory.lookup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.NotificationHandler;
import com.cisco.oss.foundation.directory.ServiceInstanceChangeListener;
import com.cisco.oss.foundation.directory.adapter.NotificationHandlerAdapter;
import com.cisco.oss.foundation.directory.adapter.ServiceInstanceChangeListenerAdapter;
import com.cisco.oss.foundation.directory.client.DirectoryServiceClient;
import com.cisco.oss.foundation.directory.entity.InstanceChange;
import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.impl.InstanceChangeListener;
import com.cisco.oss.foundation.directory.impl.ServiceDirectoryService;
import com.cisco.oss.foundation.directory.utils.ServiceInstanceUtils;

/**
 * It is the Directory LookupService to perform the lookup functionality.
 * <p/>
 * It looks up ServiceInstance from Directory Server.
 */
public class DirectoryLookupService extends ServiceDirectoryService {

    public static final Logger LOGGER = LoggerFactory.getLogger(DirectoryLookupService.class);

    /**
     * The DirectoryServiceClientManager to get the DirectoryServiceClient.
     */
    private final DirectoryServiceClient directoryServiceClient;

    /**
     * The Service Instance changes listeners Map.
     */
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<InstanceChangeListener<ModelServiceInstance>>>
            changeListenerMap = new ConcurrentHashMap<>();


    /**
     * If the service is stared
     */
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    @Override
    public void start() {
        super.start();
        if (isStarted.compareAndSet(false, true)) {
            initChangesCheckTask();
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (isStarted.compareAndSet(true, false)) {
            ScheduledExecutorService service = changesCheckService.getAndSet(newChangesCheckService());
            service.shutdown();
            // cleanUpChangeListenerMap();
            LOGGER.info("Service Instances Changes Checking Task is stopped");
        }
    }

    /**
     * ScheduledExecutorService to check for service instances changes
     */
    private final AtomicReference<ScheduledExecutorService> changesCheckService = new AtomicReference<>();

    private ScheduledExecutorService newChangesCheckService() {
        return Executors
                .newSingleThreadScheduledExecutor(new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setName("SD_Changes_Check_Task");
                        t.setDaemon(true);
                        return t;
                    }

                });
    }

    private void initChangesCheckTask() {
        changesCheckService.get().scheduleWithFixedDelay(new ChangesCheckTask(System.currentTimeMillis()),
                0L, 1L, TimeUnit.SECONDS);
        LOGGER.info("Service Instances Changes Checking Task is started");
    }


    private class ChangesCheckTask implements Runnable {
        // the time-mills when the task is initialized
        private final long INIT_TIME_MILLS;
        private final long CALL_TIMEOUT_SEC = 2L;

        // Although the map is not required thread-safe, since the task itself is single-threaded.
        // but is reasonable to protect it since the single thread is easy to break by changing
        // the way of object creation and service scheduling.
        private final ConcurrentHashMap<String, AtomicLong> lastChangedTimeMills = new ConcurrentHashMap<>();

        ChangesCheckTask(long initTimeMills) {
            this.INIT_TIME_MILLS = initTimeMills;
        }

        private void syncServiceListForChangedTimeMap(final List<String> nameList) {
            List<String> removeList = new ArrayList<>();
            for (String service : lastChangedTimeMills.keySet()) {
                if (!nameList.contains(service)) {
                    removeList.add(service);
                }
            }
            for (String s : removeList) {
                lastChangedTimeMills.remove(s);
            }
        }

        @Override
        public void run() {
            List<String> serviceNameList = new ArrayList<>();
            serviceNameList.addAll(changeListenerMap.keySet());

            //check if has service name need to be removed in last changed time map
            //for example, when unregister a service.
            syncServiceListForChangedTimeMap(serviceNameList);

            for (String serviceName : serviceNameList) {
                lastChangedTimeMills.putIfAbsent(serviceName, new AtomicLong(INIT_TIME_MILLS));
                try {
                    List<InstanceChange<ModelServiceInstance>> changes = getDirectoryServiceClient()
                            .lookupChangesSince(serviceName, lastChangedTimeMills.get(serviceName).longValue());
                    //has changes
                    if (!changes.isEmpty()) {
                        //oldest first
                        Collections.sort(changes, InstanceChange.Comparator);

                        for (final InstanceChange<ModelServiceInstance> c : changes) {
                            List<InstanceChangeListener<ModelServiceInstance>> listenerList = changeListenerMap.get(c.serviceName);
                            if (listenerList != null) {
                                for (final InstanceChangeListener<ModelServiceInstance> l : listenerList) {
                                    Future<?> f = Executors.newSingleThreadExecutor().submit(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        l.onChange(c.changeType, c);
                                                    } catch (Exception e) {
                                                        LOGGER.error("execute change listener in error ", e);
                                                    }
                                                }
                                            });
                                    try {
                                        f.get(CALL_TIMEOUT_SEC, TimeUnit.SECONDS);
                                    } catch (TimeoutException timeout) {
                                        LOGGER.warn("execute change listener is timeout in {} sec", CALL_TIMEOUT_SEC);
                                    } catch (Throwable t) {
                                        LOGGER.error("execute change listener in error ", t);
                                    }

                                }
                            }
                        }
                        Collections.sort(changes, InstanceChange.ReverseComparator);
                        //update latest change time
                        lastChangedTimeMills.get(serviceName).set(changes.get(0).changedTimeMills);
                    }
                } catch (Throwable t) {
                    LOGGER.error("Error when execute ChangesCheckTask.", t);
                }
            }
        }
    }

    /**
     * Constructor.
     *
     * @param directoryServiceClient the DirectoryServiceClient.
     */
    public DirectoryLookupService(DirectoryServiceClient directoryServiceClient) {
        this.directoryServiceClient = directoryServiceClient;
        this.changesCheckService.set(newChangesCheckService());
    }

    /**
     * Get the ModelService by service name.
     *
     * @param serviceName the Service name.
     * @return the ModelService.
     */
    public ModelService getModelService(String serviceName) {
        ModelService service = null;
        try{
            service = getDirectoryServiceClient().lookupService(serviceName);
        }catch(ServiceException se){
            if (se.getErrorCode()==ErrorCode.SERVICE_NOT_EXIST){
                LOGGER.error(se.getMessage());
            }else {
                LOGGER.error("Error when getModelService", se);
            }
        }
        return service;
    }

    /**
     * Get ModelMetadataKey, which is an object holding a list of service instances that
     * contain the key name in the service metadata.
     *
     * @param keyName the metadata key name.
     * @return the ModelMetadataKey.
     */
    protected ModelMetadataKey getModelMetadataKey(String keyName) {
        ModelMetadataKey key =  null;
        try {
           key = getDirectoryServiceClient().getMetadataKey(keyName);
        }catch (ServiceException se){
            LOGGER.error("Error when getModelMetadataKey",se);
        }
        return key;
    }

    /**
     * Get the ModelServiceInstance by serviceName and instanceId.
     *
     * @param serviceName the service name.
     * @param instanceId  the instanceId.
     * @return the ModelServiceInstance.
     * @deprecated replaced by {@link #getModelServiceInstanceByAddress}
     */
    @Deprecated
    public ModelServiceInstance getModelServiceInstance(String serviceName, String instanceId) {
        ModelService service = getModelService(serviceName);
        if (service != null && service.getServiceInstances() != null) {
            for (ModelServiceInstance instance : service.getServiceInstances()) {
                if (instance.getInstanceId().equals(instanceId)) {
                    return instance;
                } else if (instanceId.split("-")[0].equals(instance.getAddress())) {
                    //for 1.2 API old compatible, the input might be a providerId in "address-port" format
                    return instance;
                }
            }
        }
        return null;
    }

    /**
     * Get the ModelServiceInstance by serviceName and instanceAddress.
     *
     * @param serviceName     the service name.
     * @param instanceAddress the instanceAddress.
     * @return the ModelServiceInstance.
     * @since 1.2
     */
    public ModelServiceInstance getModelServiceInstanceByAddress(String serviceName, String instanceAddress) {
        ModelService service = getModelService(serviceName);
        if (service != null && service.getServiceInstances() != null) {
            for (ModelServiceInstance instance : service.getServiceInstances()) {
                if (instance.getAddress().equals(instanceAddress)) {
                    return instance;
                }
            }
        }
        return null;
    }

    /**
     * Get the ModelServiceInstance list that contains the metadata key.
     *
     * @param keyName the metadata key name.
     * @return the UP ModelServiceInstances that has the metadata key.
     */
    public List<ModelServiceInstance> getModelInstancesByMetadataKey(String keyName) {
        ModelMetadataKey key = getModelMetadataKey(keyName);
        if (key == null || key.getServiceInstances().isEmpty()) {
            return Collections.emptyList();
        } else {
            return new ArrayList<>(key.getServiceInstances());
        }
    }

    /**
     * Get the UP ModelServiceInstance list that contains the metadata key.
     *
     * @param keyName the metadata key name.
     * @return the ModelServiceInstances that have the metadata key.
     */
    public List<ModelServiceInstance> getUPModelInstancesByMetadataKey(String keyName) {
        List<ModelServiceInstance> list = new ArrayList<>();
        for (ModelServiceInstance instance : getModelInstancesByMetadataKey(keyName)) {
            if (instance.getStatus().equals(OperationalStatus.UP)) {
                list.add(instance);
            }
        }
        return list;
    }

    /**
     * Get the ModelServiceInstance list of the Service.
     *
     * @param serviceName the service name.
     * @return the ModelServiceInstance list of the Service.
     */
    public List<ModelServiceInstance> getModelInstances(String serviceName) {
        ModelService service = getModelService(serviceName);
        if (service == null || service.getServiceInstances().isEmpty()) {
            return Collections.emptyList();
        } else {
            return new ArrayList<>(service.getServiceInstances());
        }
    }

    /**
     * Get All ModelServiceInstance on the Directory Server.
     *
     * @return the ModelServiceInstance List.
     */
    public List<ModelServiceInstance> getAllInstances() {
        List<ModelServiceInstance> result = Collections.emptyList();
        try {
            result = getDirectoryServiceClient().getAllInstances();
        }catch (ServiceException se){
            LOGGER.error("Error when getAllInstances()",se);
        }
        return result;
    }

    /**
     * Get the UP ModelServiceInstance list of the Service.
     *
     * @param serviceName the service name.
     * @return the ModelServiceInstance list of the Service.
     */
    public List<ModelServiceInstance> getUPModelInstances(String serviceName) {
        List<ModelServiceInstance> list = new ArrayList<>();
        for (ModelServiceInstance instance : getModelInstances(serviceName)) {
            if (instance.getStatus().equals(OperationalStatus.UP)) {
                list.add(instance);
            }
        }
        return list;
    }

    /**
     * Add a NotificationHandler to the Service.
     * <p/>
     * This method checks the duplicated NotificationHandler for the serviceName, if the NotificationHandler
     * already exists for the serviceName, do nothing.
     *
     * @param serviceName the service name.
     * @param handler     the NotificationHandler for the service.
     */
    public void addNotificationHandler(String serviceName, NotificationHandler handler) {

        ServiceInstanceUtils.validateServiceName(serviceName);
        if (handler == null) {
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "NotificationHandler");
        }

        List<InstanceChangeListener<ModelServiceInstance>> listeners = changeListenerMap.get(serviceName);
        if (listeners != null) {
            for (InstanceChangeListener<ModelServiceInstance> listener : listeners) {
                if (listener instanceof NotificationHandlerAdapter
                        && ((NotificationHandlerAdapter) listener).getAdapter() == handler) {
                    //exist, log error and return
                    LOGGER.error("Try to register a handler {} that has already been registered.", handler);
                    return;
                }
            }
        }
        addInstanceChangeListener(serviceName, new NotificationHandlerAdapter(handler));
    }

    /**
     * Add a ServiceInstanceChangeListener to the Service.
     * <p/>
     * This method will check the duplicated listener for the serviceName, if the listener
     * already exists for the serviceName, do nothing.
     * <p/>
     * Throws IllegalArgumentException if serviceName or listener is null.
     *
     * @param serviceName the service name
     * @param listener    the ServiceInstanceChangeListener for the service
     */
    public void addServiceInstanceChangeListener(String serviceName, ServiceInstanceChangeListener listener) {
        ServiceInstanceUtils.validateServiceName(serviceName);
        if (listener == null) {
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "ServiceInstanceChangeListener");
        }

        List<InstanceChangeListener<ModelServiceInstance>> listeners = changeListenerMap.get(serviceName);
        if (listeners != null) {
            for (InstanceChangeListener<ModelServiceInstance> l : listeners) {
                if (l instanceof ServiceInstanceChangeListenerAdapter
                        && ((ServiceInstanceChangeListenerAdapter) l).getAdapter() == listener) {
                    //exist, log error and return
                    LOGGER.error("Try to register a listener {} that has already been registered.", listener);
                    return;
                }

            }
        }
        addInstanceChangeListener(serviceName, new ServiceInstanceChangeListenerAdapter(listener));

    }

    public void addInstanceChangeListener(String serviceName, InstanceChangeListener<ModelServiceInstance> listener) {
        Objects.requireNonNull(serviceName);
        CopyOnWriteArrayList<InstanceChangeListener<ModelServiceInstance>> listenerList = changeListenerMap.get(serviceName);
        if (listenerList == null) {
            listenerList = new CopyOnWriteArrayList<>();
            CopyOnWriteArrayList<InstanceChangeListener<ModelServiceInstance>> oldListeners
                    = changeListenerMap.putIfAbsent(serviceName, listenerList);
            if (oldListeners != null) {
                listenerList = oldListeners;
            }
        }
        listenerList.add(listener);
    }

    public void removeInstanceChangeListener(String serviceName, InstanceChangeListener<ModelServiceInstance> listener) {
        Objects.requireNonNull(serviceName);
        CopyOnWriteArrayList<InstanceChangeListener<ModelServiceInstance>> listeners = changeListenerMap.get(serviceName);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Remove the NotificationHandler from the Service.
     *
     * @param serviceName the service name.
     * @param handler     the NotificationHandler for the service.
     */
    public void removeNotificationHandler(String serviceName, NotificationHandler handler) {

        ServiceInstanceUtils.validateServiceName(serviceName);
        if (handler == null) {
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "NotificationHandler");
        }
        List<InstanceChangeListener<ModelServiceInstance>> list = changeListenerMap.get(serviceName);
        if (list != null) {
            boolean found = false;
            for (InstanceChangeListener<ModelServiceInstance> listener : list) {
                if (listener instanceof NotificationHandlerAdapter
                        && ((NotificationHandlerAdapter) listener).getAdapter() == handler) {
                    list.remove(listener);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new ServiceException(ErrorCode.NOTIFICATION_HANDLER_NOT_EXIST);
            }
        } else {
            throw new ServiceException(ErrorCode.SERVICE_NOT_EXIST, ErrorCode.SERVICE_NOT_EXIST.getMessageTemplate(), serviceName);
        }

    }


    /**
     * Remove a ServiceInstanceChangeListener from the Service.
     * <p/>
     * Throws IllegalArgumentException if serviceName or listener is null.
     *
     * @param serviceName the service name
     * @param listener    the ServiceInstanceChangeListener for the service
     */
    public void removeServiceInstanceChangeListener(String serviceName, ServiceInstanceChangeListener listener) {
        ServiceInstanceUtils.validateServiceName(serviceName);
        if (listener == null) {
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "ServiceInstanceChangeListener");
        }
        List<InstanceChangeListener<ModelServiceInstance>> list = changeListenerMap.get(serviceName);
        if (list != null) {
            boolean found = false;
            for (InstanceChangeListener<ModelServiceInstance> l : list) {
                if (l instanceof ServiceInstanceChangeListenerAdapter
                        && ((ServiceInstanceChangeListenerAdapter) l).getAdapter() == listener) {
                    list.remove(l);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new ServiceException(ErrorCode.SERVICE_INSTANCE_LISTENER_NOT_EXIST);
            }
            if (list.isEmpty()) {
                changeListenerMap.remove(serviceName);
            }
        } else {
            throw new ServiceException(ErrorCode.SERVICE_NOT_EXIST, ErrorCode.SERVICE_NOT_EXIST.getMessageTemplate(), serviceName);
        }

    }

    /**
     * remove all listeners by clean up the listener map.
     * beware, the method might/might not called from shutdown of the service.
     * If the method is called from shutdown. It means all listeners have to be registered again.
     * But in saturation of supporting of SD restart, the notification will not work again.
     */
    private void cleanUpChangeListenerMap() {
        if (LOGGER.isDebugEnabled()){
            dumpChangeListenerMap();
        }
        changeListenerMap.clear();
        LOGGER.debug("The listener map has been cleaned");
    }

    private void dumpChangeListenerMap() {
        for(Map.Entry<String, CopyOnWriteArrayList<InstanceChangeListener<ModelServiceInstance>>> entry : changeListenerMap.entrySet()){
            String serviceName = entry.getKey();
            CopyOnWriteArrayList<InstanceChangeListener<ModelServiceInstance>> listenerList = entry.getValue();
            LOGGER.debug("Dump listener List for {}",serviceName);
            for(InstanceChangeListener<ModelServiceInstance> listener : listenerList){
                LOGGER.debug("  {} -> {}",serviceName,listener);
            }
        }
    }

    /**
     * Get the DirectoryServiceClient.
     *
     * @return the DirectoryServiceClient
     */
    protected DirectoryServiceClient getDirectoryServiceClient() {
        return this.directoryServiceClient;
    }

}
