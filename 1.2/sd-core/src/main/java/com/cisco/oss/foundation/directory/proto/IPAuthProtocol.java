package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.AuthScheme;

public class IPAuthProtocol extends AuthProtocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String clientIP;
	
	public IPAuthProtocol(){
		
	}
	
	public IPAuthProtocol(String clientIP){
		super(AuthScheme.IP);
		this.clientIP = clientIP;
	}

	public String getClientIP() {
		return clientIP;
	}

	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}

}
