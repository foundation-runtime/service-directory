package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.ModelService;

public class GetServiceResponse extends Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ModelService service;
	
	public GetServiceResponse(){}
	
	public GetServiceResponse(ModelService service){
		this.service = service;
	}

	public ModelService getService() {
		return service;
	}

	public void setService(ModelService service) {
		this.service = service;
	}

	
	
}
