package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.AuthScheme;

public class GetAllACLProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
