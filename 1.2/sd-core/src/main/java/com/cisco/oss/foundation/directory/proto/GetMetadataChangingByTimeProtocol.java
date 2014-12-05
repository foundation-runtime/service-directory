/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

import java.util.Map;

import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;

/**
 * Get MetadataKey Changing by Time Protocol.
 * 
 * @author zuxiang
 *
 */
public class GetMetadataChangingByTimeProtocol extends Protocol {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The ModelMetadataKey map.
	 */
	private Map<String, ModelMetadataKey> metadatas;
	
	public GetMetadataChangingByTimeProtocol(){
		
	}
	
	public GetMetadataChangingByTimeProtocol(Map<String, ModelMetadataKey> metadatas){
		this.metadatas = metadatas;
	}

	public Map<String, ModelMetadataKey> getMetadatas() {
		return metadatas;
	}

	public void setMetadatas(Map<String, ModelMetadataKey> metadatas) {
		this.metadatas = metadatas;
	}
}
