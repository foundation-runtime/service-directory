package com.cisco.oss.foundation.directory.proto;

import java.util.Map;

public class GetMetadataChangingByVersionProtocol extends Protocol {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Map<String, Long> metadatas;
	
	public GetMetadataChangingByVersionProtocol(){
		
	}
	
	public GetMetadataChangingByVersionProtocol(Map<String, Long> metadatas){
		this.metadatas = metadatas;
	}

	public Map<String, Long> getMetadatas() {
		return metadatas;
	}

	public void setMetadatas(Map<String, Long> metadatas) {
		this.metadatas = metadatas;
	}
}
