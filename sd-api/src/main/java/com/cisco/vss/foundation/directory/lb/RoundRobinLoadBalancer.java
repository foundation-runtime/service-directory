/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.vss.foundation.directory.lb;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.cisco.vss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.vss.foundation.directory.entity.ServiceInstance;
import com.cisco.vss.foundation.directory.impl.DirectoryLookupService;
import com.cisco.vss.foundation.directory.utils.ServiceInstanceUtils;

/**
 * A RoundRobin LoadBalancer abstract template based on DirectoryLookupService.
 * 
 * 
 * 
 * @author zuxiang
 *
 */
public abstract class RoundRobinLoadBalancer implements ServiceInstanceLoadBalancer{
	
	/**
	 * The DirectoryLookupService to get the ServiceInstance List.
	 */
	private final DirectoryLookupService lookupService ;
	
	/**
	 * The Round Robin position index.
	 */
	private AtomicInteger index;
	
	/**
	 * Constructor.
	 */
	public RoundRobinLoadBalancer(DirectoryLookupService lookupService){
		this.lookupService = lookupService;
		this.index = new AtomicInteger(0);
	}
	
	/**
	 * Vote a ServiceInstance based on the LoadBalancer algorithm.
	 * 
	 * @return
	 * 		the voted ServiceInstance.
	 */
	@Override
	public ServiceInstance vote() {
		List<ModelServiceInstance> instances = getServiceInstanceList();
		if(instances == null || instances.isEmpty()){
			return null;
		}
		int i = index.getAndIncrement();
		int pos = i % instances.size();
		ModelServiceInstance instance = instances.get(pos);
		return ServiceInstanceUtils.transferFromModelServiceInstance(instance);
	}
	
	/**
	 * Get the LookupService.
	 * 
	 * @return
	 * 		the LookupService.
	 */
	public DirectoryLookupService getLookupService(){
		return lookupService;
	}
	
	/**
	 * Get the ServiceInstance list for the Round Robin.
	 * 
	 * @return
	 * 		the ServiceInstance list.
	 */
	public abstract List<ModelServiceInstance> getServiceInstanceList();
	
	

}
