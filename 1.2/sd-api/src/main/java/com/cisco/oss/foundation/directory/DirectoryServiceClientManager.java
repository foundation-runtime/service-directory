/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory;

import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.impl.DirectoryServiceClient;

/**
 * The Service Directory client manager.
 * 
 * @author zuxiang
 *
 */
public interface DirectoryServiceClientManager {
	
	
	/**
	 * Get the DirectoryServiceClient.
	 * 
	 * It is thread safe in lazy initialization.
	 * 
	 * @return
	 * 		the directory server client.
	 * @throws ServiceException 
	 */
	public DirectoryServiceClient getDirectoryServiceClient();

}
