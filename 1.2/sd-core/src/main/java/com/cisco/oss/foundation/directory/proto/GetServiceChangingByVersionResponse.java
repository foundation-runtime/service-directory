package com.cisco.oss.foundation.directory.proto;

import java.util.Map;

import com.cisco.oss.foundation.directory.entity.ModelService;

public class GetServiceChangingByVersionResponse extends Response {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Map<String, ModelService> services;
	
	public GetServiceChangingByVersionResponse(){
		
	}
	
	public GetServiceChangingByVersionResponse(Map<String, ModelService> services){
		this.services = services;
	}

	public Map<String, ModelService> getServices() {
		return services;
	}

	public void setServices(Map<String, ModelService> services) {
		this.services = services;
	}
}
