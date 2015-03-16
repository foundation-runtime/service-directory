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

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;

/**
 * The ServiceInstanceOperate.
 *
 *
 */
public class ServiceInstanceOperate {

    /**
     * The service name.
     */
    private String serviceName;

    /**
     * The instanceId.
     */
    private String instanceId;

    /**
     * The ModelServiceInstance.
     */
    private ModelServiceInstance serviceInstance;

    /**
     * The OperateType.
     */
    private OperateType type;

    /**
     * Constructor.
     */
    public ServiceInstanceOperate(){

    }

    /**
     * Constructor.
     *
     * @param serviceName
     *         the service name.
     * @param instanceId
     *         the instanceId.
     * @param serviceInstance
     *         the target ServiceInstance.
     * @param type
     *         the OperateType.
     */
    public ServiceInstanceOperate(String serviceName, String instanceId, ModelServiceInstance serviceInstance, OperateType type){
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.serviceInstance = serviceInstance;
        this.type = type;
    }

    /**
     * Get the ServiceName.
     *
     * @return
     *         the ServiceName.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Set the ServiceName.
     *
     * @param serviceName
     *         the ServiceName.
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Get the instanceId.
     *
     * @return
     *         the instanceId.
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Set the instanceId.
     *
     * @param instanceId
     *         the instanceId.
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Get the target ServiceInstance.
     *
     * @return
     *         the target ServiceInstance.
     */
    public ModelServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    /**
     * Set the target ServiceInstance.
     *
     * @param serviceInstance
     *         the target ServiceInstance.
     */
    public void setServiceInstance(ModelServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    /**
     * Get the OperateType.
     *
     * @return
     *         the OperateType.
     */
    public OperateType getType() {
        return type;
    }

    /**
     * Set the OperateType.
     *
     * @param type
     *         the OperateType.
     */
    public void setType(OperateType type) {
        this.type = type;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("{");
        sb.append("serviceName=").append(serviceName).append(",instanceId=").append(instanceId).append(",type=").append(type);
        sb.append("}");
        return sb.toString();
    }

    /**
     * The ServiceInstance OperateType.
     *
     *
     */
    public static enum OperateType{
        /**
         * Add the ServiceInstance.
         */
        Add(0),

        /**
         * Update the ServiceInstance.
         */
        Update(1),

        /**
         * Delete the ServiceInstance.
         */
        Delete(2),
        ;

        /**
         * The id.
         */
        private final int id;

        /**
         * Constructor.
         *
         * @param id
         *         the Id.
         */
        private OperateType(int id){
            this.id = id;
        }

        /**
         * Get the Id.
         *
         * @return
         *         the Id.
         */
        public int getId(){
            return this.id;
        }
    }
}
