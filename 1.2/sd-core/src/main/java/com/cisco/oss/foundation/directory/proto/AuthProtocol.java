package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.AuthScheme;

public class AuthProtocol extends Protocol {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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
