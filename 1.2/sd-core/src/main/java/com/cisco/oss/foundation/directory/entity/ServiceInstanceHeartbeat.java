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
 *
 */
public class ServiceInstanceHeartbeat {
    /**
     * The service name of the Instance.
     */
    private String serviceName;

    /**
     * The provider Address.
     */
    private String providerAddress;

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
    public ServiceInstanceHeartbeat(String serviceName, String providerAddress) {
        this.serviceName = serviceName;
        this.providerAddress = providerAddress;
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
     * Get the providerAddress.
     *
     * @return the providerAddress.
     */
    public String getProviderAddress() {
        return providerAddress;
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
     * Set the providerAddress.
     *
     * @param providerAddress
     *            the providerAddress.
     */
    public void setProviderAddress(String providerAddress) {
        this.providerAddress = providerAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "serviceName=" + serviceName + ", providerAddress=" + providerAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof ServiceInstanceHeartbeat) {
            ServiceInstanceHeartbeat hb = (ServiceInstanceHeartbeat) obj;
            return (serviceName.equals(hb.getServiceName()) && providerAddress
                    .equals(hb.getProviderAddress()));
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = providerAddress != null ? providerAddress.hashCode() : 0;
        result = 31 * result + serviceName != null ? serviceName.hashCode() : 0;
        return result;
    }
}
