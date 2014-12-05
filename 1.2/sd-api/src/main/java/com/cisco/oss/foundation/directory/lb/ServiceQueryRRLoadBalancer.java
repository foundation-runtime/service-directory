/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.lb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.impl.DirectoryLookupService;
import com.cisco.oss.foundation.directory.impl.ServiceInstanceQueryHelper;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;

/**
 * The Service Query RondRonbin Loadbalancer when lookupInstance.
 * 
 * @author zuxiang
 *
 */
public class ServiceQueryRRLoadBalancer extends RoundRobinLoadBalancer {

	/**
	 * The service name.
	 */
	private final String serviceName ;
	
	/**
	 * The ServiceInstanceQuery.
	 */
	private final ServiceInstanceQuery query;
	
	/**
	 * The Constructor.
	 * 
	 * @param lookupService
	 * 		the DirectoryLookupService.
	 * @param serviceName
	 * 		the service name.
	 * @param query
	 * 		the ServiceInstaneQuery.
	 */
	public ServiceQueryRRLoadBalancer(DirectoryLookupService lookupService, String serviceName, ServiceInstanceQuery query) {
		super(lookupService);
		this.serviceName = serviceName;
		this.query = query;
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
	 * Get the ServiceInstanceQuery.
	 * 
	 * @return
	 * 		the ServiceInstanceQuery.
	 */
	public ServiceInstanceQuery getServiceInstanceQuery(){
		return query;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ModelServiceInstance> getServiceInstanceList() {
		List<ModelServiceInstance> modelSvc = getLookupService().getUPModelInstances(serviceName);
		if(modelSvc != null && ! modelSvc.isEmpty()){
			List<ModelServiceInstance> filteredInstances = ServiceInstanceQueryHelper.filter(query, modelSvc);
			if(filteredInstances.size() > 0){
				List<ModelServiceInstance> instances = new ArrayList<ModelServiceInstance>();
				for(ModelServiceInstance model : filteredInstances){
					instances.add(model);
				}
				return instances;
			}
		}
		return Collections.emptyList();
	}

}
