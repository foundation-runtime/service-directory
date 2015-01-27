/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.entity;

import com.cisco.oss.foundation.directory.proto.ServiceInstanceOperate.OperateType;

/**
 * The ServiceInstance modification event.
 *
 * @author zuxiang
 *
 */
public class ServiceInstanceEvent {
    /**
     * The service name.
     */
    private String serviceName;

    /**
     * The instanceId.
     */
    private String instanceId;

    /**
     * The OerateType.
     */
    private OperateType operateType;

    /**
     * Constructor.
     */
    public ServiceInstanceEvent(){

    }

    /**
     * Constructor.
     *
     * @param serviceName
     *         the service name.
     * @param instanceId
     *         the instanceId.
     * @param operateType
     *         the operateType.
     */
    public ServiceInstanceEvent(String serviceName, String instanceId, OperateType operateType){
        this.serviceName = serviceName;
        this.operateType = operateType;
        this.instanceId = instanceId;
    }

    /**
     * Get the serviceName.
     * @return
     *         the serviceName.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Set the serviceName.
     *
     * @param serviceName
     *         the service name.
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Get the instanceId.
     *
     * @return
     *         the instanceId
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Set the instanceId.
     *
     * @param instanceId
     *         the serviceInstance instanceId.
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Get the OperateType.
     *
     * @return
     *         the ServiceInstance OperateType.
     */
    public OperateType getOperateType() {
        return operateType;
    }

    /**
     * Set the ServiceInstance OperateType.
     *
     * @param operateType
     *         the ServiceInstance OperateType.
     */
    public void setOperateType(OperateType operateType) {
        this.operateType = operateType;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("{");
        sb.append("serviceName=").append(serviceName).append(",operateType=").append(operateType).append(",instanceId=").append(instanceId);
        sb.append("}");
        return sb.toString();
    }
}
