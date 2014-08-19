/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.vss.foundation.directory;

import com.cisco.vss.foundation.directory.entity.ServiceInstance;
import com.cisco.vss.foundation.directory.exception.ServiceException;

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
	 * @param service
	 *          The ServiceInstance which the NotificationHandler triggered for.
	 * @throws ServiceException
	 *             
	 */
	public void serviceInstanceAvailable(ServiceInstance service) throws ServiceException;
	
	/**
	 * A service instance is unavailable
	 * 
	 * @param service
	 * 			The ServiceInstance which the NotificationHandler triggered for.
	 * @throws ServiceException
	 *             
	 */
	public void serviceInstanceUnavailable(ServiceInstance service) throws ServiceException;
	
}
