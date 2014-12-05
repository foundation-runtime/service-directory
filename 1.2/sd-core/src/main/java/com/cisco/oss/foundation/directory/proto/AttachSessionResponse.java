/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

import java.util.Map;

import com.cisco.oss.foundation.directory.entity.ServiceInstanceToken;
import com.cisco.oss.foundation.directory.exception.ErrorCode;

/**
 * Attach ServiceInstance to Session Response.
 * 
 * @author zuxiang
 *
 */
public class AttachSessionResponse extends Response {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The ServiceInstance Item attach result Map.
	 */
	private Map<ServiceInstanceToken, ItemResult> attachingResult;
	
	public AttachSessionResponse(){
	}
	
	public AttachSessionResponse(Map<ServiceInstanceToken, ItemResult> result){
		this.attachingResult = result;
	}
	
	public Map<ServiceInstanceToken, ItemResult> getAttachingResult() {
		return attachingResult;
	}
	public void setAttachingResult(
			Map<ServiceInstanceToken, ItemResult> attachingResult) {
		this.attachingResult = attachingResult;
	}
	
	/**
	 * The ServiceInstance attach result.
	 * 
	 * @author zuxiang
	 *
	 */
	public static class ItemResult {
		/**
		 * The result, true for success.
		 */
		private boolean result;
		
		/**
		 * The ErrorCode.
		 */
		private ErrorCode error;
		
		/**
		 * The extra message.
		 */
		private String message;
		
		public boolean isResult() {
			return result;
		}
		public void setResult(boolean result) {
			this.result = result;
		}
		public ErrorCode getError() {
			return error;
		}
		public void setError(ErrorCode error) {
			this.error = error;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
	}
}

