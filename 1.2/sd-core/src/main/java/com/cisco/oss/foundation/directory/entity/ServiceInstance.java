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
 */
public class ServiceInstance {

    /**
     * ServiceInstance id, unique in Service.
     * @deprecated for backward compatible only, use {@link #getAddress()} for ubiquity
     */
    private final String instanceId;


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
     * The real port of the instance.
     */
    private final int port;

    /**
     * ServiceInstance metadata
     */
    private final Map<String, String> metadata;

    /**
     * Constructor.
     *
     * @param serviceName
     *            the service name.
     * @param instanceId
     *            the instance id.
     * @param uri
     *            the uri.
     * @param address
     *            The real address of the instance, it can be real IP or host
     *            name.
     * @param port
     *            The real port of the instance.
     * @param metadata
     *            the metadata Map.
     *
     * @deprecated only for backward compatible.
     */
    @Deprecated
    public ServiceInstance(String serviceName, String instanceId, String uri,
            boolean monitor, OperationalStatus status, String address,
            int port, Map<String, String> metadata) {
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.uri = uri;
        this.monitorEnabled = monitor;
        this.status = status;
        this.address = address;
        this.port = port;
        this.metadata = new HashMap<>();
        if (metadata != null && metadata.size() != 0) {
            this.metadata.putAll(metadata);
        }

    }


 
    
    /**
     * Constructor.
     *
     * @param serviceName
     *            the service name.
     * @param uri
     *            the uri.
     * @param address
     *            The real address of the instance, it can be real IP or host
     *            name.
     * @param port
     *            The real port of the instance.
     * @param metadata
     *            the metadata Map.
     */
    public ServiceInstance(String serviceName, String uri,
            boolean monitor, OperationalStatus status, String address,
            int port, Map<String, String> metadata) {
        this(serviceName,"",uri,monitor,status,address,port,metadata);
    }

    /**
     * Get the instance id.
     *
     * @return the instance id.
     * @deprecated only for backward compatible
     */
    @Deprecated
    public String getInstanceId() {
        return this.instanceId;
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
     * Get the port.
     *
     * @return the port.
     */
    public int getPort() {
        return port;
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "{serviceName:" + serviceName + ", serviceAddress:" + address + "}";
    }

}
