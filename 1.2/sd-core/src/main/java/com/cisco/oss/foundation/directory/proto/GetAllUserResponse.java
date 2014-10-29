package com.cisco.oss.foundation.directory.proto;

import java.util.List;

import com.cisco.oss.foundation.directory.entity.User;

public class GetAllUserResponse extends Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
