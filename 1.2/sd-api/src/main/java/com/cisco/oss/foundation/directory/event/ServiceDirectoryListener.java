/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.event;

/**
 * The ServiceDirectoryEvent listener.
 * 
 * @author zuxiang
 *
 */
public interface ServiceDirectoryListener {

	/**
	 * notify the listener the ServiceDirectoryEvent.
	 * 
	 * @param event
	 * 		the ServiceDirectoryEvent.
	 */
	public void notify(ServiceDirectoryEvent event);
	
}
