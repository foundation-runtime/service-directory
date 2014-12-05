/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

/**
 * Get Service Protocol.
 * 
 * @author zuxiang
 *
 */
public class GetServiceProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The serviceName.
	 */
	private String serviceName;
	
	/**
	 * Indicate whether watcher the Service.
	 */
	private boolean watcher = false;
	
	/**
	 * Get Service whether include all ServiceInstances.
	 */
	private boolean withInstances = true;
	
	public GetServiceProtocol(){
		
	}
	
	public GetServiceProtocol(String serviceName){
		this.serviceName = serviceName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public boolean isWatcher() {
		return watcher;
	}

	public void setWatcher(boolean watcher) {
		this.watcher = watcher;
	}

	public boolean isWithInstances() {
		return withInstances;
	}

	public void setWithInstances(boolean withInstances) {
		this.withInstances = withInstances;
	}
	
	

}
