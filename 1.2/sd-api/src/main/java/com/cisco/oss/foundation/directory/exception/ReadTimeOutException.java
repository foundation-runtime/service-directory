/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.exception;

/**
 * The Read timeout exception in DirectorySocket.
 * 
 * @author zuxiang
 *
 */
public class ReadTimeOutException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public ReadTimeOutException(){
		
	}
	
	/**
	 * Constructor.
	 * 
	 * @param msg
	 * 		the error message.
	 */
	public ReadTimeOutException(String msg){
		super(msg);
	}
}
