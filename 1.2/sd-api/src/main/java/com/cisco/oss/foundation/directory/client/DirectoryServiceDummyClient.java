package com.cisco.oss.foundation.directory.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

/**
 * The Client is useful for unit-testing propose. The calling of the API will be logged
 */
public class DirectoryServiceDummyClient implements DirectoryServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryServiceDummyClient.class);

    @Override
    public void registerInstance(ProvidedServiceInstance instance) {
        LOGGER.info("registerInstance {}",instance);
    }

    @Override
    public void updateInstance(ProvidedServiceInstance instance) {
        LOGGER.info("updateInstance {}",instance);
    }

    @Override
    public void updateInstanceStatus(String serviceName, String instanceId, OperationalStatus status, boolean isOwned) {
        LOGGER.info("updateInstanceStatus {},{},{},{}",serviceName,instanceId,status,isOwned);
    }

    @Override
    public void updateInstanceUri(String serviceName, String instanceId, String uri, boolean isOwned) {
        LOGGER.info("updateInstanceUri {},{},{},{}",serviceName,instanceId,uri,isOwned);
    }

    @Override
    public void unregisterInstance(String serviceName, String instanceId, boolean isOwned) {
        LOGGER.info("unregisterInstance {},{},{}",serviceName,instanceId,isOwned);
    }

    @Override
    public Map<String, OperationResult<String>> sendHeartBeat(Map<String, ServiceInstanceHeartbeat> heartbeatMap) {
        LOGGER.info("sendHeartBeat {}",heartbeatMap);
        return Collections.emptyMap();
    }

    @Override
    public ModelService lookupService(String serviceName) {
        LOGGER.info("lookupService {}",serviceName);
        return null;
    }

    @Override
    public List<ModelServiceInstance> getAllInstances() {
        LOGGER.info("getAllInstances");
        return Collections.emptyList();
    }

    @Override
    public ModelMetadataKey getMetadataKey(String keyName) {
        LOGGER.info("getMetadataKey {}",keyName);
        return null;
    }

    @Override
    public Map<String, OperationResult<ModelService>> getChangedServices(Map<String, ModelService> services) {
        LOGGER.info("getChangedServices {}",services);
        return Collections.emptyMap();
    }

    @Override
    public Map<String, OperationResult<ModelMetadataKey>> getChangedMetadataKeys(Map<String, ModelMetadataKey> keys) {
        LOGGER.info("getChangedMetadataKeys {}",keys);
        return Collections.emptyMap();
    }

    @Override
    public void setInvoker(DirectoryInvoker invoker) {
        //do NOTHING for NOW
    }

    @Override
    public long getLastChangedTimeMills(String serviceName) {
        throw new UnsupportedOperationException("not support now for 1.2 api");
    }

    @Override
    public List<ServiceInstance> lookUpChangedServiceInstancesSince(String serviceName, long since) {
        throw new UnsupportedOperationException("not support now for 1.2 api");
    }

    @Override
    public List<InstanceChange<ServiceInstance>> lookupChangesSince(String serviceName, long since) {
        throw new UnsupportedOperationException("not support now for 1.2 api");
    }
}
