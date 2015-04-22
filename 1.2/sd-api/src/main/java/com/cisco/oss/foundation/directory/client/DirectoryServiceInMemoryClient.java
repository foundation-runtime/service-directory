package com.cisco.oss.foundation.directory.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationResult;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstanceHeartbeat;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceDirectoryError;
import com.cisco.oss.foundation.directory.utils.ServiceInstanceUtils;

/**
 * It's client works as a in-memory server. so that sd-api can work as-like there is a real sd-server.
 * It's useful for unit/integration in some case. The different between {@code DirectoryServiceDummyClient} is
 * The dummy one actually do nothing. but in-memory do something really
 *
 * @see DirectoryServiceDummyClient
 * @since 1.2
 */
public class DirectoryServiceInMemoryClient implements DirectoryServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryServiceInMemoryClient.class);
    /**
     * in-memory registry store
     * K1 -> Service Name , K2 -> ProviderId
     */
    private final ConcurrentMap<String, ConcurrentMap<String, ModelServiceInstance>> inMemoryRegistry = new
            ConcurrentHashMap<>();

    private static final int MAX_CHARGES_HISTORY_SIZE=500; //TODO, use config

    private final BlockingQueue<InstanceChange<ModelServiceInstance>> changeHistory =
            new ArrayBlockingQueue<>(MAX_CHARGES_HISTORY_SIZE);

    private void addToHistory(InstanceChange<ModelServiceInstance> change){
        if (change == null) {
            throw new IllegalArgumentException("ServiceInstanceChange should not be null");
        }
        //if history is full, remove the oldest one, because FIFO queue, so that take() will remove the oldest
        if (0 == changeHistory.remainingCapacity()){
            try {
                changeHistory.take();
            } catch (InterruptedException e) {
                LOGGER.error("error when try to remove change from history queue",e);
            }
        }
        try {
            changeHistory.put(change);
        } catch (InterruptedException e) {
            LOGGER.error("error when try to add change into history queue", e);
        }
        LOGGER.debug("addToHistory {}. {} capacity left",change,changeHistory.remainingCapacity());
    }

    // ----------------------
    //  internal helper methods to convert between model-instance and provided-instance
    // ----------------------
    //TODO refactor the sd-core's ServiceInstanceUtils,
    //TODO refactor ModelService/ModelServiceInstance itself
    //TODO merge the instance/modelInstance conversion methods when we refactor model.
    //for now, use the internal methods created here
    private static ModelService _newModelService(String serviceName) {
        // TODO might changed in the future
        // in current sd-service implementation, the 'id' equals 'serviceName'
        // so we keep the pattern,
        // TODO the create/modify time for service are redundancies, in instance level will do
        // use 0L 1970-Jun-01 as created time. (should not depends on it)
        return new ModelService(serviceName, serviceName, new Date(0L));

    }

    private static ModelServiceInstance _newModelInstanceFrom(ProvidedServiceInstance instance) {

        ModelServiceInstance mInstance = new ModelServiceInstance();

        mInstance.setServiceName(instance.getServiceName());
        mInstance.setAddress(instance.getAddress());
        mInstance.setPort(instance.getPort());
        mInstance.setStatus(instance.getStatus());
        mInstance.setUri(instance.getUri());

        //TODO, refactor the model, the current implementation 'id' and 'instanceId' both => providerId
        mInstance.setId(instance.getProviderId());
        mInstance.setInstanceId(instance.getProviderId());

        //TODO, refactor the model, now use the monitor enabled by default
        mInstance.setMonitorEnabled(true);

        //TODO, refactor the model,
        // 1.) the xxxTime need not to be declared as Date, use long as milli-secs is enough
        // 2.) create-time should never be changed, so need to be final
        final long now = System.currentTimeMillis();
        mInstance.setCreateTime(new Date(now));
        mInstance.setModifiedTime(new Date(now));

        //TODO, refactor the model, need to decide how to handle the field
        //now use '1970-Jan-01' as initial value.
        mInstance.setHeartbeatTime(new Date(0L));

        //TODO, do we need to support metadata?
        //for now, we don't need support metadata
        mInstance.setMetadata(Collections.<String, String>emptyMap());

        return mInstance;
    }

    private ProvidedServiceInstance _toProvidedInstance(ModelServiceInstance mInstance) {
        return new ProvidedServiceInstance(mInstance.getServiceName(), mInstance.getAddress(),
                mInstance.getPort(), mInstance.getUri(), mInstance.getStatus(),
                //TODO, do not support metadata for now
                Collections.<String, String>emptyMap());
    }

    private static String objHashStr(Object o) {
        return o.getClass().getSimpleName() + "@" + Integer.toHexString(o.hashCode());
    }

    @Override
    public void registerInstance(ProvidedServiceInstance instance) {
        String serviceName = instance.getServiceName();
        ConcurrentMap<String, ModelServiceInstance> previousMap = inMemoryRegistry.putIfAbsent(serviceName, new ConcurrentHashMap<String, ModelServiceInstance>());
        if (previousMap == null) {
            LOGGER.debug("new service Map is created for {}", serviceName);
        }
        ConcurrentMap<String, ModelServiceInstance> iMap = inMemoryRegistry.get(serviceName);
        ModelServiceInstance newInstance = _newModelInstanceFrom(instance);
        ModelServiceInstance previous = iMap.putIfAbsent(instance.getProviderId(), newInstance);
        if (previous != null) {
            LOGGER.debug("ModelServiceInstance id {} already registered by {} ", instance.getProviderId(), objHashStr(previous));
        } else {
            addToHistory(new InstanceChange<>(newInstance.getModifiedTime().getTime(),
                    newInstance,
                    InstanceChange.ChangeType.Create,
                    "null", //
                    newInstance.toString()
            ));
            LOGGER.debug("Registered new ModelServiceInstance {} with id {} ", objHashStr(newInstance), instance.getProviderId());
        }

    }

    @Deprecated
    @Override
    public void updateInstance(ProvidedServiceInstance instance) {
        throw new UnsupportedOperationException("updateInstance(ProvidedServiceInstance instance) not supported since 1.2");
        /*
        String serviceName = instance.getServiceName();
        Map<String, ModelServiceInstance> iMap = inMemoryRegistry.get(serviceName);
        if (iMap == null) {
            LOGGER.debug("Service {} not exist", serviceName);
        } else {
            ModelServiceInstance mInstance = iMap.get(instance.getProviderId());
            if (mInstance == null) {
                LOGGER.debug("ModelServiceInstance is not found by {}", instance.getProviderId());
            } else {
                mInstance.setServiceName(instance.getServiceName());
                mInstance.setAddress(instance.getAddress());
                mInstance.setPort(instance.getPort());
                mInstance.setStatus(instance.getStatus());
                mInstance.setUri(instance.getUri());
                mInstance.setId(instance.getProviderId());
                mInstance.setInstanceId(instance.getProviderId());
                mInstance.setModifiedTime(new Date());
                mInstance.setMetadata(instance.getMetadata());
                LOGGER.debug("ModelServiceInstance {} is update by {}", objHashStr(mInstance), objHashStr(instance));
            }
        }
        */
    }

    private ModelServiceInstance _getInstance(String serviceName, String instanceId) {
        Map<String, ModelServiceInstance> iMap = inMemoryRegistry.get(serviceName);
        if (iMap != null) {
            return iMap.get(instanceId);
        }
        return null;
    }

    @Override
    public void updateInstanceStatus(String serviceName, String instanceId, OperationalStatus status, boolean isOwned) {
        if (status==null){
            throw new NullPointerException("status is null");
        }
        ModelServiceInstance mInstance = _getInstance(serviceName, instanceId);
        if (mInstance != null) {
            if (!isOwned) {
                //IN this imple, we allow update don't care of if the service instance is owned by user
                LOGGER.warn("do updateInstanceStatus even when isOwned is false");
            }
            final OperationalStatus oldStatus = mInstance.getStatus(); // might null
            mInstance.setStatus(status);
            mInstance.setModifiedTime(new Date());
            addToHistory(new InstanceChange<>(mInstance.getModifiedTime().getTime(),
                    mInstance,
                    InstanceChange.ChangeType.Status,
                    oldStatus==null?"null":oldStatus.getName(), //initialized as null
                    status.getName()
                    ));
        } else {
            LOGGER.warn("no service instance exist for {} {}", serviceName, instanceId);
        }
    }

    @Override
    public void updateInstanceUri(String serviceName, String instanceId, String uri, boolean isOwned) {
        ModelServiceInstance mInstance = _getInstance(serviceName, instanceId);
        if (mInstance != null) {
            if (!isOwned) {
                //IN this imple, we allow update don't care of if the service instance is owned by user
                LOGGER.debug("do updateInstanceUri even when isOwned is false");
            }
            final String oldUri = mInstance.getUri();
            mInstance.setUri(uri);
            mInstance.setModifiedTime(new Date());
            addToHistory(new InstanceChange<>(mInstance.getModifiedTime().getTime(),
                    mInstance,
                    InstanceChange.ChangeType.URL,
                    oldUri,uri));
        } else {
            LOGGER.debug("no service instance exist for {} {}", serviceName, instanceId);
        }
    }

    @Override
    public void unregisterInstance(String serviceName, String instanceId, boolean isOwned) {
        ConcurrentMap<String, ModelServiceInstance> iMap = inMemoryRegistry.get(serviceName);
        if (iMap == null) {
            LOGGER.warn("Service {} not exist", serviceName);
        } else {
            ModelServiceInstance previousInstance = iMap.remove(instanceId);
            if (previousInstance != null) {
                LOGGER.debug("service instance {} removed by name : {} , id : {}", previousInstance, serviceName, instanceId);
                addToHistory(new InstanceChange<>(previousInstance.getModifiedTime().getTime(),
                        previousInstance,
                        InstanceChange.ChangeType.Remove,
                        previousInstance.toString(), "null"));
            } else {
                LOGGER.debug("no service instance exist for {} {}", serviceName, instanceId);
            }
        }
    }

    @Override
    public Map<String, OperationResult<String>> sendHeartBeat(Map<String, ServiceInstanceHeartbeat> heartbeatMap) {
        HashMap<String, OperationResult<String>> result = new HashMap<>();
        for (Map.Entry<String, ServiceInstanceHeartbeat> entry : heartbeatMap.entrySet()) {
            String id = entry.getKey(); //What's Id means?
            ServiceInstanceHeartbeat heartbeat = entry.getValue();
            String serviceName = heartbeat.getServiceName();
            String providedId = heartbeat.getServiceName();
            ModelServiceInstance instance = _getInstance(serviceName, providedId);
            if (instance != null) {
                //TODO, refactoring model
                final long now = System.currentTimeMillis();
                instance.setHeartbeatTime(new Date(now));
                instance.setModifiedTime(new Date(now));
                //TODO, refactor OperationResult structure
                result.put(id, new OperationResult<String>(true, null, null));
                LOGGER.debug("heart beat send ok for {}", instance);
            } else {
                LOGGER.debug("no service instance exist for {} {}", serviceName, providedId);
                result.put(id, new OperationResult<String>(false, null, new ServiceDirectoryError(ErrorCode.SERVICE_INSTANCE_NOT_EXIST, id)));
            }
        }
        return result;
    }

    @Override
    public ModelService lookupService(String serviceName) {
        Map<String, ModelServiceInstance> iMap = inMemoryRegistry.get(serviceName);
        if (iMap != null) {
            ModelService service = _newModelService(serviceName);
            if (!iMap.isEmpty()) {
                List<ModelServiceInstance> instanceList = new ArrayList<>(iMap.entrySet().size());
                List<Long> modifiedTimes = new ArrayList<>(instanceList.size());
                for (Map.Entry<String, ModelServiceInstance> entry : iMap.entrySet()) {
                    ModelServiceInstance mInstance = entry.getValue();
                    instanceList.add(mInstance);
                    modifiedTimes.add(mInstance.getModifiedTime().getTime());
                }
                service.setServiceInstances(instanceList);
                // the latest modify time
                Collections.sort(modifiedTimes, Collections.reverseOrder());
                service.setModifiedTime(new Date(modifiedTimes.get(0)));
            }
            return service;
        } else {
            LOGGER.debug("Service {} not exist", serviceName);
            return null;
        }

    }

    @Override
    public List<ModelServiceInstance> getAllInstances() {
        List<ModelServiceInstance> instanceList = new ArrayList<>();
        for (Map<String, ModelServiceInstance> entry : inMemoryRegistry.values()) {
            instanceList.addAll(entry.values());
            /*
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
        }
        LOGGER.debug("getAllInstances() {}", instanceList);
        return Collections.unmodifiableList(instanceList);

    }

    @Override
    //TODO, refactor upper interface.
    // the input parameter map stands for all services, don't know why the interface is defined like this?
    //TODO, refactor logic
    public Map<String, OperationResult<ModelService>> getChangedServices(Map<String, ModelService> services) {
        Map<String, OperationResult<ModelService>> map = new HashMap<>();
        for (Map.Entry<String, ModelService> entry : services.entrySet()) {
            //TODO, refactor model, key in map is service name, redundancy with ModelService.serviceName
            String serviceName = entry.getKey();
            ModelService oldService = entry.getValue();
            LOGGER.debug("check for {} -> {}", serviceName, oldService);
            ModelService latestService = lookupService(serviceName);
            if (latestService != null) {
                LOGGER.debug("the latest service instance is {}", latestService);
                if (oldService.getModifiedTime()==null){
                    oldService.setModifiedTime(new Date(0L)); // null means never changed. so we set up to oldest
                }
                LOGGER.debug("new modifyTime {} vs. old modifiedTime {}",latestService.getModifiedTime().getTime(),oldService.getModifiedTime().getTime());
                if (latestService.getModifiedTime().getTime() > oldService.getModifiedTime().getTime()) {
                    map.put(serviceName, new OperationResult<>(true, latestService, null));
                } else {
                    map.put(serviceName, new OperationResult<ModelService>(false, null,new ServiceDirectoryError(ErrorCode.GENERAL_ERROR)));
                }
            }

        }
        return map;
    }



    @Override
    public ModelMetadataKey getMetadataKey(String keyName) {
        //TODO, should we support metadata?
        throw new UnsupportedOperationException("get metadata is not supported now");
    }

    @Override
    public Map<String, OperationResult<ModelMetadataKey>> getChangedMetadataKeys(Map<String, ModelMetadataKey> keys) {
        //TODO, should we support metadata?
        throw new UnsupportedOperationException("get metadata change is not supported now");
    }

    @Override
    public void setInvoker(DirectoryInvoker invoker) {
        //DO nothing.
    }




    //-------------------------------
    // 1.2 API
    //-------------------------------

    public long getLastChangedTimeMills(String serviceName) {
        ModelService service = lookupService(serviceName);
        return service == null ? -1L : lookupService(serviceName).getModifiedTime().getTime();
    }

    /**
     * @param serviceName
     * @param since
     * @return
     */
    public List<ServiceInstance> lookUpChangedServiceInstancesSince(String serviceName, long since){
        List<ServiceInstance> changed = new ArrayList<>();
        // the latest in model
        ModelService latest = lookupService(serviceName);
        if (since < latest.getModifiedTime().getTime()){
            // has changes , so where is the changes?
            for (ModelServiceInstance instance : latest.getServiceInstances()){
                if (since < instance.getModifiedTime().getTime()){
                    changed.add(ServiceInstanceUtils.toServiceInstance(instance));
                }
            }
        }
        return changed;
    }

    @Override
    public List<InstanceChange<ServiceInstance>> lookupChangesSince(String serviceName, long since) {

        InstanceChange<ModelServiceInstance>[] all = changeHistory.toArray((InstanceChange<ModelServiceInstance>[]) new InstanceChange[changeHistory.size()]);
        int index = -1;
        for (int i = 0; i < all.length; i++) {
            if (all[i].changedTimeMills > since) {
                index = i;
                LOGGER.debug("found changes {} at {} since {}",all[i].changedTimeMills,index,since);
                break;
            }
        }
        if (index >= 0){
            final int newLength = all.length-index;
            List<InstanceChange<ServiceInstance>> result = new ArrayList<>(newLength);
            for (int i=index; i<all.length; i++){
                if (all[i]==null) break; //no more items. in the case
                //check if the instance belongs to serviceName
                ModelServiceInstance instance = all[i].changed;
                ConcurrentMap<String, ModelServiceInstance> instances = inMemoryRegistry.get(serviceName);
                if(instances!=null&& instances.containsValue(instance)){
                    LOGGER.debug("build change {} to result list",all[i]);
                    result.add(new InstanceChange<ServiceInstance>(all[i].changedTimeMills,
                        ServiceInstanceUtils.toServiceInstance(instance),
                        all[i].changeType, all[i].from, all[i].to));
                }
            }
            return result;
        }
        return Collections.emptyList();
    }
}
