/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

/**
 * Get Metadata Protocol.
 * 
 * @author zuxiang
 *
 */
public class GetMetadataProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The meta key name.
	 */
	private String keyName;
	
	public GetMetadataProtocol(){
		
	}
	
	public GetMetadataProtocol(String keyName){
		this.keyName = keyName;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}
	
	

}
