/**
 * Copyright 2014 Cisco Systems, Inc.
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
package com.cisco.oss.foundation.directory.proto;

/**
 * Update ServiceInstance URI Protocol.
 *
 * @author zuxiang
 *
 */
public class UpdateServiceInstanceUriProtocol extends Protocol{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * service name.
     */
    private String serviceName;

    /**
     * the instance id.
     */
    private String instanceId;

    /**
     * the ServiceInstance URI.
     */
    private String uri;

    /**
     * is owned the ServiceInstance.
     */
    private boolean isOwned = false;

    /**
     * is rest request.
     */
    private boolean restRequest = false;

    /**
     * is session based request.
     */
    private boolean noSession = false;

    public UpdateServiceInstanceUriProtocol(){}

    public UpdateServiceInstanceUriProtocol(String serviceName, String instanceId, String uri){
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.uri = uri;
    }

    public UpdateServiceInstanceUriProtocol(String serviceName, String instanceId, String uri, boolean restRequest, boolean isOwned, boolean noSession){
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.uri = uri;
        this.restRequest = restRequest;
        this.isOwned = isOwned;
        this.noSession = noSession;
    }

    public boolean isNoSession() {
        return noSession;
    }

    public void setNoSession(boolean noSession) {
        this.noSession = noSession;
    }

    public boolean isOwned() {
        return isOwned;
    }

    public void setOwned(boolean isOwned) {
        this.isOwned = isOwned;
    }

    public boolean isRestRequest() {
        return restRequest;
    }

    public void setRestRequest(boolean restRequest) {
        this.restRequest = restRequest;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }



}
