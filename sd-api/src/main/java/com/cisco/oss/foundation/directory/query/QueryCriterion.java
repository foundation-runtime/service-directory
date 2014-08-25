/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.query;

import java.util.Map;

/**
 * The ModelServiceInstance QueryCriterion interface.
 * 
 * The QueryCriterion provides a method to check whether a ModelServiceInstance satisfies the criterion or not.
 * 
 * @author zuxiang
 *
 */
public interface QueryCriterion {
	
	/**
	 * Filter the metadata Map of ServiceInstance.
	 * 
	 * If it satisfies the query criterion, then true is returned, otherwise false is returned.
	 * 
	 * @param metadataMap
	 * 		the metadata Map of ServiceInstance.
	 * @return
	 * 		true if matched against the QueryCriterion.
	 */
	public boolean isMatch(Map<String, String> metadataMap);
	
	/**
	 * Get the metadata key String that the QueryCriterion validate against.
	 * 
	 * @return
	 * 		the metadata key String.
	 */
	public String getMetadataKey();
	
}
