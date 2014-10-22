/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.entity;

import com.cisco.oss.foundation.directory.exception.ServiceDirectoryError;

/**
 * The class to describe the operation result.
 * 
 * It has the result field to indicate the operation result, true for succeed, false for fail.
 * If result is false, see the ServiceDirectoryError to error field.
 * Now it used in the batch update operation, like send heartbeat for bunches of ServiceInstance.
 *  
 * @author zuxiang
 *
 */
public class OperationResult<T> {

	/**
	 * The operation result, true for success, false for fail.
	 */
	private boolean result;
	
	/**
	 * The operation return Object.
	 */
	private T object;
	
	/**
	 * The operation error.
	 */
	private ServiceDirectoryError error;
	
	/**
	 * Default constructor.
	 */
	public OperationResult(){
		
	}
	
	/**
	 * Constructor.
	 * 
	 * @param result
	 * 		the operation result, true for success, false for fail.
	 * @param object
	 * 		the operation response object.
	 * @param error
	 * 		the operation error if result is false.
	 */
	public OperationResult(boolean result, T object, ServiceDirectoryError error){
		this.result = result;
		this.object = object;
		this.error = error;
	}

	/**
	 * Get the operation result.
	 * 
	 * @return
	 * 		the operation result.
	 */
	public boolean getResult() {
		return result;
	}

	/**
	 * Set the operation result.
	 * 
	 * @param result
	 * 		the operation result.
	 */
	public void setResult(boolean result) {
		this.result = result;
	}

	/**
	 * get the operation Object.
	 * 
	 * @return
	 * 		the operation response Object.
	 */
	public T getobject() {
		return object;
	}

	/**
	 * Set the operation Object.
	 * 
	 * @param object
	 * 		the operation response Object.
	 */
	public void setObject(T object) {
		this.object = object;
	}

	/**
	 * Get the operation error.
	 * 
	 * @return
	 * 		the operation error.
	 */
	public ServiceDirectoryError getError() {
		return error;
	}

	/**
	 * Set the operation error.
	 * 
	 * @param error
	 * 		the operation error.
	 */
	public void setError(ServiceDirectoryError error) {
		this.error = error;
	}
	
	
}
