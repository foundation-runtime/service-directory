/**
 * Copyright 2014 Cisco Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * The service query Round Robin loadbalancer implementation.
 *
 */
public class ServiceQueryRRLoadBalancer extends RoundRobinLoadBalancer {

    private final String serviceName ;
    private final ServiceInstanceQuery query;
    
    /**
     * Constructor.
     *
     * @param lookupService
     *         the DirectoryLookupService
     *         
     * @param serviceName
     *         the service name
     *         
     * @param query
     *         the ServiceInstanceQuery
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
     *         the service name.
     */
    public String getServiceName(){
        return serviceName;
    }

    /**
     * Get the ServiceInstanceQuery.
     *
     * @return
     *         the ServiceInstanceQuery.
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
