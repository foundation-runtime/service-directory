/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable ServiceInstance provided to the Service Consumer.
 */
public class ServiceInstance {
	
	/**
	 * ServiceInstance id, unique in Service.
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
	private OperationalStatus status;
	
	/**
	 * Whether the instance enabled Monitor in Service Directory.
	 */
	private boolean monitorEnabled = true;
	
	/**
	 * ServiceInstance metadata
	 */
	private final Map<String, String> metadata;

	/**
	 * Constructor.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param instanceId
	 * 		the instance id.
	 * @param uri
	 * 		the uri.
	 * @param metadata
	 * 		the metadata Map.
	 */
	public ServiceInstance(String serviceName, String instanceId, String uri, boolean monitor, OperationalStatus status, Map<String, String> metadata) {
		this.serviceName = serviceName;
		this.instanceId = instanceId;
		this.uri = uri;
		this.monitorEnabled = monitor;
		this.status = status;
		this.metadata = new HashMap<String, String>();
		if(metadata != null && metadata.size() != 0){
			this.metadata.putAll(metadata);
		}
	}

	/**
	 * Get the instance id.
	 * @return
	 * 		the instance id.
	 */
	public String getInstanceId() {
		return instanceId;
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
	 * Get the URI.
	 * 
	 * @return
	 * 		the URI.
	 */
	public String getUri() {
		return uri;
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
	 * Get the OperationalStatus.
	 * 
	 * @return
	 * 		the OperationalStatus.
	 */
	public OperationalStatus getStatus() {
		return status;
	}

	/**
	 * Get the metadata Map.
	 * 
	 * @return
	 * 		the metadata Map.
	 */
	public Map<String, String> getMetadata() {
		return new HashMap<String, String>(this.metadata);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "{serviceName:" + serviceName + ",clientId:" + instanceId + "}";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ServiceInstance) {
			ServiceInstance si = (ServiceInstance) obj;
			if (instanceId == null || serviceName == null) {
				return false;
			}
			return instanceId.equals(si.getInstanceId()) && serviceName.equals(si.getServiceName());
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int result = instanceId != null ? instanceId.hashCode() : 0;
		result = 31 * result + serviceName != null ? serviceName.hashCode() : 0;
		return result;
	}
}
