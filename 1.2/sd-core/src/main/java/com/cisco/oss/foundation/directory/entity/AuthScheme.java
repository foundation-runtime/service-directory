/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.entity;

/**
 * The Authentication Scheme type.
 * 
 * @author zuxiang
 *
 */
public enum AuthScheme {
	
	/**
	 * The userName and password based authentication and authorization.
	 */
	DIRECTORY,
	
	/**
	 * The remote client IP address based authentication and authorization.
	 */
	IP
}
