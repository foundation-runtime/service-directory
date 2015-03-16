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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;

/**
 * Register ServiceInstance Protocol.
 *
 *
 */
public class RegisterServiceInstanceProtocol extends Protocol{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The ProvidedServiceInstance.
     */
    private ProvidedServiceInstance instance;

    /**
     * Indicate whether is HTTP Rest request.
     */
    private boolean restRequest = false;

    /**
     * Indicate wheter is session based request.
     */
    private boolean noSession = false;

    public RegisterServiceInstanceProtocol(){};

    public RegisterServiceInstanceProtocol(ProvidedServiceInstance instance){
        this.instance = instance;
    }

    public RegisterServiceInstanceProtocol(ProvidedServiceInstance instance, boolean restRequest, boolean noSession){
        this.instance = instance;
        this.restRequest = restRequest;
        this.noSession = noSession;
    }

    public boolean isNoSession() {
        return noSession;
    }

    public void setNoSession(boolean noSession) {
        this.noSession = noSession;
    }

    public boolean isRestRequest() {
        return restRequest;
    }

    public void setRestRequest(boolean restRequest) {
        this.restRequest = restRequest;
    }

    public void setProvidedServiceInstance(ProvidedServiceInstance instance){
        this.instance = instance;
    }

    public ProvidedServiceInstance getProvidedServiceInstance(){
        return this.instance;
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException{
        ois.defaultReadObject();
        instance = (ProvidedServiceInstance) ois.readObject();
    }

    private void writeObject(ObjectOutputStream oos) throws IOException{
        oos.defaultWriteObject();
        oos.writeObject(instance);
    }
}
