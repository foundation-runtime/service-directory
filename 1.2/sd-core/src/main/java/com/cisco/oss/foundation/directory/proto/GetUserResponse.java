package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.User;

public class GetUserResponse extends Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private User user;
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public GetUserResponse(){
		
	}
	
	public GetUserResponse(User user){
		this.user = user;
	}
}
