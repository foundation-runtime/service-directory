/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.OperationalStatus;

/**
 * Update ServiceInstance internal Status protocol.
 *
 * @author zuxiang
 *
 */
public class UpdateServiceInstanceInternalStatusProtocol extends Protocol {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Service name.
     */
    private String serviceName;

    /**
     * instanceId.
     */
    private String instanceId;

    /**
     * The OperationalStatus.
     */
    private OperationalStatus status;

    /**
     * is owned the ServiceInstance.
     */
    private boolean isOwned = false;

    /**
     * is the rest request.
     */
    private boolean restRequest = false;

    /**
     * is session based request.
     */
    private boolean noSession = false;

    public UpdateServiceInstanceInternalStatusProtocol(){

    }

    public UpdateServiceInstanceInternalStatusProtocol(String serviceName, String instanceId, OperationalStatus status){
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.status = status;
    }

    public UpdateServiceInstanceInternalStatusProtocol(String serviceName, String instanceId, OperationalStatus status, boolean restRequest, boolean isOwned, boolean noSession){
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.status = status;
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

    public OperationalStatus getStatus() {
        return status;
    }

    public void setStatus(OperationalStatus status) {
        this.status = status;
    }


}
