package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.OperationalStatus;

public class UpdateServiceInstanceStatusProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String serviceName;
	private String instanceId;
	private OperationalStatus status;
	private boolean isOwned = false;
	private boolean restRequest = false;
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
