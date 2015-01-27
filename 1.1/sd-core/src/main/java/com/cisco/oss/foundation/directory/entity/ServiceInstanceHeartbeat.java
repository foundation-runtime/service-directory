/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.entity;

/**
 * The heartbeat info of the ServiceInstance.
 *
 * It is the heartbeat info fo the ServiceInstance, sd-api send a list of the
 * ServiceInstanceHeartbeat to Service Directory server to update the heartbeat
 * of the ServiceInstance.
 *
 * @author zuxiang
 *
 */
public class ServiceInstanceHeartbeat {
    /**
     * The service name of the Instance.
     */
    private String serviceName;

    /**
     * The providerId id.
     */
    private String providerId;

    /**
     * Constructor.
     */
    public ServiceInstanceHeartbeat() {

    }

    /**
     * Constructor.
     *
     * @param serviceName
     *            the service name.
     * @param providerId
     *            the provider id.
     */
    public ServiceInstanceHeartbeat(String serviceName, String providerId) {
        this.serviceName = serviceName;
        this.providerId = providerId;
    }

    /**
     * Get the service name.
     *
     * @return the service name.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Get the providerId.
     *
     * @return the providerId.
     */
    public String getProviderId() {
        return providerId;
    }

    /**
     * Set the service name.
     *
     * @param serviceName
     *            the service name.
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Set the providerId.
     *
     * @param providerId
     *            the providerId.
     */
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "serviceName=" + serviceName + ", providerId=" + providerId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof ServiceInstanceHeartbeat) {
            ServiceInstanceHeartbeat hb = (ServiceInstanceHeartbeat) obj;
            return (serviceName.equals(hb.getServiceName()) && providerId
                    .equals(hb.getProviderId()));
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = providerId != null ? providerId.hashCode() : 0;
        result = 31 * result + serviceName != null ? serviceName.hashCode() : 0;
        return result;
    }
}
