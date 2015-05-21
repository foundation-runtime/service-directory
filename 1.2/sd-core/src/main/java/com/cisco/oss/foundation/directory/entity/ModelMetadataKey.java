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
     * The MatadataKeyMapper Object id.
     */
    private String id;

    /**
     * The metadata key name.
     */
    private String name;

    /**
     * The last modified time.
     */
    private Date modifiedTime;

    /**
     * The creating time.
     */
    private Date createTime;

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
    public ModelMetadataKey(String name) {
        this.name = name;
    }

    /**
     * Constructor.
     *
     * @param name
     *            the key name.
     * @param id
     *            the id.
     * @param modifiedTime
     *            the last modified time stamp.
     * @param createTime
     *            the create time stamp.
     */
    public ModelMetadataKey(String name, String id, Date modifiedTime,
            Date createTime) {
        this.id = id;
        this.name = name;
        this.modifiedTime = modifiedTime;
        this.createTime = createTime;
    }

    /**
     * Get the id.
     *
     * @return the id.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id.
     *
     * @param id
     *            the id.
     */
    public void setId(String id) {
        this.id = id;
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
     * Get the last modified time stamp.
     *
     * @return the last modifed time stamp.
     */
    public Date getModifiedTime() {
        return modifiedTime;
    }

    /**
     * Set the last modified time stamp.
     *
     * @param modifiedTime
     *            the last modified time stamp.
     */
    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    /**
     * Get the create time stamp.
     *
     * @return the create time stamp.
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * Set the create time stamp.
     *
     * @param createTime
     *            the create time stamp.
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", modifiedTime=" + (modifiedTime == null ? null : modifiedTime.getTime()) +
                ", createTime=" + (createTime == null ? null : createTime.getTime()) +
                ", serviceInstances=" + serviceInstances +
                '}';
    }
}
