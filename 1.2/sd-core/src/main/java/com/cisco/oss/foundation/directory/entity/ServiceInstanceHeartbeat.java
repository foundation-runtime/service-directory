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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The heartbeat info of the ServiceInstance.
 *
 * It is the heartbeat info of the ServiceInstance, API sends a list of the
 * ServiceInstanceHeartbeat to ServiceDirectory server to update the heartbeat
 * of the ServiceInstance.
 *
 */

@JsonIgnoreProperties(ignoreUnknown = true)
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
     * The providerId (for backward compatible)
     */
    @Deprecated
    @JsonProperty("providerId")
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
     * @param providerAddress
     *            the provider address.
     */
    @JsonCreator
    public ServiceInstanceHeartbeat(@JsonProperty("serviceName")String serviceName, @JsonProperty("providerAddress")String providerAddress) {
        this.serviceName = serviceName;
        this.providerAddress = providerAddress;
        this.providerId = providerAddress;
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
        if (providerAddress!=null) {
            Matcher matcher = HOST_PORT_PATTERN.matcher(providerAddress);
            if (matcher.matches() && isValidPort(matcher.group(2))) {
                return matcher.group(1);
            }
        }else if(providerId!=null){
            Matcher matcher = HOST_PORT_PATTERN.matcher(providerId);
            if (matcher.matches() && isValidPort(matcher.group(2))) {
                return matcher.group(1);
            }
        }
        return providerAddress;
    }

    @JsonIgnore
    private static final Pattern HOST_PORT_PATTERN = Pattern.compile("^(.*)?-(\\d*)?$");

    @JsonIgnore
    private static boolean isValidPort(String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            return port >= 0 && port <= 65535;
        }catch (NumberFormatException e){
            return false;
        }
    }

    /**
     * Get the providerId
     * @deprecated use {@link #getProviderAddress()}
     */
    @Deprecated
    public String getProviderId(){
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

}
