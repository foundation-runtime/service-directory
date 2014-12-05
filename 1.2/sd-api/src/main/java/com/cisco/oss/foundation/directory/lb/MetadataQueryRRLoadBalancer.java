/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.lb;

import java.util.Collections;
import java.util.List;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.impl.DirectoryLookupService;
import com.cisco.oss.foundation.directory.impl.ServiceInstanceQueryHelper;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;

/**
 * The metadata Query RondRonbin Loadbalancer when lookupInstance.
 * 
 * @author zuxiang
 *
 */
public class MetadataQueryRRLoadBalancer extends RoundRobinLoadBalancer {

	/**
	 * The ServiceInstanceQuery.
	 */
	private final ServiceInstanceQuery query;

	/**
	 * Constructor.
	 * 
	 * @param lookupService
	 * 		the DirectoryLookupService.
	 * @param query
	 * 		the ServiceInstanceQuery.
	 */
	public MetadataQueryRRLoadBalancer(DirectoryLookupService lookupService,
			ServiceInstanceQuery query) {
		super(lookupService);
		this.query = query;
	}

	/**
	 * Get the ServiceInstanceQuery.
	 * 
	 * @return	
	 * 		get the ServiceInstanceQuery.
	 */
	public ServiceInstanceQuery getServiceInstanceQuery() {
		return query;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ModelServiceInstance> getServiceInstanceList() {
		List<ModelServiceInstance> instances = null;
		String keyName = null;
		if (query.getCriteria().size() > 0) {
			keyName = query.getCriteria().get(0).getMetadataKey();
		}
		if (keyName != null && !keyName.isEmpty()) {
			List<ModelServiceInstance> modelInstances = getLookupService()
					.queryUPModelInstances(query);
			instances = ServiceInstanceQueryHelper
					.filter(query, modelInstances);
		}

		if (instances != null) {
			return instances;
		} else {
			return Collections.emptyList();
		}
	}
}
