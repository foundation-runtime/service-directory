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
     * Constructor.
     *
     * @param serviceName
     *            the service name.
     * @param uri
     *            the uri.
     * @param address
     *            The real address of the instance, it can be real IP or host
     *            name.
     * @param metadata
     *            the metadata Map.
     */
    public ServiceInstance(String serviceName, String uri,
            boolean monitor, OperationalStatus status, String address,
            Map<String, String> metadata) {
        this.serviceName = serviceName;
        this.uri = uri;
        this.monitorEnabled = monitor;
        this.status = status;
        this.address = address;
        this.metadata = new HashMap<>();
        if (metadata != null && metadata.size() != 0) {
            this.metadata.putAll(metadata);
        }
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "{serviceName:" + serviceName + ", serviceAddress:" + address + "}";
    }

}
