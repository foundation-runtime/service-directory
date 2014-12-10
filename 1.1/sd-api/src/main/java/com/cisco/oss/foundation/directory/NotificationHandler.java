/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory;

import com.cisco.oss.foundation.directory.entity.ServiceInstance;

/**
 * The callback interface when the service instance status is changed.
 * 
 * @author zuxiang
 *
 */
public interface NotificationHandler {
	/**
	 * A service instance is available
	 * 
	 * When ServiceInstance registered, but it may be DOWN.
	 * 
	 * @param service
	 *          The ServiceInstance which the NotificationHandler triggered for.
	 *             
	 */
	public void serviceInstanceAvailable(ServiceInstance service);
	
	/**
	 * A service instance is unavailable
	 * 
	 * When ServiceInstance unregistered.
	 * 
	 * @param service
	 * 			The ServiceInstance which the NotificationHandler triggered for.
	 *             
	 */
	public void serviceInstanceUnavailable(ServiceInstance service);
	
	/**
	 * A service instance change.
	 * 
	 * When ServiceInstance changed, it includes the status change.
	 * 
	 * @param service
	 * 			The ServiceInstance which the NotificationHandler triggered for.
	 */
	public void serviceInstanceChange(ServiceInstance service);
	
}
