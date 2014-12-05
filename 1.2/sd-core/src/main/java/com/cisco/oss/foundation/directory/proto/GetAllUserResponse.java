/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

import java.util.List;

import com.cisco.oss.foundation.directory.entity.User;

/**
 * Get All user Response.
 * 
 * @author zuxiang
 *
 */
public class GetAllUserResponse extends Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The User List.
	 */
	private List<User> users;
	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public GetAllUserResponse(){
		
	}
	
	public GetAllUserResponse(List<User> users){
		this.users = users;
	}
}
