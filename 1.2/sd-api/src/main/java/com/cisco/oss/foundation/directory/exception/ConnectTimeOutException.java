/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.exception;

/**
 * The Connect timeout exception.
 * 
 * @author zuxiang
 *
 */
public class ConnectTimeOutException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public ConnectTimeOutException(){
		
	}
	
	/**
	 * Constructor.
	 * 
	 * @param msg
	 * 		the error message.
	 */
	public ConnectTimeOutException(String msg){
		super(msg);
	}
}
