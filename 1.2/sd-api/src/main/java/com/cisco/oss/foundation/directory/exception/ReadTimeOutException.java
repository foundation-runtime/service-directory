package com.cisco.oss.foundation.directory.exception;

public class ReadTimeOutException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ReadTimeOutException(){
		
	}
	
	public ReadTimeOutException(String msg){
		super(msg);
	}
}
