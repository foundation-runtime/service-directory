/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory;

/**
 * The callback interface for checking the ServiceInstance health status on behalf of Service Provider.
 * 
 * Service Provider opts to implement this interface for checking the health of the ServiceInstance.
 * It attaches the ServiceInstanceHealth object when registering the ServiceInstance. ServiceDirectory will invoke
 * this ServiceInstanceHealth to update the OperationalStatus on behalf of Service Provider.
 * 
 * @author zuxiang
 *
 */
public interface ServiceInstanceHealth {
	
	/**
	 * Check the health of the ServiceInstance.
	 * 
	 * Service Provider implements the logic in this method.
	 * 
	 * @return
	 * 		true if the ServiceInstance is up and running.
	 */
	public boolean isHealthy();
}
