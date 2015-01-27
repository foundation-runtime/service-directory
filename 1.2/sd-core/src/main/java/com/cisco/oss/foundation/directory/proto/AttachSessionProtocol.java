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

import java.util.List;

import com.cisco.oss.foundation.directory.entity.ServiceInstanceToken;

/**
 * Attach ServiceInstance to Session Protocol.
 *
 * @author zuxiang
 *
 */
public class AttachSessionProtocol extends Protocol {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The Session id.
     */
    private String sessionId;

    /**
     * The instance list.
     */
    private List<ServiceInstanceToken> serviceInstances;

    public AttachSessionProtocol(){

    }

    public AttachSessionProtocol(List<ServiceInstanceToken> serviceInstances, String sessionId){
        this.serviceInstances = serviceInstances;
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<ServiceInstanceToken> getServiceInstances() {
        return serviceInstances;
    }

    public void setServiceInstances(List<ServiceInstanceToken> serviceInstances) {
        this.serviceInstances = serviceInstances;
    }


}
