/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.lb;

import java.util.List;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.impl.DirectoryLookupService;

/**
 * The Service RondRonbin Loadbalancer when lookupInstance.
 * 
 * @author zuxiang
 *
 */
public class ServiceRRLoadBalancer extends RoundRobinLoadBalancer {

	/**
	 * The service name.
	 */
	private final String serviceName ;
	
	/**
	 * The constructor.
	 * 
	 * @param lookupService
	 * 		the DirectoryLookupService.
	 * @param serviceName
	 * 		the service name.
	 */
	public ServiceRRLoadBalancer(DirectoryLookupService lookupService, String serviceName) {
		super(lookupService);
		this.serviceName = serviceName;
	}
	
	/**
	 * Get the service name.
	 * 
	 * @return
	 * 		the service name.
	 */
	public String getServiceName(){
		return serviceName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ModelServiceInstance> getServiceInstanceList() {
		return getLookupService().getUPModelInstances(serviceName);
	}

}
