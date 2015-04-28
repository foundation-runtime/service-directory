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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * ProvidedServiceInstance used by Service Provider to register/update a service
 * instance.
 *
 * In the ProvidedServiceInstance, the address and port are required attributes
 * for generating ProviderId to identify the multiple instances of the same
 * service.
 *
 * The providerId is defined as "address-port".
 *
 * The address is the real IP address or hostname of the running instance, 
 * the port is the port which the instance binds to.
 *
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProvidedServiceInstance {

    /**
     * The instance service name.
     */
    private String serviceName;

    /**
     * The instance uri.
     */
    private String uri;

    /**
     * The real address of the instance, it can be real IP or host name
     */
    private String address;

    /**
     * The real port of the instance.
     */
    private int port = 0;

    /**
     * The instance OperationalStatus.
     */
    private OperationalStatus status;

    /**
     * Whether the instance is monitored or not in Service Directory.
     */
    private boolean monitorEnabled = true;

    /**
     * The instance metadata info.
     */
    private Map<String, String> metadata;

    /**
     * Constructor.
     */
    public ProvidedServiceInstance() {

    }

    /**
     * Constructor.
     *
     * Replaced by {@link #ProvidedServiceInstance(String, String)}
     *
     * @param serviceName
     *            the service name.
     * @param address
     *            the server address, it can be real IP or host name.
     * @param port
     *            the port.
     */
    @Deprecated
    public ProvidedServiceInstance(String serviceName, String address, int port) {
        this(serviceName, address, port, null, null,null);

    }
    
    /**
     * Constructor.
     *
     * @param serviceName
     *            the service name.
     * @param address
     *            the address that the instance is running on
     */
    public ProvidedServiceInstance(String serviceName, String address) {
        this(serviceName, address, 1, null, null,null);

    }

    /**
     * Constructor.
     *
     * @param serviceName
     *            the service name.
     * @param address
     *            the server address, it can be real IP or host name.
     * @param port
     *            the port.
     * @param uri
     *            the instance uri.
     * @param status
     *            the OperationalStatus.
     * @param metadata
     *            the metadata Map.
     */
    public ProvidedServiceInstance(String serviceName, String address,
            int port, String uri, OperationalStatus status,
            Map<String, String> metadata) {
        this.serviceName = serviceName;
        this.address = address;
        this.port = port;
        this.uri = uri;
        this.status = status;
        this.metadata = metadata;
    }

    /**
     * Get the URI string.
     *
     * @return the URI String.
     */
    public String getUri() {
        return uri;
    }

    /**
     * Set the URI String.
     *
     * @param uri
     *            the URI String.
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Get the real address, it can be real IP or host name.
     *
     * @return the real address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Set the real address, it can be real IP or host name.
     *
     * @param address
     *            the real address.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Get the port.
     *
     * @return the port.
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the port.
     *
     * @param port
     *            the port.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get the OperationalStatus.
     *
     * @return the OperationalStatus.
     */
    public OperationalStatus getStatus() {
        return status;
    }

    /**
     * Set the OperationalStatus.
     *
     * @param status
     *            the OperationalStatus.
     */
    public void setStatus(OperationalStatus status) {
        this.status = status;
    }

    /**
     * check if it is monitored in Service Directory.
     *
     * @return true if monitor enabled.
     */
    public boolean isMonitorEnabled() {
        return monitorEnabled;
    }

    /**
     * Set the service to be monitored or not.
     *
     * @param monitor
     *            true if monitor enabled, false if monitor disabled.
     */
    public void setMonitorEnabled(boolean monitor) {
        this.monitorEnabled = monitor;
    }

    /**
     * Get the metadata Map.
     *
     * @return the metadata Map.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Set the Metadata Map.
     *
     * @param metadata
     *            the metadata Map.
     */
    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
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
     * Get the composed provider id.
     *
     * Provider id is the unique id for service, it is composed of the address and
     * port fields of the ProvidedServiceInstance.
     *
     * The address always be the real ip address or hostname of the machine that 
     * provides the service.
     *
     * @return the provider id.
     */
    @Deprecated
    public String getProviderId() {
        return this.address + "-" + String.valueOf(this.port);
    }

}
