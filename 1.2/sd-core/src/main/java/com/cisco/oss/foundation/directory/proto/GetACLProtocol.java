package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.AuthScheme;

public class GetACLProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private AuthScheme scheme;
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
