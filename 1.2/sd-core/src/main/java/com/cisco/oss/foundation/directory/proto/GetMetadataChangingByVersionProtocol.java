/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

import java.util.Map;

/**
 * Get ModelMetadataKey change by version Protocol.
 * 
 * @author zuxiang
 *
 */
public class GetMetadataChangingByVersionProtocol extends Protocol {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The MetadataKey version map.
	 */
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
