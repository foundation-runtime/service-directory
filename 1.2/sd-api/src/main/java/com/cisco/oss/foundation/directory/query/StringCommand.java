/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.query;

import com.cisco.oss.foundation.directory.proto.QueryServiceProtocol.QueryCommand;

/**
 * The interface to get the QueryCommand from the QueryCritrion.
 * 
 * @author zuxiang
 *
 */
public interface StringCommand {
	/**
	 * Get the QueryCommand of the QueryCriterion.
	 * 
	 * @return
	 * 		the QueryCommand.
	 */
	public QueryCommand getStringCommand();
}
