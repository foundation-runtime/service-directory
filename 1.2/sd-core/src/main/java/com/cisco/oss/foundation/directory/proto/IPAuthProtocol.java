/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.AuthScheme;

/**
 * IP Authentication Protocol.
 * 
 * @author zuxiang
 *
 */
public class IPAuthProtocol extends AuthProtocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The client ip.
	 */
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
