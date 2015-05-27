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

/**
 * The interface hide the complexity how client make request to sd server
 * hide the transfer layer such as http protocol or web-socket etc.
 *
 * @since 1.2
 */
public interface DirectoryServiceClient {

    void registerInstance(ProvidedServiceInstance instance);

    @Deprecated /* should not support anymore */
    void updateInstance(ProvidedServiceInstance instance);

    void updateInstanceStatus(String serviceName, String instanceAddress, OperationalStatus status, boolean isOwned);

    void updateInstanceUri(String serviceName, String instanceAddress, String uri, boolean isOwned);
    
    void updateInstanceMetadata(String serviceName, String instanceAddress, Map<String, String> metadata, boolean isOwned);

    void unregisterInstance(String serviceName, String instanceAddress, boolean isOwned);

    Map<String, OperationResult<String>> sendHeartBeat(Map<String, ServiceInstanceHeartbeat> heartbeatMap);

    ModelService lookupService(String serviceName);

    List<ModelServiceInstance> getAllInstances();

    @Deprecated /* replaced by lookupChangesSince */
    Map<String, OperationResult<ModelService>> getChangedServices(Map<String, ModelService> services);

    ModelMetadataKey getMetadataKey(String keyName);

    List<InstanceChange<ModelServiceInstance>> lookupChangesSince(String serviceName,long since);

}
