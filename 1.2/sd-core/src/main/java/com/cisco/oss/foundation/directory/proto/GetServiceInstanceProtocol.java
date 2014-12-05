/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

/**
 * Get ServiceInstance Protocol.
 * 
 * @author zuxiang
 *
 */
public class GetServiceInstanceProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * the serviceName.
	 */
	private String serviceName;
	
	/**
	 * The instanceId.
	 */
	private String instanceId;
	
	public GetServiceInstanceProtocol(){
		
	}
	
	public GetServiceInstanceProtocol(String serviceName, String instanceId){
		this.serviceName = serviceName;
		this.instanceId = instanceId;
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
