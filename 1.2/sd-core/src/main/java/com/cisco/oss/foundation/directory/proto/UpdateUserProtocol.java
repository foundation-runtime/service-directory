package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.User;

public class UpdateUserProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private User user;
	public UpdateUserProtocol(){
		
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public UpdateUserProtocol(User user){
		this.user = user;
	}

}
