package com.cisco.oss.foundation.directory.lb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.impl.DirectoryLookupService;
import com.cisco.oss.foundation.directory.impl.ServiceInstanceQueryHelper;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;

public class ServiceQueryRRLoadBalancer extends RoundRobinLoadBalancer {

	private final String serviceName ;
	private final ServiceInstanceQuery query;
	public ServiceQueryRRLoadBalancer(DirectoryLookupService lookupService, String serviceName, ServiceInstanceQuery query) {
		super(lookupService);
		this.serviceName = serviceName;
		this.query = query;
	}
	
	public String getServiceName(){
		return serviceName;
	}
	
	public ServiceInstanceQuery getServiceInstanceQuery(){
		return query;
	}

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
