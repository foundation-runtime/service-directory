/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The Model ServiceInstance object in Service Directory.
 * 
 * It is the Model ServiceInstance object that has all attributes.
 * 
 * @author zuxiang
 *
 */
public class ModelServiceInstance implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
	 * The last modified time. 
	 */
	private Date modifiedTime;
	
	/**
	 * The creating time.
	 */
	private Date createTime;
	
	private BaseInfo info;
	
	/**
	 * The instance metadata info.
	 */
	private Map<String, String> metadata;
	
	/**
	 * The instance service name.
	 */
	private String serviceName;
	
	/**
	 * Constructor.
	 */
	public ModelServiceInstance(){
		
	}
	
	/**
	 * Constructor.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param instanceId
	 * 		the instance id.
	 * @param id
	 * 		the id.
	 * @param uri
	 * 		the uri of the ServiceInstance.
	 * @param status
	 * 		the OperationalStatus.
	 * @param metadata
	 * 		the metadata map.
	 */
	public ModelServiceInstance(String serviceName, String instanceId, String id, String uri, 
			OperationalStatus status, Map<String, String> metadata){
		this.serviceName = serviceName;
		this.instanceId = instanceId;
		this.id = id;
		this.uri = uri;
		this.status = status;
		this.metadata = metadata;
		
	}
	
	public ModelServiceInstance(String serviceName, String instanceId, String id, String uri, 
			OperationalStatus status, boolean monitorEnabled, Map<String, String> metadata){
		this.serviceName = serviceName;
		this.instanceId = instanceId;
		this.id = id;
		this.uri = uri;
		this.status = status;
		this.metadata = metadata;
		this.monitorEnabled = monitorEnabled;
	}

	/**
	 * Get the id.
	 * 
	 * @return
	 * 		the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Set the id.
	 * 
	 * @param id
	 * 		the id.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Get the instance id.
	 * 
	 * @return
	 * 		the instance id.
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * Set the instance id.
	 * 
	 * @param instanceId
	 * 		the instance id.
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	/**
	 * Get the URI.
	 * 
	 * @return
	 * 		the URI.
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Set the URI.
	 * 
	 * @param uri
	 * 		the URI.
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * Get the OperationalStatus.
	 * 
	 * @return
	 * 		the OperationalStatus.
	 */
	public OperationalStatus getStatus() {
		return status;
	}

	/**
	 * Set the OperationalStatus.
	 * 
	 * @param status
	 * 		the OperationalStatus.
	 */
	public void setStatus(OperationalStatus status) {
		this.status = status;
	}
	
	/**
	 * check is monitor enable in Service Directory.
	 * 
	 * @return
	 * 		true if monitor enabled.
	 */
	public boolean isMonitorEnabled() {
		return monitorEnabled;
	}

	/**
	 * Set the monitor.
	 * 
	 * @param monitor
	 * 		the monitor.
	 */
	public void setMonitorEnabled(boolean monitor) {
		this.monitorEnabled = monitor;
	}

	/**
	 * Get the Metadata Map.
	 * 
	 * @return
	 * 		the Metadata Map.
	 */
	public Map<String, String> getMetadata() {
		return metadata;
	}

	/**
	 * Set the Metadata Map.
	 * 
	 * @param metadata
	 * 		the Metadata Map.
	 */
	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	/**
	 * Get the service name.
	 * 
	 * @return
	 * 		the service name.
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * Set the service name.
	 * 
	 * @param serviceName
	 * 		the service name.
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("{");
		
		sb.append("serviceName=").append(serviceName)
			.append(", intanceId=").append(instanceId)
			.append(",status=").append(status)
			.append(", uri=").append(uri)
			.append(", monitorEnabled=").append(monitorEnabled);
		sb.append(", metadata=");
		
		if(metadata == null){
			sb.append("null");
		}else{
			sb.append("[");
			for(Entry<String, String> e : metadata.entrySet()){
				sb.append(e.getKey()).append("=").append(e.getValue()).append(";");
			}
			sb.append("]");
		}
		sb.append("}");
		return sb.toString();
	}

	public BaseInfo getInfo() {
		return info;
	}

	public void setInfo(BaseInfo info) {
		this.info = info;
	}
	
	
	@Deprecated
	public Date getModifiedTime() {
		return modifiedTime;
	}

	@Deprecated
	public void setModifiedTime(Date modifiedTime) {
		this.modifiedTime = modifiedTime;
	}

	@Deprecated
	public Date getCreateTime() {
		return createTime;
	}

	@Deprecated
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}
