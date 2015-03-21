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
 * LoadBalancerManager interface for the service lookup.
 *
 */
public class LoadBalancerManager {

    /**
     * The loadbalancer map for Services.
     */
    @Deprecated
    private final List<ServiceRRLoadBalancer> svcLBList;

    @Deprecated
    private final List<MetadataQueryRRLoadBalancer> metaQueryLBList;

    @Deprecated
    private final List<ServiceQueryRRLoadBalancer> svcQueryLBList;

    @Deprecated
    private final DirectoryLookupService lookupService;

    private final RoundRobinLoadBalancer roundRobinLoadBalancer;

    /**
     * Constructor.
     *
     * @param lookupService
     *         the DirectoryLookupService
     */
    @Deprecated
    public LoadBalancerManager(DirectoryLookupService lookupService) {
        //TODO remove the constructor
        svcLBList = new ArrayList<ServiceRRLoadBalancer>();
        metaQueryLBList = new ArrayList<MetadataQueryRRLoadBalancer>();
        svcQueryLBList = new ArrayList<ServiceQueryRRLoadBalancer>();
        this.lookupService = lookupService;
        this.roundRobinLoadBalancer = new RoundRobinLoadBalancer();
    }

    /**
     * Default Constructor
     */
    public LoadBalancerManager() {
        //TODO remove those deprecated fields
        svcLBList = new ArrayList<ServiceRRLoadBalancer>();
        metaQueryLBList = new ArrayList<MetadataQueryRRLoadBalancer>();
        svcQueryLBList = new ArrayList<ServiceQueryRRLoadBalancer>();
        lookupService = null;
        roundRobinLoadBalancer = new RoundRobinLoadBalancer();
    }

    /**
     * Get the Round Robin load balancer
     * @return
     */
    public RoundRobinLoadBalancer getRoundRobinLoadBalancer(){
        return this.roundRobinLoadBalancer;
    }

    /**
     * The default load Balancer which is used by {@link com.cisco.oss.foundation.directory.impl.LookupManagerImpl}
     * @return
     */
    public ServiceInstanceLoadBalancer getDefaultLoadBalancer(){
        return getRoundRobinLoadBalancer();
    }

    /**
     * Get the Round Robin load balancer for named service.
     *
     * @param serviceName
     *         the service name.
     * @return
     *         the ServiceRRLoadBalancer.
     */
    @Deprecated
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

    /**
     * Get the metadata query Round Robin load balancer for a ServiceInstanceQuery.
     *
     * @param query
     *         the ServiceInstanceQuery.
     * @return
     *         the MetadataQueryRRLoadBalancer.
     */
    @Deprecated
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

    /**
     * Get the service query Round Robin load balancer for a ServiceInstanceQuery.
     *
     * @param serviceName
     *         the service name.
     * @param query
     *         the ServiceInstanceQuery.
     * @return
     *         the ServiceQueryRRLoadBalancer.
     */
    @Deprecated
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
