package com.cisco.oss.foundation.directory.proto;


public class DeleteUserProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userName;
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public DeleteUserProtocol(){
		
	}
	
	public DeleteUserProtocol(String userName){
		this.userName = userName;
	}
}
