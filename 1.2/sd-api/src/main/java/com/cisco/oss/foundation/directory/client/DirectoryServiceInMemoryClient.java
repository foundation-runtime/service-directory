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

import com.cisco.oss.foundation.directory.entity.InstanceChange;
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
 * InMemory client works as a in-memory server. so that sd-api can work as-like there is a real sd-server.<p>
 * It's useful for unit/integration test in some case. The different with {@link DirectoryServiceDummyClient}
 * is the dummy-client actually do nothing, but the in-memory client will maintain all service instances in
 * memory.
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

    private static final int MAX_CHARGES_HISTORY_SIZE = 100;

    private final BlockingQueue<InstanceChange<ModelServiceInstance>> changeHistory =
            new ArrayBlockingQueue<>(MAX_CHARGES_HISTORY_SIZE);

    private void addToHistory(InstanceChange<ModelServiceInstance> change) {
        if (change == null) {
            throw new IllegalArgumentException("ServiceInstanceChange should not be null");
        }
        //if history is full, remove the oldest one, because FIFO queue, so that take() will remove the oldest
        if (0 == changeHistory.remainingCapacity()) {
            try {
                changeHistory.take();
            } catch (InterruptedException e) {
                LOGGER.error("error when try to remove change from history queue", e);
            }
        }
        try {
            changeHistory.put(change);
        } catch (InterruptedException e) {
            LOGGER.error("error when try to add change into history queue", e);
        }
        LOGGER.debug("addToHistory {}. {} capacity left", change, changeHistory.remainingCapacity());
    }

    // ----------------------
    //  internal helper methods to convert between model-instance and provided-instance
    // ----------------------
    //for now, use the internal methods created here
    private static ModelService newModelService(String serviceName) {
        // in current sd-service implementation, the 'id' equals 'serviceName'
        // so we keep the pattern,the create/modify time for service are redundancies,
        // in instance level will use 0L 1970-Jun-01 as created time. (should not depends on it)
        return new ModelService(serviceName, serviceName, new Date(0L));

    }

    /**
     * In history record, need a copy for the that-time state of the instance
     */
    private static ModelServiceInstance copyModelInstFrom(ModelServiceInstance original) {
        ModelServiceInstance copied = new ModelServiceInstance();
        copied.setServiceName(original.getServiceName());
        copied.setAddress(original.getAddress());
        copied.setPort(original.getPort());
        copied.setStatus(original.getStatus());
        copied.setUri(original.getUri());
        copied.setId(original.getId());
        copied.setInstanceId(original.getInstanceId());
        copied.setMonitorEnabled(original.isMonitorEnabled());
        copied.setCreateTime(original.getCreateTime());
        copied.setModifiedTime(original.getModifiedTime());
        copied.setHeartbeatTime(original.getHeartbeatTime());
        copied.setMetadata(original.getMetadata());
        return copied;
    }

    private static ModelServiceInstance newModelInstFromProvidedInst(ProvidedServiceInstance instance) {

        ModelServiceInstance mInstance = new ModelServiceInstance();

        mInstance.setServiceName(instance.getServiceName());
        mInstance.setAddress(instance.getAddress());
        mInstance.setPort(instance.getPort());
        mInstance.setStatus(instance.getStatus());
        mInstance.setUri(instance.getUri());

        mInstance.setId(instance.getAddress());
        // use address as instanceId
        mInstance.setInstanceId(instance.getAddress());

        // use the monitor enabled by default
        mInstance.setMonitorEnabled(true);

        // Although problems in the model,
        // 1.) the xxxTime need not to be declared as Date, use long as milli-secs is enough
        // 2.) create-time should never be changed, good to be final
        // To maintain the compatible in JSON level. we need to keep using Date in 1.x until 2.0 when old protocol
        // compatibility is not a requirement. or we can re-define the model in 2.0
        final long now = System.currentTimeMillis();
        mInstance.setCreateTime(new Date(now));
        mInstance.setModifiedTime(new Date(now));

        //now use '1970-Jan-01' as initial value.
        mInstance.setHeartbeatTime(new Date(0L));

        // metadata
        mInstance.setMetadata(instance.getMetadata());

        return mInstance;
    }

    private ProvidedServiceInstance toProvidedInstance(ModelServiceInstance mInstance) {
        return new ProvidedServiceInstance(mInstance.getServiceName(), mInstance.getAddress(),
                mInstance.getPort(), mInstance.getUri(), mInstance.getStatus(),
                mInstance.getMetadata());
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
        ModelServiceInstance newInstance = newModelInstFromProvidedInst(instance);
        ModelServiceInstance previous = iMap.putIfAbsent(instance.getAddress(), newInstance);
        if (previous != null) {
            LOGGER.debug("ModelServiceInstance id {} already registered by {} ", instance.getAddress(), objHashStr(previous));
        } else {
            addToHistory(new InstanceChange<>(newInstance.getModifiedTime().getTime(),
                    newInstance.getServiceName(),
                    InstanceChange.ChangeType.Create,
                    null,
                    copyModelInstFrom(newInstance)
            ));
            LOGGER.debug("Registered new ModelServiceInstance {} with id {} ", objHashStr(newInstance), instance.getAddress());
        }

    }

    @Deprecated
    @Override
    public void updateInstance(ProvidedServiceInstance instance) {
        String serviceName = instance.getServiceName();
        Map<String, ModelServiceInstance> iMap = inMemoryRegistry.get(serviceName);
        if (iMap == null) {
            LOGGER.debug("Service {} not exist", serviceName);
        } else {
            ModelServiceInstance mInstance = iMap.get(instance.getAddress());
            if (mInstance == null) {
                LOGGER.debug("ModelServiceInstance is not found by {}", instance.getAddress());
            } else {
                mInstance.setServiceName(instance.getServiceName());
                mInstance.setAddress(instance.getAddress());
                mInstance.setPort(instance.getPort());
                mInstance.setStatus(instance.getStatus());
                mInstance.setUri(instance.getUri());
                mInstance.setId(instance.getAddress());
                mInstance.setInstanceId(instance.getAddress());
                mInstance.setModifiedTime(new Date());
                mInstance.setMetadata(instance.getMetadata());
                LOGGER.debug("ModelServiceInstance {} is update by {}", objHashStr(mInstance), objHashStr(instance));
            }
        }
    }

    private ModelServiceInstance getInstance(String serviceName, String instanceId) {
        Map<String, ModelServiceInstance> iMap = inMemoryRegistry.get(serviceName);
        if (iMap != null) {
            return iMap.get(instanceId);
        }
        return null;
    }

    @Override
    public void updateInstanceStatus(String serviceName, String instanceAddress, OperationalStatus status, boolean isOwned) {
        if (status == null) {
            throw new NullPointerException("status is null");
        }
        ModelServiceInstance mInstance = getInstance(serviceName, instanceAddress);
        if (mInstance != null) {
            if (!isOwned) {
                //IN this implementation, update don't care of if the service instance is owned by user
                LOGGER.warn("do updateInstanceStatus even when isOwned is false");
            }
            final ModelServiceInstance old = copyModelInstFrom(mInstance);
            mInstance.setStatus(status);
            mInstance.setModifiedTime(new Date());
            addToHistory(new InstanceChange<>(mInstance.getModifiedTime().getTime(),
                    mInstance.getServiceName(),
                    InstanceChange.ChangeType.Status,
                    old,
                    copyModelInstFrom(mInstance)
            ));
        } else {
            LOGGER.warn("no service instance exist for {} {}", serviceName, instanceAddress);
        }
    }

    @Override
    public void updateInstanceUri(String serviceName, String instanceAddress, String uri, boolean isOwned) {
        ModelServiceInstance mInstance = getInstance(serviceName, instanceAddress);
        if (mInstance != null) {
            if (!isOwned) {
                //IN this implementation, we allow update don't care of if the service instance is owned by user
                LOGGER.debug("do updateInstanceUri even when isOwned is false");
            }
            final ModelServiceInstance old = copyModelInstFrom(mInstance);

            mInstance.setUri(uri);
            mInstance.setModifiedTime(new Date());

            addToHistory(new InstanceChange<>(mInstance.getModifiedTime().getTime(),
                    mInstance.getServiceName(),
                    InstanceChange.ChangeType.URL,
                    old, copyModelInstFrom(mInstance)));
        } else {
            LOGGER.debug("no service instance exist for {} {}", serviceName, instanceAddress);
        }
    }

    @Override
    public void updateInstanceMetadata(String serviceName, String instanceAddress, Map<String, String> metadata, boolean isOwned) {
        ModelServiceInstance mInstance = getInstance(serviceName, instanceAddress);
        if (mInstance != null) {
            if (!isOwned) {
                //IN this implementation, we allow update don't care of if the service instance is owned by user
                LOGGER.debug("do updateInstanceMetadata even when isOwned is false");
            }
            final ModelServiceInstance old = copyModelInstFrom(mInstance);

            mInstance.setMetadata(metadata);
            mInstance.setModifiedTime(new Date());

            addToHistory(new InstanceChange<>(mInstance.getModifiedTime().getTime(),
                    mInstance.getServiceName(),
                    InstanceChange.ChangeType.URL,
                    old, copyModelInstFrom(mInstance)));
        } else {
            LOGGER.debug("no service instance exist for {} {}", serviceName, instanceAddress);
        }
    }
    
    @Override
    public void unregisterInstance(String serviceName, String instanceAddress, boolean isOwned) {
        ConcurrentMap<String, ModelServiceInstance> iMap = inMemoryRegistry.get(serviceName);
        if (iMap == null) {
            LOGGER.warn("Service {} not exist", serviceName);
        } else {
            ModelServiceInstance previousInstance = iMap.remove(instanceAddress);
            if (previousInstance != null) {
                LOGGER.debug("service instance {} removed by name : {} , address : {}", previousInstance, serviceName, instanceAddress);
                addToHistory(new InstanceChange<>(previousInstance.getModifiedTime().getTime(),
                        previousInstance.getServiceName(),
                        InstanceChange.ChangeType.Remove,
                        copyModelInstFrom(previousInstance), null));
            } else {
                LOGGER.debug("no service instance exist for {} {}", serviceName, instanceAddress);
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
            String providedAddress = heartbeat.getProviderAddress();
            ModelServiceInstance instance = getInstance(serviceName, providedAddress);
            if (instance != null) {
                final long now = System.currentTimeMillis();
                instance.setHeartbeatTime(new Date(now));
                instance.setModifiedTime(new Date(now));
                result.put(id, new OperationResult<String>(true, null, null));
                LOGGER.debug("heart beat send ok for {}", instance);
            } else {
                LOGGER.debug("no service instance exist for {} {}", serviceName, providedAddress);
                result.put(id, new OperationResult<String>(false, null, new ServiceDirectoryError(ErrorCode.SERVICE_INSTANCE_NOT_EXIST, id)));
            }
        }
        return result;
    }

    @Override
    public ModelService lookupService(String serviceName) {
        Map<String, ModelServiceInstance> iMap = inMemoryRegistry.get(serviceName);
        if (iMap != null) {
            ModelService service = newModelService(serviceName);
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
        }
        LOGGER.debug("getAllInstances() {}", instanceList);
        return Collections.unmodifiableList(instanceList);

    }

    @Override
    @Deprecated /* use lookUpChangedServiceInstancesSince() instead */
    public Map<String, OperationResult<ModelService>> getChangedServices(Map<String, ModelService> services) {
       throw new UnsupportedOperationException("not support the 1.1 method in DirectoryServiceInMemoryClient");
    }


    @Override
    public ModelMetadataKey getMetadataKey(String keyName) {
        throw new UnsupportedOperationException("get metadata is not supported now");
    }

    @Override
    public Map<String, OperationResult<ModelMetadataKey>> getChangedMetadataKeys(Map<String, ModelMetadataKey> keys) {
        throw new UnsupportedOperationException("get metadata change is not supported now");
    }

    //-------------------------------
    // 1.2 API
    //-------------------------------

    public long getLastChangedTimeMills(String serviceName) {
        ModelService service = lookupService(serviceName);
        return service == null ? -1L : lookupService(serviceName).getModifiedTime().getTime();
    }

    public List<ServiceInstance> lookUpChangedServiceInstancesSince(String serviceName, long since) {
        List<ServiceInstance> changed = new ArrayList<>();
        // the latest in model
        ModelService latest = lookupService(serviceName);
        if (since < latest.getModifiedTime().getTime()) {
            // has changes , so where is the changes?
            for (ModelServiceInstance instance : latest.getServiceInstances()) {
                if (since < instance.getModifiedTime().getTime()) {
                    changed.add(ServiceInstanceUtils.toServiceInstance(instance));
                }
            }
        }
        return changed;
    }

    @Override
    public List<InstanceChange<ModelServiceInstance>> lookupChangesSince(String serviceName, long since) {

        InstanceChange<ModelServiceInstance>[] all = changeHistory.toArray((InstanceChange<ModelServiceInstance>[]) new InstanceChange[changeHistory.size()]);
        int index = -1;
        for (int i = 0; i < all.length; i++) {
            if (all[i].changedTimeMills > since) {
                index = i;
                LOGGER.debug("found changes {} at {} since {}", all[i].changedTimeMills, index, since);
                break;
            }
        }
        if (index >= 0) {
            final int newLength = all.length - index;
            List<InstanceChange<ModelServiceInstance>> result = new ArrayList<>(newLength);
            for (int i = index; i < all.length; i++) {
                if (all[i] == null) break; //no more items. in the case
                //check if the instance is the service looked up for
                if (serviceName.equals(all[i].serviceName)) {
                    LOGGER.debug("build change {} to result list", all[i]);
                    result.add(new InstanceChange<ModelServiceInstance>(
                            all[i].changedTimeMills,
                            all[i].serviceName,
                            all[i].changeType,
                            all[i].from == null ? null :all[i].from,
                            all[i].to == null ? null : all[i].to));
                }
            }
            return result;
        }
        return Collections.emptyList();
    }
}
