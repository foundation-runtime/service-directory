package com.cisco.oss.foundation.directory.proto;

import java.util.List;

import com.cisco.oss.foundation.directory.entity.ServiceInstanceToken;

public class AttachSessionProtocol extends Protocol {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String sessionId;
	private List<ServiceInstanceToken> serviceInstances;
	
	public AttachSessionProtocol(){
		
	}
	
	public AttachSessionProtocol(List<ServiceInstanceToken> serviceInstances, String sessionId){
		this.serviceInstances = serviceInstances;
		this.sessionId = sessionId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public List<ServiceInstanceToken> getServiceInstances() {
		return serviceInstances;
	}

	public void setServiceInstances(List<ServiceInstanceToken> serviceInstances) {
		this.serviceInstances = serviceInstances;
	}
	
	
}
