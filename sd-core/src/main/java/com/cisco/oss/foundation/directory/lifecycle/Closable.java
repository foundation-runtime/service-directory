/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.lifecycle;

/**
 * Interface to manage the Closable component.
 * 
 * @author zuxiang
 *
 */
public interface Closable {

	/**
	 * Start the component.
	 */
	public void start();
	
	/**
	 * Stop the component.
	 */
	public void stop();
}
