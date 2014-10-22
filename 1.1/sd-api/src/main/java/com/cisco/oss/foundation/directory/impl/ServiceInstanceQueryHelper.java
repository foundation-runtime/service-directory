/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.query.QueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;

/**
 * The helper class to do the ServiceInstances filter against the QueryCriterion.
 * 
 * @author zuxiang
 *
 */
public class ServiceInstanceQueryHelper {
	
	/**
	 * Filter the ModelServiceInstance list against the ServiceInstanceQuery.
	 * 
	 * @param query
	 * 		the ServiceInstanceQuery matchers.
	 * @param list
	 * 		the ModelServiceInstance list.
	 * @return
	 * 		the matched ModelServiceInstance list.
	 */
	public static List<ModelServiceInstance> filter(ServiceInstanceQuery query, List<ModelServiceInstance> list) {

		if (list == null || list.size() == 0) {
			return Collections.emptyList();
		}

		List<QueryCriterion> criteria = query.getCriteria();
		
		if (criteria == null || criteria.size() == 0) {
			return list;
		}

		List<ModelServiceInstance> instances = new ArrayList<ModelServiceInstance>();
		for (ModelServiceInstance instance : list) {
			boolean passed = true;
			for (QueryCriterion criterion : criteria) {
				if (criterion.isMatch(instance.getMetadata()) == false) {
					passed = false;
					break;
				}
			}
			if (passed) {
				instances.add(instance);
			}
		}

		return instances;

	}
	
	/**
	 * Filter the ServiceInstance list against the ServiceInstanceQuery.
	 * 
	 * @param query
	 * 		the ServiceInstanceQuery matchers.
	 * @param list
	 * 		the ServiceInstance list.
	 * @return
	 * 		the matched ServiceInstance list.
	 */
	public static List<ServiceInstance> filterServiceInstance(ServiceInstanceQuery query, List<ServiceInstance> list) {

		if (list == null || list.size() == 0) {
			return Collections.emptyList();
		}

		List<QueryCriterion> criteria = query.getCriteria();
		
		if (criteria == null || criteria.size() == 0) {
			return list;
		}

		List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
		for (ServiceInstance instance : list) {
			boolean passed = true;
			for (QueryCriterion criterion : criteria) {
				if (criterion.isMatch(instance.getMetadata()) == false) {
					passed = false;
					break;
				}
			}
			if (passed) {
				instances.add(instance);
			}
		}

		return instances;

	}
}
