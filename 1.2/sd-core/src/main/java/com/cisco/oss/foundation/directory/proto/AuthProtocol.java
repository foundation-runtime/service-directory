/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.AuthScheme;

/**
 * The Authentication Protocol.
 * 
 * @author zuxiang
 *
 */
public class AuthProtocol extends Protocol {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * THe AuthScheme.
	 */
	private AuthScheme scheme;

	public AuthProtocol() {
	}

	public AuthProtocol(AuthScheme scheme) {
		this.scheme = scheme;
	}

	public AuthScheme getScheme() {
		return scheme;
	}

	public void setScheme(AuthScheme scheme) {
		this.scheme = scheme;
	}
	
	
}
