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


/**
 * LoadBalancerManager interface for the service lookup.
 *
 */
public class LoadBalancerManager {

    private final RoundRobinLoadBalancer roundRobinLoadBalancer;


    /**
     * Default Constructor
     */
    public LoadBalancerManager() {
       roundRobinLoadBalancer = new RoundRobinLoadBalancer();
    }

    /**
     * Get the Round Robin load balancer
     * @return RoundRobinLoadBalancer
     */
    public RoundRobinLoadBalancer getRoundRobinLoadBalancer(){
        return this.roundRobinLoadBalancer;
    }

    /**
     * The default load Balancer which is used by {@link com.cisco.oss.foundation.directory.lookup.LookupManagerImpl}
     * @return ServiceInstanceLoadBalancer
     */
    public ServiceInstanceLoadBalancer getDefaultLoadBalancer(){
        return getRoundRobinLoadBalancer();
    }


}
