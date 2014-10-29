package com.cisco.oss.foundation.directory.proto;

public class GetServiceInstanceProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String serviceName;
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
