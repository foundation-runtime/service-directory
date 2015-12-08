/**
 * Copyright 2015 Cisco Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cisco.oss.foundation.directory.client;

import java.util.List;
import java.util.Map;

import com.cisco.oss.foundation.directory.entity.InstanceChange;
import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationResult;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstanceHeartbeat;
import com.cisco.oss.foundation.directory.exception.ServiceException;

/**
 * The interface is used for hiding the complexity of how client makes HTTP requests to the directory server.
 *
 * @since 1.2
 */
public interface DirectoryServiceClient {
    
    /**
     * Register a ServiceInstance.
     *
     * @param instance
     *         the ProvidedServiceInstance.
     * @throws ServiceException
     */
    void registerInstance(ProvidedServiceInstance instance);


    /**
     * Update a ServiceInstance.
     *
     * @param instance
     *         the ProvidedServiceInstance.
     * @deprecated  As of release 1.2, replaced by {@link #updateInstanceMetadata(String, String, Map, boolean)}
     */
    @Deprecated
    void updateInstance(ProvidedServiceInstance instance);

    /**
     * Update the ServiceInstance OperationalStatus by serviceName and instanceAddress.
     *
     * @param serviceName
     *         the service name.
     * @param instanceAddress
     *         The IP address or FQDN that the instance is running on.
     * @param status
     *         the ServiceInstance OperationalStatus.
     * @param isOwned
     *         whether the DirectoryAPI owns this ServiceProvider.
     */
    void updateInstanceStatus(String serviceName, String instanceAddress, OperationalStatus status, boolean isOwned);

    /**
     * Update the ServiceInstance attribute "uri".
     *
     * @param serviceName
     *         the service name.
     * @param instanceAddress
     *         The IP address or FQDN that the instance is running on.
     * @param uri
     *         the ServiceInstance URI.
     * @param isOwned
     *         whether the DirectoryAPI owns this ServiceProvider.
     */
    void updateInstanceUri(String serviceName, String instanceAddress, String uri, boolean isOwned);

    /**
     * Update the ServiceInstance metadata.
     *
     * @param serviceName
     *         the service name.
     * @param instanceAddress
     *         The IP address or FQDN that the instance is running on.
     * @param metadata
     *         the ServiceInstance metadata.
     * @param isOwned
     *         whether the DirectoryAPI owns this ServiceProvider.
     */
    void updateInstanceMetadata(String serviceName, String instanceAddress, Map<String, String> metadata, boolean isOwned);

    /**
     * Unregister a ServiceInstance.
     *
     * @param serviceName
     *         service name.
     * @param instanceAddress
     *         The IP address or FQDN that the instance is running on.
     * @param isOwned
     *         whether the DirectoryAPI owns this ServiceProvider.
     */
    void unregisterInstance(String serviceName, String instanceAddress, boolean isOwned);

    /**
     * Send ServiceInstance heartbeats.
     *
     * @param heartbeatMap
     *         the ServiceInstances heartbeat Map.
     */
    Map<String, OperationResult<String>> sendHeartBeat(Map<String, ServiceInstanceHeartbeat> heartbeatMap);

    /**
     * Lookup a Service by serviceName.
     *
     * @param serviceName
     *         the service name.
     * @return
     *         the ModelService.
     */
    ModelService lookupService(String serviceName);

    /**
     * Get all service instances.
     *
     * @return
     *         the ModelServiceInstance list.
     */
    List<ModelServiceInstance> getAllInstances();

    /**
     * Get the changed services list.
     *
     * @param services
     *         the Service list.
     * @return
     *         the list of Services that have been changed.
     * @throws ServiceException
     * @deprecated  As of release 1.2, replaced by {@link #lookupChangesSince(String, long)}
     */
    @Deprecated
    Map<String, OperationResult<ModelService>> getChangedServices(Map<String, ModelService> services);

    /**
     * Get ModelMetadataKey, which is an object holding a list of service instances that contain 
     * the key name in the service metadata.
     *
     * @param keyName
     *         the key name.
     * @return
     *         the ModelMetadataKey.
     */
    ModelMetadataKey getMetadataKey(String keyName);

    /**
     * Get the changed services list.
     *
     * @param serviceName
     *         the service name
     * @param since
     *         the time in long when the service was last changed
     * @return
     *         the list of Services that have been changed.
     * @throws ServiceException
     */
    List<InstanceChange<ModelServiceInstance>> lookupChangesSince(String serviceName,long since);

}
