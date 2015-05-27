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

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Metadata Key name mapping to the ServiceInstance.
 *
 * It maps the metadata key to the ServiceInstance. Use it to get
 * ServiceInstance by metadata.
 *
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelMetadataKey {

    /**
     * The metadata key name.
     */
    private String name;


    /**
     * The list of the ServiceInstance that contains this metadata key.
     */
    private List<ModelServiceInstance> serviceInstances;


    /**
     * Constructor.
     */
    public ModelMetadataKey() {

    }
    /**
     * Constructor.
     *
     * @param name
     *            the key name.
     */
    @JsonCreator
    public ModelMetadataKey(String name) {
        this.name = name;
    }

    /**
     * Get the key name.
     *
     * @return the key name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the key name.
     *
     * @param name
     *            the key name.
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Get the ServiceInstance list.
     *
     * @return the ServiceInstance list.
     */
    public List<ModelServiceInstance> getServiceInstances() {
        return serviceInstances;
    }

    /**
     * Set the ServiceInstance list.
     *
     * @param serviceInstances
     *            the ServiceInstance list.
     */
    public void setServiceInstances(List<ModelServiceInstance> serviceInstances) {
        this.serviceInstances = serviceInstances;
    }

    @Override
    public String toString() {
        return "ModelMetadataKey{" +
                ", name='" + name + '\'' +
                ", serviceInstances=" + serviceInstances +
                '}';
    }
}
