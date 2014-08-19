package com.cisco.vss.foundation.directory.lb;

import java.util.ArrayList;
import java.util.List;

import com.cisco.vss.foundation.directory.impl.DirectoryLookupService;
import com.cisco.vss.foundation.directory.query.ServiceInstanceQuery;

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
		ServiceRRLoadBalancer lb = null;
		synchronized(svcLBList){
			for (ServiceRRLoadBalancer i : svcLBList) {
				if (i.getServiceName().equals(serviceName)) {
					lb = i;
					break;
				}
			}
			if(lb == null){
				lb = new ServiceRRLoadBalancer(lookupService, serviceName);
				svcLBList.add(lb);
			}
		}
		return lb;
	}

	public MetadataQueryRRLoadBalancer getMetadataQueryRRLoadBalancer(
			ServiceInstanceQuery query) {
		MetadataQueryRRLoadBalancer lb = null;
		synchronized(metaQueryLBList){
			for (MetadataQueryRRLoadBalancer i : metaQueryLBList) {
				if (i.getServiceInstanceQuery().equals(query)) {
					lb = i;
					break;
				}
			}
			if (lb == null) {
				lb = new MetadataQueryRRLoadBalancer(lookupService, query);
				metaQueryLBList.add(lb);
			}
		}
		return lb;
	}

	public ServiceQueryRRLoadBalancer getServiceQueryRRLoadBalancer(
			String serviceName, ServiceInstanceQuery query) {
		ServiceQueryRRLoadBalancer lb = null;
		synchronized(svcQueryLBList){
			for (ServiceQueryRRLoadBalancer i : svcQueryLBList) {
				if (i.getServiceName().equals(serviceName)
						&& i.getServiceInstanceQuery().equals(query)) {
					lb = i;
					break;
				}
			}
			if (lb == null) {
				lb = new ServiceQueryRRLoadBalancer(lookupService, serviceName,
						query);
				svcQueryLBList.add(lb);
			}
		}
		return lb;
	}
}
