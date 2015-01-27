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

/**
 * The LoadBalance Manager for lookup Service.
 *
 * @author zuxiang
 *
 */
public class LoadBalancerManager {

    /**
     * The loadbalancer map for Services.
     */
    private final List<ServiceRRLoadBalancer> svcLBList;

    /**
     * The Loadbalancer for the meta query.
     */
    private final List<MetadataQueryRRLoadBalancer> metaQueryLBList;

    /**
     * The loadbalancer for the service query.
     */
    private final List<ServiceQueryRRLoadBalancer> svcQueryLBList;

    /**
     * The Directory LookupService.
     */
    private final DirectoryLookupService lookupService;

    /**
     * Constructor.
     *
     * @param lookupService
     *         the DirectoryLookupService.
     */
    public LoadBalancerManager(DirectoryLookupService lookupService) {
        svcLBList = new ArrayList<ServiceRRLoadBalancer>();
        metaQueryLBList = new ArrayList<MetadataQueryRRLoadBalancer>();
        svcQueryLBList = new ArrayList<ServiceQueryRRLoadBalancer>();
        this.lookupService = lookupService;
    }

    /**
     * Get the ServiceRRLoadBalancer.
     *
     * @param serviceName
     *         the service name.
     * @return
     *         the ServiceRRLoadBalancer.
     */
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

    /**
     * Get the MetadataQueryRRLoadBalancer.
     *
     * @param query
     *         the ServiceInstanceQuery.
     * @return
     *         the MetadataQueryRRLoadBalancer.
     */
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

    /**
     * Get the ServiceQueryRRLoadBalancer.
     *
     * @param serviceName
     *         the service name.
     * @param query
     *         the ServiceInstanceQuery.
     * @return
     *         the ServiceQueryRRLoadBalancer.
     */
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
