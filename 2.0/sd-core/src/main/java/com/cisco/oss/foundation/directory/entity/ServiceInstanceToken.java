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
package com.cisco.oss.foundation.directory.entity;

/**
 * The ServiceInstanceToken identity of the ServiceInstance.
 *
 * @author zuxiang
 *
 */
public class ServiceInstanceToken {

    /**
     * The ServiceName.
     */
    private String serviceName;

    /**
     * The instanceId.
     */
    private String instanceId;

    /**
     * Constructor.
     */
    public ServiceInstanceToken(){

    }

    /**
     * Constructor.
     *
     * @param serviceName
     *         the serviceName.
     * @param instanceId
     *         the instanceId
     */
    public ServiceInstanceToken(String serviceName, String instanceId){
        this.serviceName = serviceName;
        this.instanceId = instanceId;
    }

    /**
     * Get the service name.
     *
     * @return
     *         the service name.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Set the service name.
     *
     * @param serviceName
     *         the service name.
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Get the instance id.
     * @return
     *         the instance id.
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Set the instance id.
     *
     * @param instanceId
     *         the instance id.
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public boolean equals(Object object){
        if(object != null && object instanceof ServiceInstanceToken){
            if(object == this){
                return true;
            }

            ServiceInstanceToken target = (ServiceInstanceToken)object;
            return (serviceName.equals(target.serviceName) && instanceId.equals(target.instanceId));
        }
        return false;
    }

    @Override
    public int hashCode(){
        int i = 37;
        i = 31 * i + serviceName == null ? 0 : serviceName.hashCode();
        i = 31 * i + instanceId == null ? 0 : instanceId.hashCode();
        return i;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("{");
        sb.append("serviceName=").append(serviceName).append(",instanceId=").append(instanceId).append("}");
        return sb.toString();
    }


}
