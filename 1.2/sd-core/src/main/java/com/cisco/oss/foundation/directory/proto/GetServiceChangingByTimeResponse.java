/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

import java.util.Map;

import com.cisco.oss.foundation.directory.entity.ModelService;

/**
 * Get the ModelService change by time Response.
 * 
 * @author zuxiang
 *
 */
public class GetServiceChangingByTimeResponse extends Response {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The ModelService map.
	 */
	private Map<String, ModelService> services;
	
	public GetServiceChangingByTimeResponse(){
		
	}
	
	public GetServiceChangingByTimeResponse(Map<String, ModelService> services){
		this.services = services;
	}

	public Map<String, ModelService> getServices() {
		return services;
	}

	public void setServices(Map<String, ModelService> services) {
		this.services = services;
	}
}
