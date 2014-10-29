package com.cisco.oss.foundation.directory.proto;


public class GetServiceProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String serviceName;
	private boolean watcher = false;
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
