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

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.impl.DirectoryLookupService;

/**
 * The Service RondRonbin Loadbalancer when lookupInstance.
 *
 *
 */
public class ServiceRRLoadBalancer extends RoundRobinLoadBalancer {

    /**
     * The service name.
     */
    private final String serviceName ;

    /**
     * The constructor.
     *
     * @param lookupService
     *         the DirectoryLookupService.
     * @param serviceName
     *         the service name.
     */
    public ServiceRRLoadBalancer(DirectoryLookupService lookupService, String serviceName) {
        super(lookupService);
        this.serviceName = serviceName;
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
     * {@inheritDoc}
     */
    @Override
    public List<ModelServiceInstance> getServiceInstanceList() {
        return getLookupService().getUPModelInstances(serviceName);
    }

}
