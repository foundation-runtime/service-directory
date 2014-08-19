package com.cisco.vss.foundation.directory.lb;

import java.util.Collections;
import java.util.List;

import com.cisco.vss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.vss.foundation.directory.impl.DirectoryLookupService;
import com.cisco.vss.foundation.directory.impl.ServiceInstanceQueryHelper;
import com.cisco.vss.foundation.directory.query.ServiceInstanceQuery;

public class MetadataQueryRRLoadBalancer extends RoundRobinLoadBalancer {

		private final ServiceInstanceQuery query;
		public MetadataQueryRRLoadBalancer(DirectoryLookupService lookupService, ServiceInstanceQuery query) {
			super(lookupService);
			this.query = query;
		}
		
		public ServiceInstanceQuery getServiceInstanceQuery(){
			return query;
		}

		@Override
		public List<ModelServiceInstance> getServiceInstanceList() {
			List<ModelServiceInstance> instances = null; 
			String keyName = null;
			if (query.getCriteria().size() > 0) {
				keyName = query.getCriteria().get(0).getMetadataKey();
			}
			if (keyName != null && !keyName.isEmpty()) {
				List<ModelServiceInstance> modelInstances = getLookupService()
						.getUPModelInstancesByKey(keyName);
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
