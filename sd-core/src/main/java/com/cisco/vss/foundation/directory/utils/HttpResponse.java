/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.vss.foundation.directory.utils;

/**
 * The wrapper to the HttpResponse.
 * 
 * @author zuxiang
 *
 */
public class HttpResponse {
	/**
	 * The HttpCode
	 */
	private int httpCode;
	
	/**
	 * The response body String.
	 */
	private String retBody;

	/**
	 * Constructor.
	 * 
	 * @param httpCode
	 * 		the HttpCode.
	 * @param retBody
	 * 		the Http body String.
	 */
	public HttpResponse(int httpCode, String retBody) {
		super();
		this.httpCode = httpCode;
		this.retBody = retBody;
	}

	/**
	 * Get HttpCode.
	 * 
	 * @return
	 * 		the HttpCode.
	 */
	public int getHttpCode() {
		return httpCode;
	}

	/**
	 * Set HttpCode.
	 * 
	 * @param httpCode
	 * 		the HttpCode.
	 */
	public void setHttpCode(int httpCode) {
		this.httpCode = httpCode;
	}

	/**
	 * Get the Http body String.
	 * @return
	 * 		the Http body String.
	 */
	public String getRetBody() {
		return retBody;
	}

	/**
	 * Set the Http body String.
	 * 
	 * @param retBody
	 * 		the Http body String.
	 */
	public void setRetBody(String retBody) {
		this.retBody = retBody;
	}

}
