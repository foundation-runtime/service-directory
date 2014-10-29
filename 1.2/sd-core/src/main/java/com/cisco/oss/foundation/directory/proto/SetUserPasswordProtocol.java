package com.cisco.oss.foundation.directory.proto;

public class SetUserPasswordProtocol extends Protocol{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userName;
	private byte[] secret;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public byte[] getSecret() {
		return secret;
	}

	public void setSecret(byte[] secret) {
		this.secret = secret;
	}

	public SetUserPasswordProtocol(){
		
	}
	
	public SetUserPasswordProtocol(String userName, byte[] secret){
		this.userName = userName;
		this.secret = secret;
	}
}
