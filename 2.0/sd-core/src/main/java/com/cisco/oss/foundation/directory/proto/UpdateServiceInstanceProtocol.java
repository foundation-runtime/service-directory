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

import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;

/**
 * Update ServiceInstance Protocol.
 *
 * @author zuxiang
 *
 */
public class UpdateServiceInstanceProtocol extends Protocol {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The ServiceIntance.
     */
    private ProvidedServiceInstance providedServiceInstance;

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

    public UpdateServiceInstanceProtocol(){

    }

    public UpdateServiceInstanceProtocol(ProvidedServiceInstance providedServiceInstance){
        this.providedServiceInstance = providedServiceInstance;
    }

    public UpdateServiceInstanceProtocol(ProvidedServiceInstance providedServiceInstance, boolean restRequest, boolean isOwned, boolean noSession){
        this.providedServiceInstance = providedServiceInstance;
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

    public ProvidedServiceInstance getProvidedServiceInstance() {
        return providedServiceInstance;
    }

    public void setProvidedServiceInstance(
            ProvidedServiceInstance providedServiceInstance) {
        this.providedServiceInstance = providedServiceInstance;
    }


}
