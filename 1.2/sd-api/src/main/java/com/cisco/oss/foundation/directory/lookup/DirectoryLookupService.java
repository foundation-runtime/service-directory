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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.NotificationHandler;
import com.cisco.oss.foundation.directory.ServiceInstanceChangeListener;
import com.cisco.oss.foundation.directory.client.DirectoryServiceClient;
import com.cisco.oss.foundation.directory.entity.InstanceChange;
import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.impl.InstanceChangeAdapter;
import com.cisco.oss.foundation.directory.impl.ServiceDirectoryService;
import com.cisco.oss.foundation.directory.utils.ServiceInstanceUtils;

import static com.cisco.oss.foundation.directory.entity.InstanceChange.ChangeType.Status;
import static com.cisco.oss.foundation.directory.entity.InstanceChange.toServiceInstanceChange;

/**
 * It is the Directory LookupService to perform the lookup functionality.
 *
 * It looks up ServiceInstance from Directory Server.
 *
 *
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
    private final Map<String, List<ServiceInstanceChangeListener>> changeListeners = new HashMap<>();


    /**
     * If the service is stared
     */
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    @Override
    public void start() {
        super.start();
        if (isStarted.compareAndSet(false,true)){
            initChangesCheckTask();
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (isStarted.compareAndSet(true,false)) {
            ScheduledExecutorService service = changesCheckService.getAndSet(newChangesCheckService());
            service.shutdown();
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
                0L, 1L, TimeUnit.SECONDS); //TODO, interval might need to be configurable
        LOGGER.info("Service Instances Changes Checking Task is started");
    }




    private class ChangesCheckTask implements Runnable {
        // the time-mills when the task is initialized
        private final long INIT_TIME_MILLS;

        // Although the map is not required thread-safe, since the task itself is single=threaded.
        // but is reasonable to protect it since the single thread is easy to break by changing
        // the way of object creation and service scheduling.
        private final ConcurrentHashMap<String, AtomicLong> lastChangedTimeMills = new ConcurrentHashMap<>();

        ChangesCheckTask(long initTimeMills) {
            this.INIT_TIME_MILLS = initTimeMills;
        }

        private void syncServiceListForChangedTimeMap(final List<String> nameList){
            List<String> removeList = new ArrayList<>();
            for (String service : lastChangedTimeMills.keySet()){
               if (!nameList.contains(service)){
                   removeList.add(service);
               }
            }
            for (String s : removeList){
                lastChangedTimeMills.remove(s);
            }
        }

        @Override
        public void run() {
            List<String> serviceList = new ArrayList<>();
            synchronized (changeListeners) {
                serviceList.addAll(changeListeners.keySet());
            }

            //check if has service name need to be removed in last changed time map
            //for example, when unregister a service.
            syncServiceListForChangedTimeMap(serviceList);

            for (String service : serviceList) {
                lastChangedTimeMills.putIfAbsent(service, new AtomicLong(INIT_TIME_MILLS));
                try {
                    List<InstanceChange<ModelServiceInstance>> changes = getDirectoryServiceClient()
                            .lookupChangesSince(service, lastChangedTimeMills.get(service).longValue());
                    //has changes
                    if (!changes.isEmpty()) {
                        //oldest first
                        Collections.sort(changes, InstanceChange.Comparator);
                        for (InstanceChange<ModelServiceInstance> c : changes) {
                            List<ServiceInstanceChangeListener> listenerList = new ArrayList<>();
                            synchronized (changeListeners) {
                                if (changeListeners.containsKey(c.serviceName)) {
                                    listenerList.addAll(changeListeners.get(c.serviceName));
                                }
                            }
                            for (ServiceInstanceChangeListener l : listenerList) {
                                l.onChange(c.changeType,toServiceInstanceChange(c));
                            }
                        }
                        Collections.sort(changes,InstanceChange.ReverseComparator);
                        //update latest change time
                        lastChangedTimeMills.get(service).set(changes.get(0).changedTimeMills);
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
     * @param directoryServiceClient
     *         the DirectoryServiceClient.
     */
    public DirectoryLookupService(DirectoryServiceClient directoryServiceClient) {
        this.directoryServiceClient = directoryServiceClient;
        this.changesCheckService.set(newChangesCheckService());
    }

    /**
     * Get the ModelService by service name.
     *
     * @param serviceName
     *         the Service name.
     * @return
     *         the ModelService.
     */
    protected ModelService getModelService(String serviceName) {
        return getDirectoryServiceClient().lookupService(serviceName);
    }

    /**
     * Get ModelMetadataKey, which is an object holding a list of service instances that
     * contain the key name in the service metadata.
     *
     * @param keyName
     *         the metadata key name.
     * @return
     *         the ModelMetadataKey.
     */
    protected ModelMetadataKey getModelMetadataKey(String keyName) {
        return getDirectoryServiceClient().getMetadataKey(keyName);
    }

    /**
     * Get the ModelServiceInstance by serviceName and instanceId.
     *
     * @param serviceName
     *         the service name.
     * @param instanceId
     *         the instanceId.
     * @return
     *         the ModelServiceInstance.
     * @deprecated replaced by {@link #getModelServiceInstanceByAddress}
     */
    @Deprecated
    public ModelServiceInstance getModelServiceInstance(String serviceName, String instanceId) {
        ModelService service = getModelService(serviceName);
        if (service != null && service.getServiceInstances() != null) {
            for (ModelServiceInstance instance : service.getServiceInstances()) {
                if (instance.getInstanceId().equals(instanceId)) {
                    return instance;
                }else if (instanceId.split("-")[0].equals(instance.getAddress())){
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
     * @param serviceName
     *         the service name.
     * @param instanceAddress
     *         the instanceAddress.
     * @return
     *         the ModelServiceInstance.
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
     * @param keyName
     *         the metadata key name.
     * @return
     *         the UP ModelServiceInstances that has the metadata key.
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
     *
     * @param keyName
     *         the metadata key name.
     * @return
     *         the ModelServiceInstances that have the metadata key.
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
     * @param serviceName
     *         the service name.
     * @return
     *         the ModelServiceInstance list of the Service.
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
     * @return
     *         the ModelServiceInstance List.
     */
    public List<ModelServiceInstance> getAllInstances() {
        return getDirectoryServiceClient().getAllInstances();
    }

    /**
     * Get the UP ModelServiceInstance list of the Service.
     *
     * @param serviceName
     *         the service name.
     * @return
     *         the ModelServiceInstance list of the Service.
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
     *
     * This method checks the duplicated NotificationHandler for the serviceName, if the NotificationHandler
     * already exists for the serviceName, do nothing.
     *
     * @param serviceName
     *         the service name.
     * @param handler
     *         the NotificationHandler for the service.
     */
    public void addNotificationHandler(String serviceName, NotificationHandler handler) {

        ServiceInstanceUtils.validateServiceName(serviceName);
        if (handler == null) {
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "NotificationHandler");
        }

        synchronized (changeListeners) {
            if (!changeListeners.containsKey(serviceName)) {
                changeListeners.put(serviceName, new ArrayList<ServiceInstanceChangeListener>());
            }
            List<ServiceInstanceChangeListener> listeners = changeListeners.get(serviceName);
            if (!listeners.isEmpty()){
                for(ServiceInstanceChangeListener listener : listeners){
                    if (listener instanceof InstanceChangeAdapter
                            && ((InstanceChangeAdapter)listener).getHandler()==handler){
                            //exist, log error and return
                            LOGGER.error("Try to register a handler {} that has already been registered.",handler);
                            return;
                    }

                }
            }
            changeListeners.get(serviceName).add(new InstanceChangeAdapter(handler));
        }

    }

    /**
     * Add a ServiceInstanceChangeListener to the Service.
     *
     * This method will check the duplicated listener for the serviceName, if the listener
     * already exists for the serviceName, do nothing.
     *
     * Throws IllegalArgumentException if serviceName or listener is null.
     *
     * @param serviceName
     *          the service name
     * @param listener
     *          the ServiceInstanceChangeListener for the service
     */
    public void addInstanceChangeListener(String serviceName, ServiceInstanceChangeListener listener) {
        ServiceInstanceUtils.validateServiceName(serviceName);
        if (listener == null) {
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "ServiceInstanceChangeListener");
        }
        synchronized (changeListeners) {
            if (!changeListeners.containsKey(serviceName)) {
                changeListeners.put(serviceName, new ArrayList<ServiceInstanceChangeListener>());
            }
            if (!changeListeners.get(serviceName).contains(listener)) {
                changeListeners.get(serviceName).add(listener);
            }
        }
    }

    /**
     * Remove the NotificationHandler from the Service.
     *
     * @param serviceName
     *         the service name.
     * @param handler
     *         the NotificationHandler for the service.
     */
    public void removeNotificationHandler(String serviceName, NotificationHandler handler) {

        ServiceInstanceUtils.validateServiceName(serviceName);
        if (handler == null) {
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "NotificationHandler");
        }

        synchronized (changeListeners) {
            if (changeListeners.containsKey(serviceName)) {
                List<ServiceInstanceChangeListener> list = changeListeners.get(serviceName);
                boolean found = false;
                for (ServiceInstanceChangeListener listener : list){
                   if (listener instanceof InstanceChangeAdapter
                           && ((InstanceChangeAdapter)listener).getHandler()==handler) {
                       list.remove(listener);
                       found = true;
                       break;
                   }
                }
                if (!found){
                    //TODO, might a specified error code for handler not found.
                    throw new ServiceException(ErrorCode.GENERAL_ERROR, "NotificationHandler not exist, It may has been removed or has not been registered before.");
                }
                if (list.isEmpty()) {
                    changeListeners.remove(serviceName);
                }
            } else {
                throw new ServiceException(ErrorCode.SERVICE_NOT_EXIST, ErrorCode.SERVICE_NOT_EXIST.getMessageTemplate(), serviceName);
            }
        }
    }


    /**
     * Remove a ServiceInstanceChangeListener from the Service.
     *
     * Throws IllegalArgumentException if serviceName or listener is null.
     * @param serviceName
     *          the service name
     * @param listener
     *          the ServiceInstanceChangeListener for the service
     */
    public void removeInstanceChangeListener(String serviceName, ServiceInstanceChangeListener listener) {
        ServiceInstanceUtils.validateServiceName(serviceName);
        if (listener == null) {
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "ServiceInstanceChangeListener");
        }
        synchronized (changeListeners) {
            if (changeListeners.containsKey(serviceName)) {
                List<ServiceInstanceChangeListener> list = changeListeners.get(serviceName);
                if (list.contains(listener)){
                    list.remove(listener);
                }else{
                    throw new ServiceException(ErrorCode.GENERAL_ERROR, "ServiceInstanceChangeListener not exist, It may has been removed or has not been registered before.");
                }
                if (list.isEmpty()) {
                    changeListeners.remove(serviceName);
                }
            } else {
                throw new ServiceException(ErrorCode.SERVICE_NOT_EXIST, ErrorCode.SERVICE_NOT_EXIST.getMessageTemplate(), serviceName);
            }
        }
    }

    /**
     * Get the DirectoryServiceClient.
     *
     * @return
     *         the DirectoryServiceClient
     */
    protected DirectoryServiceClient getDirectoryServiceClient() {
        return this.directoryServiceClient;
    }

}
