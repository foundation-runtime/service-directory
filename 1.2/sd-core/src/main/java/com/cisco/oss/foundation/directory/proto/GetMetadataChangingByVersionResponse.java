package com.cisco.oss.foundation.directory.proto;

import java.util.Map;

import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;

public class GetMetadataChangingByVersionResponse extends Response {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Map<String, ModelMetadataKey> metadatas;
	
	public GetMetadataChangingByVersionResponse(){
		
	}
	
	public GetMetadataChangingByVersionResponse(Map<String, ModelMetadataKey> metadatas){
		this.metadatas = metadatas;
	}

	public Map<String, ModelMetadataKey> getMetadatas() {
		return metadatas;
	}

	public void setMetadatas(Map<String, ModelMetadataKey> metadatas) {
		this.metadatas = metadatas;
	}
}
