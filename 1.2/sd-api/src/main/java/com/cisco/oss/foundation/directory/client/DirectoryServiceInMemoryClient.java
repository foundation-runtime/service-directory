package com.cisco.oss.foundation.directory.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationResult;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstanceHeartbeat;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceDirectoryError;

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
    private final ConcurrentHashMap<String, Map<String, ModelServiceInstance>> inMemoryRegistry = new
            ConcurrentHashMap<>();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock read = readWriteLock.readLock();
    private final Lock write = readWriteLock.writeLock();

    // ----------------------
    //  internal helper methods to convert between model-instance and provided-instance
    // ----------------------
    //TODO refactor the sd-core's ServiceInstanceUtils,
    //TODO refactor ModelService/ModelServiceInstance itself
    //TODO merge the instance/modelInstance conversion methods when we refactor model.
    //for now, use the internal methods created here
    private static ModelService _newModelService(String serviceName){
        // TODO might changed in the future
        // in current sd-service implementation, the 'id' equals 'serviceName'
        // so we keep the pattern,
        // TODO the create/modify time for service are redundancies, in instance level will do
        // use 0L 1970-Jun-01 as created time. (should not depends on it)
        return new ModelService(serviceName,serviceName,new Date(0L));

    }
    private static ModelServiceInstance _newModelInstanceFrom(ProvidedServiceInstance instance){

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
    private ProvidedServiceInstance _toProvidedInstance(ModelServiceInstance mInstance){
        return new ProvidedServiceInstance(mInstance.getServiceName(),mInstance.getAddress(),
                mInstance.getPort(),mInstance.getUri(),mInstance.getStatus(),
                //TODO, do not support metadata for now
                Collections.<String,String>emptyMap());
    }


    @Override
    public void registerInstance(ProvidedServiceInstance instance) {
        try {
            read.lock();
            String serviceName = instance.getServiceName();
            inMemoryRegistry.putIfAbsent(serviceName, new HashMap<String, ModelServiceInstance>());
            Map<String, ModelServiceInstance> iMap = inMemoryRegistry.get(serviceName);
            if (iMap.containsKey(instance.getProviderId())) {
                //already exist
                LOGGER.warn("ModelServiceInstance id {} already exist", instance.getProviderId());
            } else {
                iMap.put(instance.getProviderId(),_newModelInstanceFrom(instance));
            }
        } finally {
            read.unlock();
        }

    }

    @Override
    public void updateInstance(ProvidedServiceInstance instance) {
        try {
            read.lock();
            String serviceName = instance.getServiceName();
            Map<String, ModelServiceInstance> iMap = inMemoryRegistry.get(serviceName);
            if (iMap == null) {
                LOGGER.warn("Service {} not exist", serviceName);
            } else {
                ModelServiceInstance mInstance = iMap.get(instance.getProviderId());
                if (mInstance == null) {
                    LOGGER.warn("ModelServiceInstance is not found by {}", instance.getProviderId());
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
                }
            }
        } finally {
            read.unlock();
        }
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
        try {
            read.lock();
            ModelServiceInstance mInstance = _getInstance(serviceName, instanceId);
            if (mInstance != null) {
                if (!isOwned) {
                    //IN this imple, we allow update don't care of if the service instance is owned by user
                    LOGGER.warn("do updateInstanceStatus even when isOwned is false");
                }
                mInstance.setStatus(status);
                mInstance.setModifiedTime(new Date());
            } else {
                LOGGER.warn("no service instance exist for {} {}", serviceName, instanceId);
            }
        } finally {
            read.unlock();
        }

    }

    @Override
    public void updateInstanceUri(String serviceName, String instanceId, String uri, boolean isOwned) {
        try {
            read.lock();
            ModelServiceInstance mInstance = _getInstance(serviceName, instanceId);
            if (mInstance != null) {
                if (!isOwned) {
                    //IN this imple, we allow update don't care of if the service instance is owned by user
                    LOGGER.warn("do updateInstanceUri even when isOwned is false");
                }
                mInstance.setUri(uri);
                mInstance.setModifiedTime(new Date());
            } else {
                LOGGER.warn("no service instance exist for {} {}", serviceName, instanceId);
            }
        } finally {
            read.unlock();
        }

    }

    @Override
    public void unregisterInstance(String serviceName, String instanceId, boolean isOwned) {
        try {
            read.lock();
            Map<String, ModelServiceInstance> iMap = inMemoryRegistry.get(serviceName);
            if (iMap == null) {
                LOGGER.warn("Service {} not exist", serviceName);
            } else {
                if (iMap.containsKey(instanceId)) {
                    iMap.remove(instanceId);
                } else {
                    LOGGER.warn("no service instance exist for {} {}", serviceName, instanceId);
                }
            }

        } finally {
            read.unlock();
        }

    }

    @Override
    public Map<String, OperationResult<String>> sendHeartBeat(Map<String, ServiceInstanceHeartbeat> heartbeatMap) {
        HashMap<String, OperationResult<String>> result = new HashMap<>();
        try {
            read.lock();
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
                } else {
                    LOGGER.warn("no service instance exist for {} {}", serviceName, providedId);
                    result.put(id, new OperationResult<String>(false, null, new ServiceDirectoryError(ErrorCode.SERVICE_INSTANCE_NOT_EXIST, id)));
                }
            }
        } finally {
            read.unlock();
        }
        return result;
    }

    @Override
    public ModelService lookupService(String serviceName) {
        try {
            write.lock();
            Map<String, ModelServiceInstance> iMap = inMemoryRegistry.get(serviceName);
            if (iMap!=null){
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
            }else{
                LOGGER.warn("Service {} not exist", serviceName);
                return null;
            }

        } finally {
            write.unlock();
        }
    }

    @Override
    public List<ModelServiceInstance> getAllInstances() {
        try {
            write.lock();
            List<ModelServiceInstance> instanceList = new ArrayList<>();
            for (Map<String,ModelServiceInstance> entry : inMemoryRegistry.values()){
                instanceList.addAll(entry.values());
            }
            return instanceList;
        }finally {
            write.unlock();
        }
    }

    @Override
    //TODO, refactor upper interface.
    // the input parameter map stands for all services, don't know why the interface is defined like this?
    //TODO, refactor logic
    public Map<String, OperationResult<ModelService>> getChangedServices(Map<String, ModelService> services) {
        Map<String, OperationResult<ModelService>> map = new HashMap<>();
        for (Map.Entry<String,ModelService> entry : services.entrySet()){
            //TODO, refactor model, key in map is service name, redundancy with ModelService.serviceName
            String serviceName = entry.getKey();
            ModelService oldService = entry.getValue();
            ModelService latestService = lookupService(serviceName);
            if (latestService.getModifiedTime().getTime() > oldService.getModifiedTime().getTime()){
                map.put(serviceName,new OperationResult<>(true,latestService,null));
            }else{
                map.put(serviceName,new OperationResult<>(false,oldService,null));
            }
        }
        return null;
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
}
