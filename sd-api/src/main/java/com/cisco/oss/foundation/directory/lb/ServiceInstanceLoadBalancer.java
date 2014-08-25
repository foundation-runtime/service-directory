/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.lb;

import com.cisco.oss.foundation.directory.entity.ServiceInstance;

/**
 * Load Balancer interface for ServiceInstance.
 * 
 * @author zuxiang
 *
 */
public interface ServiceInstanceLoadBalancer {
	
	/**
	 * Vote a ServiceInstance.
	 * 
	 * @return
	 * 		the ServiceInstance.
	 */
	public ServiceInstance vote();
}
