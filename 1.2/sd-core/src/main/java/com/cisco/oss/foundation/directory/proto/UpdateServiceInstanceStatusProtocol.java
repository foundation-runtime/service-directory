/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.OperationalStatus;

/**
 * Update ServiceInstance status protocol.
 * @author zuxiang
 *
 */
public class UpdateServiceInstanceStatusProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * the service name.
	 */
	private String serviceName;
	
	/**
	 * the instance id.
	 */
	private String instanceId;
	
	/**
	 * The service instance status.
	 */
	private OperationalStatus status;
	
	/**
	 * is owned ServiceInstance.
	 */
	private boolean isOwned = false;
	
	/**
	 * is rest request.
	 */
	private boolean restRequest = false;
	
	/**
	 * is no session based request.
	 */
	private boolean noSession = false;
	
	public UpdateServiceInstanceStatusProtocol(){
		
	}

	public boolean isOwned() {
		return isOwned;
	}

	public void setOwned(boolean isOwned) {
		this.isOwned = isOwned;
	}

	public boolean isRestRequest() {
		return restRequest;
	}

	public void setRestRequest(boolean restRequest) {
		this.restRequest = restRequest;
	}

	public UpdateServiceInstanceStatusProtocol(String serviceName, String instanceId, OperationalStatus status){
		this.serviceName = serviceName;
		this.instanceId = instanceId;
		this.status = status;
	}
	
	public UpdateServiceInstanceStatusProtocol(String serviceName, String instanceId, OperationalStatus status, boolean restRequest, boolean isOwned, boolean noSession){
		this.serviceName = serviceName;
		this.instanceId = instanceId;
		this.status = status;
		this.restRequest = restRequest;
		this.isOwned = isOwned;
		this.noSession = noSession;
	}

	public boolean isNoSession() {
		return noSession;
	}

	public void setNoSession(boolean noSession) {
		this.noSession = noSession;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public OperationalStatus getStatus() {
		return status;
	}

	public void setStatus(OperationalStatus status) {
		this.status = status;
	}
	
	
}
