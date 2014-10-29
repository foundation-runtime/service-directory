package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;

public class GetServiceInstanceResponse extends Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ModelServiceInstance serviceInstance;
	
	public GetServiceInstanceResponse(){
		
	}
	
	public GetServiceInstanceResponse(ModelServiceInstance serviceInstance){
		this.serviceInstance = serviceInstance;
	}

	public ModelServiceInstance getServiceInstance() {
		return serviceInstance;
	}

	public void setServiceInstance(ModelServiceInstance serviceInstance) {
		this.serviceInstance = serviceInstance;
	}
	
	
}
