package com.cisco.oss.foundation.directory.proto;

import java.util.List;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;

public class GetAllServicesResponse extends Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<ModelServiceInstance> instances;
	public List<ModelServiceInstance> getInstances() {
		return instances;
	}

	public void setInstances(List<ModelServiceInstance> instances) {
		this.instances = instances;
	}

	public GetAllServicesResponse(){
		
	}
	
	public GetAllServicesResponse(List<ModelServiceInstance> instances){
		this.instances = instances;
	}
}
