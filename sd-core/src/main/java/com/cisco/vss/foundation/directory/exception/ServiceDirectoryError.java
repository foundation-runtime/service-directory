/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.vss.foundation.directory.exception;

import java.text.MessageFormat;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * The official ServiceDirectory ERROR.
 * 
 * This error will throw to upper Application in Exception. Application can do recovering 
 * according to the ExceptionCode if required.
 * 
 * @author zuxiang
 *
 */
public class ServiceDirectoryError {

	/**
	 * The ExceptionCode.
	 */
	private ErrorCode exceptionCode;
	
	/**
	 * resource bundle replacement.
	 */
	private Object[] params;
	
	/**
	 * Default constructor for JSON serializer.
	 */
	public ServiceDirectoryError(){
		
	}

	/**
	 * Constructor.
	 * 
	 * @param ec
	 * 		the ExceptionCode.
	 * @param params
	 * 		the string holder parameters.
	 */
	public ServiceDirectoryError(ErrorCode ec, Object... params) {
		this.params = params;
		this.exceptionCode = ec;
	}
	
	/**
	 * Get the locale-specific error message.
	 * 
	 * @return
	 * 		the error message String.
	 */
	@JsonIgnore
	public String getErrorMessage(){
        if (params != null && params.length > 0) {
            return (MessageFormat.format(exceptionCode.getMessage(), params));
        } else {
            return (exceptionCode.getMessage());
        }
	}
	
	/**
	 * Get the String holder parameters.
	 * 
	 * @return
	 * 		the String holder parameters.
	 */
	public Object[] getParams(){
		return this.params;
	}
	
	/**
	 * Get the ExceptionCode of the error.
	 * 
	 * @return
	 * 		the ExceptionCode.
	 */
	public ErrorCode getExceptionCode(){
		return exceptionCode;
	}
}
