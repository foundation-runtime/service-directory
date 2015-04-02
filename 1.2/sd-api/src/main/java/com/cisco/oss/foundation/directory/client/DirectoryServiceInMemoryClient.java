package com.cisco.oss.foundation.directory.client;

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

/**
 * It's client works as a in-memory server. so that sd-api can work as-like there is a real sd-server.
 * It's useful for unit/integration in some case. The different between {@code DirectoryServiceDummyClient} is
 * The dummy one actually do nothing. but in-memory do something really
 *
 * @since 1.2
 * @see DirectoryServiceDummyClient
 */
public class DirectoryServiceInMemoryClient implements DirectoryServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryServiceInMemoryClient.class);
    /**
     * in-memory registry store
     * K1 -> Service Name
     * K2 -> ProviderId
     */
    ConcurrentHashMap<String, Map<String,ProvidedServiceInstance>> inMemoryRegistry = new
            ConcurrentHashMap<>();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock read = readWriteLock.readLock();
    private final Lock write = readWriteLock.writeLock();

    @Override
    public void registerInstance(ProvidedServiceInstance instance) {
        try{
            read.lock();
            String serviceName = instance.getServiceName();
            inMemoryRegistry.putIfAbsent(serviceName, new HashMap<String, ProvidedServiceInstance>());
            Map<String, ProvidedServiceInstance> iMap = inMemoryRegistry.get(serviceName);
            if (iMap.containsKey(instance.getProviderId())){
                //already exist
                LOGGER.warn("ProvidedServiceInstance {} already exist",instance);
            }else{
                iMap.put(instance.getProviderId(), instance);
            }
        }finally {
            read.unlock();
        }

    }

    @Override
    public void updateInstance(ProvidedServiceInstance instance) {
        try {
            read.lock();
            String serviceName = instance.getServiceName();
            Map<String, ProvidedServiceInstance> iMap = inMemoryRegistry.get(serviceName);
            if (iMap == null) {
                LOGGER.warn("Service {} not exist", serviceName);
            } else {
                if (!iMap.containsKey(instance.getProviderId())) {
                    LOGGER.warn("ProvidedServiceInstance {} not found", instance);
                } else {
                    iMap.put(instance.getProviderId(), instance);
                }

            }
        } finally {
            read.unlock();
        }
    }

    private ProvidedServiceInstance _getInstance(String serviceName,String instanceId){
        Map<String, ProvidedServiceInstance> iMap = inMemoryRegistry.get(serviceName);
        if (iMap!=null){
            return iMap.get(instanceId);
        }
        return null;
    }

    @Override
    public void updateInstanceStatus(String serviceName, String instanceId, OperationalStatus status, boolean isOwned) {
        try {
            read.lock();
            ProvidedServiceInstance instance = _getInstance(serviceName, instanceId);
            if (instance!=null) {
                instance.setStatus(status);
                if (!isOwned) {
                    //IN this imple, we allow update don't care of if the service instance is owned by user
                    LOGGER.warn("do updateInstanceStatus even when isOwned is false");
                }
                updateInstance(instance);
            }else{
                LOGGER.warn("no service instance exist for {} {}",serviceName,instanceId);
            }
        } finally {
            read.unlock();
        }

    }

    @Override
    public void updateInstanceUri(String serviceName, String instanceId, String uri, boolean isOwned) {
        try{
            read.lock();
            ProvidedServiceInstance instance = _getInstance(serviceName, instanceId);
            if (instance!=null) {
                instance.setUri(uri);
                if (!isOwned) {
                    //IN this imple, we allow update don't care of if the service instance is owned by user
                    LOGGER.warn("do updateInstanceUri even when isOwned is false");
                }
                updateInstance(instance);
            }else{
                LOGGER.warn("no service instance exist for {} {}",serviceName,instanceId);
            }
        }finally {
            read.unlock();
        }

    }

    @Override
    public void unregisterInstance(String serviceName, String instanceId, boolean isOwned) {
        try{
            read.lock();
            Map<String, ProvidedServiceInstance> iMap = inMemoryRegistry.get(serviceName);
            if (iMap==null){
                LOGGER.warn("Service {} not exist", serviceName);
            }else{
                if(iMap.containsKey(instanceId)){
                    iMap.remove(instanceId);
                }else{
                    LOGGER.warn("no service instance exist for {} {}",serviceName,instanceId);
                }
            }

        }finally {
            read.unlock();
        }

    }

    @Override
    public Map<String, OperationResult<String>> sendHeartBeat(Map<String, ServiceInstanceHeartbeat> heartbeatMap) {
        return null;
    }

    @Override
    public ModelService lookupService(String serviceName) {
        return null;
    }

    @Override
    public List<ModelServiceInstance> getAllInstances() {
        return null;
    }

    @Override
    public ModelMetadataKey getMetadataKey(String keyName) {
        return null;
    }

    @Override
    public Map<String, OperationResult<ModelService>> getChangedServices(Map<String, ModelService> services) {
        return null;
    }

    @Override
    public Map<String, OperationResult<ModelMetadataKey>> getChangedMetadataKeys(Map<String, ModelMetadataKey> keys) {
        return null;
    }

    @Override
    public void setInvoker(DirectoryInvoker invoker) {
        //DO nothing.
    }
}
