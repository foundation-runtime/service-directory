/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.async;

import com.cisco.oss.foundation.directory.proto.ServiceInstanceOperate;

/**
 * The Service Watcher interface.
 * 
 * We can register the watcher for a Service. Then SD API can invoke the
 * Watcher for the Service update.
 * 
 * @author zuxiang
 *
 */
public interface Watcher {

	/**
	 * The method invoked for the Watcher for the Service update.
	 * 
	 * @param name
	 * 		the ServiceName.
	 * @param operate
	 * 		the actual ServiceInstance update Operation of the Service.
	 */
	public void process(String name, ServiceInstanceOperate operate);
}
