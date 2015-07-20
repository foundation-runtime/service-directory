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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable ServiceInstance provided to the Service Consumer.
 *
 * @since 1.2 the old instanceId and port has been removed
 *
 * also @see {@link com.cisco.oss.foundation.directory.entity.compatible.ServiceInstance11}
 */
public class ServiceInstance {

    /**
     * the Service Name of the ServiceInstance
     */
    private final String serviceName;

    /**
     * the complete URL of the ServiceInstance
     */
    private final String uri;

    /**
     * The instance OperationalStatus
     */
    private final OperationalStatus status;

    /**
     * Whether the instance enabled Monitor in Service Directory.
     */
    private final boolean monitorEnabled;

    /**
     * The real address of the instance, it can be real IP or host name, unique for a service.
     */
    private final String address;

    /**
     * ServiceInstance metadata
     */
    private final Map<String, String> metadata;

    /**
     * The real port of the instance. Optional.
     */
    private int port = 0;

    /**
     *  The TLS port of the instance. Optional.
     */
    private int tls_port = 0;
    
    /**
     * The protocol. Optional.
     */
    private String protocol;


/**
 * 
 * Constructor
 * 
 * @param serviceName
 *            the service name
 * @param uri
 *            the service url
 * @param monitor
 *            boolean to indicate whether service is monitable
 * @param status
 *            the service operational status
 * @param address
 *            the address of the service, it can be an IP address or hostname
 * @param metadata
 *            optional. the metadata for the service.
 * @param port
 *            optional. the port on which the service runs.
 * @param tls_port
 *            optional. the TLS port on which the service runs.
 * @param protocol
 *            optional. the transport protocol 
 */
    public ServiceInstance(String serviceName, String uri,
            boolean monitor, OperationalStatus status, String address,
            Map<String, String> metadata, int port, int tls_port, String protocol) {
        this.serviceName = serviceName;
        this.uri = uri;
        this.monitorEnabled = monitor;
        this.status = status;
        this.address = address;
        this.metadata = new HashMap<>();
        if (metadata != null && metadata.size() != 0) {
            this.metadata.putAll(metadata);
        }
        this.port = port;
        this.tls_port = tls_port;
        this.protocol = protocol;
        
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
     * Get the URI.
     *
     * @return the URI.
     */
    public String getUri() {
        return uri;
    }

    /**
     * check if service is monitored in Service Directory.
     *
     * @return true if monitor enabled.
     */
    public boolean isMonitorEnabled() {
        return monitorEnabled;
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
     * Get the real address, it can be real IP or host name.
     *
     * @return the real address.
     */
    public String getAddress() {
        return address;
    }


    /**
     * Get the metadata Map.
     *
     * @return the metadata Map.
     */
    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(this.metadata);
    }

    /**
     * Get the port
     * 
     * @return port
     *           the port on which instance runs
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the TLS port
     * 
     * @return tls_port
     *           the TLS port on which instance runs
     */
    public int getTls_port() {
        return tls_port;
    }

    /**
     * Get the protocol e.g. http
     * @return protocol
     *             the transport protocol
     */
    public String getProtocol() {
        return protocol;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "{serviceName:" + serviceName + ", serviceAddress:" + address + "}";
    }

}
