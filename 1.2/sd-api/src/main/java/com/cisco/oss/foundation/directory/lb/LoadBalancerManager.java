package com.cisco.oss.foundation.directory.lb;

import java.util.ArrayList;
import java.util.List;

import com.cisco.oss.foundation.directory.impl.DirectoryLookupService;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;

public class LoadBalancerManager {

	/**
	 * The loadbalancer map for Services.
	 */
	private final List<ServiceRRLoadBalancer> svcLBList;

	private final List<MetadataQueryRRLoadBalancer> metaQueryLBList;

	private final List<ServiceQueryRRLoadBalancer> svcQueryLBList;

	private final DirectoryLookupService lookupService;

	public LoadBalancerManager(DirectoryLookupService lookupService) {
		svcLBList = new ArrayList<ServiceRRLoadBalancer>();
		metaQueryLBList = new ArrayList<MetadataQueryRRLoadBalancer>();
		svcQueryLBList = new ArrayList<ServiceQueryRRLoadBalancer>();
		this.lookupService = lookupService;
	}

	public ServiceRRLoadBalancer getServiceRRLoadBalancer(String serviceName) {
		for (ServiceRRLoadBalancer lb : svcLBList) {
			if (lb.getServiceName().equals(serviceName)) {
				return lb;
			}
		}
		ServiceRRLoadBalancer lb = new ServiceRRLoadBalancer(lookupService,
				serviceName);
		svcLBList.add(lb);
		return lb;
	}

	public MetadataQueryRRLoadBalancer getMetadataQueryRRLoadBalancer(
			ServiceInstanceQuery query) {
		for (MetadataQueryRRLoadBalancer lb : metaQueryLBList) {
			if (lb.getServiceInstanceQuery().equals(query)) {
				return lb;
			}
		}
		MetadataQueryRRLoadBalancer lb = new MetadataQueryRRLoadBalancer(
				lookupService, query);
		metaQueryLBList.add(lb);
		return lb;
	}

	public ServiceQueryRRLoadBalancer getServiceQueryRRLoadBalancer(
			String serviceName, ServiceInstanceQuery query) {
		for (ServiceQueryRRLoadBalancer lb : svcQueryLBList) {
			if (lb.getServiceName().equals(serviceName)
					&& lb.getServiceInstanceQuery().equals(query)) {
				return lb;
			}
		}
		ServiceQueryRRLoadBalancer lb = new ServiceQueryRRLoadBalancer(
				lookupService, serviceName, query);
		svcQueryLBList.add(lb);
		return lb;
	}
}
