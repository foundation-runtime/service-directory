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
