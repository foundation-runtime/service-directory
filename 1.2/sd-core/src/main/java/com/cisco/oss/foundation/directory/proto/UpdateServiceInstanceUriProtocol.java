/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
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
