package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.User;

public class CreateUserProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private User user;
	private byte[] password;
	
	public byte[] getPassword() {
		return password;
	}
	public void setPassword(byte[] password) {
		this.password = password;
	}
	public CreateUserProtocol(){
		
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public CreateUserProtocol(User user, byte[] password){
		this.user = user;
		this.password = password;
	}

}
