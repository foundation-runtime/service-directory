/**
 * Copyright 2014 Cisco Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
