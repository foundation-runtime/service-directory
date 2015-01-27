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
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * The Model ServiceInstance object in Service Directory.
 *
 * It is the Model ServiceInstance object that has all attributes.
 *
 * @author zuxiang
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelServiceInstance {

    /**
     * The id.
     */
    private String id;

    /**
     * The instance id.
     */
    private String instanceId;

    /**
     * The instance uri.
     */
    private String uri;

    /**
     * The instance OperationalStatus
     */
    private OperationalStatus status;

    /**
     * Whether the instance enabled Monitor in Service Directory.
     */
    private boolean monitorEnabled = true;

    /**
     * The instance creating time.
     */
    private Date createTime;

    /**
     * The instance last modified time.
     */
    private Date modifiedTime;

    /**
     * The instance last heartbeat time.
     */
    private Date heartbeatTime;

    /**
     * The instance metadata info.
     */
    private Map<String, String> metadata;

    /**
     * The instance service name.
     */
    private String serviceName;

    /**
     * The real address of the instance, it can be real IP or host name
     */
    private String address;

    /**
     * The real port of the instance.
     */
    private int port = 0;

    /**
     * Constructor.
     */
    public ModelServiceInstance() {

    }

    /**
     * Constructor.
     *
     * @param serviceName
     *            the service name.
     * @param instanceId
     *            the instance id.
     * @param id
     *            the id.
     * @param uri
     *            the uri of the ServiceInstance.
     * @param status
     *            the OperationalStatus.
     * @param address
     *            The real address of the instance, it can be real IP or host
     *            name.
     * @param port
     *            The real port of the instance.
     * @param modifiedTime
     *            the last modified time stamp.
     * @param createTime
     *            the create time stamp.
     * @param metadata
     *            the metadata map.
     */
    public ModelServiceInstance(String serviceName, String instanceId,
            String id, String uri, OperationalStatus status, String address,
            int port, Date modifiedTime, Date createTime,
            Map<String, String> metadata) {
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.id = id;
        this.uri = uri;
        this.status = status;
        this.metadata = metadata;
        this.modifiedTime = modifiedTime;
        this.createTime = createTime;
        this.address = address;
        this.port = port;
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
     * Get the instance id.
     *
     * @return the instance id.
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Set the instance id.
     *
     * @param instanceId
     *            the instance id.
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
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
     * Set the URI.
     *
     * @param uri
     *            the URI.
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
     * check is monitor enable in Service Directory.
     *
     * @return true if monitor enabled.
     */
    public boolean isMonitorEnabled() {
        return monitorEnabled;
    }

    /**
     * Set the monitor.
     *
     * @param monitor
     *            the monitor.
     */
    public void setMonitorEnabled(boolean monitor) {
        this.monitorEnabled = monitor;
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
     * Get the last modified time stamp.
     *
     * @return the last modified time stamp.
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
     * Get the last heartbeat time stamp.
     *
     * @return the last heartbeat time stamp.
     */
    public Date getHeartbeatTime() {
        return heartbeatTime;
    }

    /**
     * Set the last heartbeat time stamp.
     *
     * @param heartbeatTime
     *            the last heartbeat time stamp.
     */
    public void setHeartbeatTime(Date heartbeatTime) {
        this.heartbeatTime = heartbeatTime;
    }

    /**
     * Get the Metadata Map.
     *
     * @return the Metadata Map.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Set the Metadata Map.
     *
     * @param metadata
     *            the Metadata Map.
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
     * Set the service name.
     *
     * @param serviceName
     *            the service name.
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

}
