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
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The Service Model Object in Service Directory.
 *
 * It is the logic Service Object in Service Directory. ModelService consists of Service Instances with 
 * same service name.
 *
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelService {

    /**
     * The name.
     */
    private String name;

    /**
     * The id.
     */
    private String id;

    /**
     * The ServiceInstances of the Service.
     */
    private List<ModelServiceInstance> serviceInstances;

    /**
     * The last modified time.
     */
    private Date modifiedTime;

    /**
     * The creating time.
     */
    private Date createTime;

    /**
     * Constructor.
     */
    public ModelService() {

    }

    /**
     * Constructor.
     *
     * @param name
     *            the name.
     * @param id
     *            the instanceId.
     * @param createTime
     *            the create time stamp.
     */
    public ModelService(String name, String id, Date createTime) {
        this.name = name;
        this.id = id;
        this.createTime = createTime;
    }

    /**
     * Get service name.
     *
     * @return the service name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set service name.
     *
     * @param name
     *            the service name.
     */
    public void setName(String name) {
        this.name = name;
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
     * Get the ModelServiceInstance list.
     *
     * @return the ModelServiceInstance list.
     */
    public List<ModelServiceInstance> getServiceInstances() {
        return serviceInstances;
    }

    /**
     * Set the ModelServiceInstance list.
     *
     * @param serviceInstances
     *            the ModelServiceInstance list.
     */
    public void setServiceInstances(List<ModelServiceInstance> serviceInstances) {
        this.serviceInstances = serviceInstances;
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

    @Override
    public String toString() {
        return "ModelService{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", serviceInstances=" + serviceInstances +
                ", modifiedTime=" + (modifiedTime==null? null: modifiedTime.getTime()) +
                ", createTime=" + (createTime==null? null: createTime.getTime()) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelService that = (ModelService) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(id, that.id) &&
                Objects.equals(serviceInstances, that.serviceInstances) &&
                Objects.equals(modifiedTime, that.modifiedTime) &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, serviceInstances, modifiedTime, createTime);
    }
}
