package com.cisco.oss.foundation.directory.exception;

public class ConnectTimeOutException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConnectTimeOutException(){
		
	}
	
	public ConnectTimeOutException(String msg){
		super(msg);
	}
}
