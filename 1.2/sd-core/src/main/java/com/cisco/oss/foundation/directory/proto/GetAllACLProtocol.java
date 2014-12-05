/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.AuthScheme;

/**
 * Get all ACL by AuthScheme Protocol.
 * 
 * @author zuxiang
 *
 */
public class GetAllACLProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The AuthScheme.
	 */
	private AuthScheme scheme;
	public AuthScheme getScheme() {
		return scheme;
	}

	public void setScheme(AuthScheme scheme) {
		this.scheme = scheme;
	}

	public GetAllACLProtocol(){
		
	}
	
	public GetAllACLProtocol(AuthScheme scheme){
		this.scheme = scheme;
	}
}
