/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.exception;

public class DirectoryServerClientException extends ServiceRuntimeException{
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param ec
	 * 		the ServiceDirectoryError.
	 */
	public DirectoryServerClientException(ServiceDirectoryError ec){
		super(ec);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param ec
	 * 		the ServiceDirectoryError.
	 * @param ex
	 * 		the root Exception.
	 */
	public DirectoryServerClientException(ServiceDirectoryError ec, Exception ex) {
		super(ec, ex);
	}
	
	
	
	/**
	 * Constructor.
	 * 
	 * @param ec
	 * 		the ServiceDirectoryError.
	 * @param message
	 * 		the error message.
	 */
	public DirectoryServerClientException(ServiceDirectoryError ec, String message) {
		super(ec, message);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param ec
	 * 		the ServiceDirectoryError.
	 * @param message
	 * 		the error message.
	 * @param ex
	 * 		the root Exception.
	 */
	public DirectoryServerClientException(ServiceDirectoryError ec, String message, Exception ex) {
		super(ec, message, ex);
	}

}
