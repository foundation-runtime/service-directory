/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.AuthScheme;

/**
 * Get ACL Protocol.
 * 
 * @author zuxiang
 *
 */
public class GetACLProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The ACL AuthScheme.
	 */
	private AuthScheme scheme;
	
	/**
	 * The identity id.
	 */
	private String id ;
	
	public AuthScheme getScheme() {
		return scheme;
	}

	public void setScheme(AuthScheme scheme) {
		this.scheme = scheme;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public GetACLProtocol(){
		
	}
	
	public GetACLProtocol(AuthScheme scheme, String id){
		this.scheme = scheme;
		this.id = id;
	}
}
