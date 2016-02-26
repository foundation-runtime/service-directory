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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.cisco.oss.foundation.directory.entity.ServiceInstance;

/**
 * A RoundRobin LoadBalancer abstract template based on DirectoryLookupService.
 *
 */
public class RoundRobinLoadBalancer implements ServiceInstanceLoadBalancer{

   /**
     * The Round Robin position index.
     */
    private final AtomicInteger index;

    /**
     * Constructor.
     */
    public RoundRobinLoadBalancer(){
        this.index = new AtomicInteger(0);
    }

    /**
     * Vote a ServiceInstance based on the RoundRobin LoadBalancer algorithm.
     *
     * @return
     *         the voted ServiceInstance.
     */
    @Override
    public ServiceInstance vote(List<ServiceInstance> instances) {
        if (instances==null||instances.isEmpty()){
            return null;
        }
        int i = index.getAndIncrement();
        int pos = i % instances.size();
        ServiceInstance instance = instances.get(pos);
        return instance;
    }

}
