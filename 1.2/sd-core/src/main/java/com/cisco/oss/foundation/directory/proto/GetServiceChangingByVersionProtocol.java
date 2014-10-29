package com.cisco.oss.foundation.directory.proto;

import java.util.Map;

public class GetServiceChangingByVersionProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Map<String, Long> services;
	
	public GetServiceChangingByVersionProtocol(){
		
	}
	
	public GetServiceChangingByVersionProtocol(Map<String, Long> services){
		this.services = services;
	}

	public Map<String, Long> getServices() {
		return services;
	}

	public void setServices(Map<String, Long> services) {
		this.services = services;
	}
	
	
}
