/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

/**
 * Unregister ServiceInstance Protocol.
 * 
 * @author zuxiang
 *
 */
public class UnregisterServiceInstanceProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Service name.
	 */
	private String serviceName;
	
	/**
	 * Instance id.
	 */
	private String instanceId;
	
	/**
	 * is own the ServiceInstanc.
	 */
	private boolean isOwned = false;
	
	/**
	 * is rest request.
	 */
	private boolean restRequest = false;
	
	/**
	 * is session based.
	 */
	private boolean noSession = false;
	
	public UnregisterServiceInstanceProtocol(){
		
	}
	
	public UnregisterServiceInstanceProtocol(String serviceName, String instanceId){
		this.serviceName = serviceName;
		this.instanceId = instanceId;
	}
	
	public UnregisterServiceInstanceProtocol(String serviceName, String instanceId, boolean restRequest, boolean isOwned, boolean noSession){
		this.serviceName = serviceName;
		this.instanceId = instanceId;
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
	
	
}
