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

import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationResult;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstanceHeartbeat;

/**
 * The interface hide the complexity how client make request to sd server
 * hide the transfer layer such as http protocol or web-socket etc.
 * @since 1.2 (previously, only http implementation)
 */
public interface DirectoryServiceClient {

    void registerInstance(ProvidedServiceInstance instance);

    @Deprecated /* should not support anymore */
    void updateInstance(ProvidedServiceInstance instance);

    void updateInstanceStatus(String serviceName, String instanceId, OperationalStatus status, boolean isOwned);

    void updateInstanceUri(String serviceName, String instanceId, String uri, boolean isOwned);

    void unregisterInstance(String serviceName, String instanceId, boolean isOwned);

    Map<String, OperationResult<String>> sendHeartBeat(Map<String, ServiceInstanceHeartbeat> heartbeatMap);

    @Deprecated /*why I need to expose ModelService */
    ModelService lookupService(String serviceName);

    @Deprecated /*why I need to expose ModelServiceInstances */
    List<ModelServiceInstance> getAllInstances();

    @Deprecated /*why I need to expose ModelService */
    Map<String, OperationResult<ModelService>> getChangedServices(Map<String, ModelService> services);

    /* TODO metadata refactoring in future */
    ModelMetadataKey getMetadataKey(String keyName);
    Map<String, OperationResult<ModelMetadataKey>> getChangedMetadataKeys(Map<String, ModelMetadataKey> keys);

    /* TODO the invoker is for http only. can be eliminated from the interface */
    public void setInvoker(DirectoryInvoker invoker);

    /**
     * 1.2 API
     */
    //List<ServiceInstance> lookupService2(String serviceName);

    // serviceName -> serviceInstanceList
    // Map<String,List<ServiceInstance>> getAllServices();

    /*
     * Get the timeMills of the latest changed of service
     */
    long getLastChangedTimeMills(String serviceName);

    /**
     * Return a list of modified service instances since the provided timeMillis
     * usually client will provided a last changed time.
     * @param serviceName
     * @param since the time millis in long
     * @return
     */
    List<ServiceInstance> lookUpChangedServiceInstancesSince(String serviceName, long since);

    List<InstanceChange<ServiceInstance>> lookupChangesSince(String serviceName,long since);

    class InstanceChange<T> {
        enum ChangeType{
            Status,
            URL
        }
        public final long changedTimeMills;
        public final ChangeType changeType;
        public final T source;
        public final String from;
        public final String to;
        InstanceChange(long time, T instance, ChangeType type,String from,String to){
            this.changedTimeMills = time;
            this.changeType = type;
            this.source = instance;
            this.from = from;
            this.to = to;
        }
        @Override
        public String toString() {
            return "ServiceInstanceChange{" +
                    "changedTimeMills=" + changedTimeMills +
                    ", changeType=" + changeType +
                    ", source=" + source +
                    ", from='" + from + '\'' +
                    ", to='" + to + '\'' +
                    '}';
        }
    }



}
