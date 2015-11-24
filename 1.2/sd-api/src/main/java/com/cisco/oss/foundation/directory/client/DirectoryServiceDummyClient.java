package com.cisco.oss.foundation.directory.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.entity.InstanceChange;
import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationResult;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
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
    @Deprecated
    public void updateInstance(ProvidedServiceInstance instance) {
        LOGGER.info("updateInstance {}",instance);
    }

    @Override
    public void updateInstanceStatus(String serviceName, String instanceAddress, OperationalStatus status, boolean isOwned) {
        LOGGER.info("updateInstanceStatus {},{},{},{}",serviceName,instanceAddress,status,isOwned);
    }

    @Override
    public void updateInstanceUri(String serviceName, String instanceAddress, String uri, boolean isOwned) {
        LOGGER.info("updateInstanceUri {},{},{},{}",serviceName,instanceAddress,uri,isOwned);
    }

    @Override
    public void updateInstanceMetadata(String serviceName, String instanceAddress, Map<String, String> metadata, boolean isOwned) {
        LOGGER.info("updateInstanceMetadata {},{},{},{}",serviceName,instanceAddress,metadata,isOwned);
    }
    
    @Override
    public void unregisterInstance(String serviceName, String instanceAddress, boolean isOwned) {
        LOGGER.info("unregisterInstance {},{},{}",serviceName,instanceAddress,isOwned);
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
    public List<InstanceChange<ModelServiceInstance>> lookupChangesSince(String serviceName, long since) {
        throw new UnsupportedOperationException("not support now for 1.2 api");
    }

    @Override
    public void registerInstance(ProvidedServiceInstance instance,
            boolean favorMyDC, String myDC) {
        // first get the metadata from instance. 
        LOGGER.info("registerInstance with datacenter affinity {}, my datacenter is {}.",instance, myDC);
        
    }
}
