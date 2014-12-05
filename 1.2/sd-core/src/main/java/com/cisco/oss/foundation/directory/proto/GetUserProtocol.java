/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

/**
 * Get User Protocol.
 * 
 * @author zuxiang
 *
 */
public class GetUserProtocol extends Protocol {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The user name.
	 */
	private String userName ;
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public GetUserProtocol(){
		
	}
	
	public GetUserProtocol(String userName){
		this.userName = userName;
	}
}
