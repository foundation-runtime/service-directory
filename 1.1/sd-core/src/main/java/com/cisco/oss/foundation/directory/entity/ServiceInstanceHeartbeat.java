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
 * The heartbeat info of the ServiceInstance.
 *
 * It is the heartbeat info of the ServiceInstance, API sends a list of the
 * ServiceInstanceHeartbeat to ServiceDirectory server to update the heartbeat
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
