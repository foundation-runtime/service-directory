/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

import java.util.List;

import com.cisco.oss.foundation.directory.entity.ServiceInstanceToken;

/**
 * Attach ServiceInstance to Session Protocol.
 * 
 * @author zuxiang
 *
 */
public class AttachSessionProtocol extends Protocol {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The Session id.
	 */
	private String sessionId;
	
	/**
	 * The instance list.
	 */
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
