/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.exception;

/**
 * The sub RuntimeException to wrap ServiceDirectory fails and errors.
 * 
 * The ServiceException has the ExceptionCode to category the certain error type.
 * 
 * @author zuxiang
 *
 */
public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = -3706093386454084825L;
	/**
	 * The ExceptionCode.
	 */
	private ServiceDirectoryError error;

	/**
	 * Constructor.
	 * 
	 * @param error
	 * 		the ServiceDirectoryError.
	 */
	public ServiceException(ServiceDirectoryError error) {
		super(error.getErrorMessage());
		this.error = error;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param error
	 * 		the ServiceDirectoryError.
	 */
	public ServiceException(ServiceDirectoryError error, String message) {
		super(error.getErrorMessage() + ", " + message);
		this.error = error;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param error
	 * 		the ServiceDirectoryError.
	 * @param ex
	 * 		the root Exception.
	 */
	public ServiceException(ServiceDirectoryError error, Exception ex) {
		super(ex);
		this.error = error;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param error
	 * 		the ServiceDirectoryError.
	 * @param message
	 * 		the error message.
	 * @param ex
	 * 		the exception.
	 */
	public ServiceException(ServiceDirectoryError error, String message, Exception ex){
		super(error.getErrorMessage() + ", " + message, ex);
		this.error = error;
	}

	/**
	 * Get the ServiceDirectoryError.
	 * 
	 * @return
	 * 		the ServiceDirectoryError.
	 */
	public ServiceDirectoryError getServiceDirectoryError() {
		return error;
	}
}
