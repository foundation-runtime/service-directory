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
package com.cisco.oss.foundation.directory.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * The WatchedService for the ServiceInstanceEvent.
 *
 *
 */
public class WatchedService{
    /**
     * The ModelService of the Service.
     */
    private ModelService service;

    /**
     * The ServiceInstanceEvent list.
     */
    private List<ServiceInstanceEvent> serviceInstanceEvents;

    /**
     * Constructor.
     */
    public WatchedService(){};

    /**
     * Constructor.
     *
     * @param service
     *         the ModelService object.
     */
    public WatchedService(ModelService service){
        this.service = service;
        serviceInstanceEvents = new ArrayList<ServiceInstanceEvent>();
    }

    /**
     * get the ModelService.
     *
     * @return
     *         the ModelService.
     */
    public ModelService getService() {
        return service;
    }

    /**
     * Set the ModelService.
     *
     * @param service
     *         the ModelService
     */
    public void setService(ModelService service) {
        this.service = service;
    }

    /**
     * Get the ServiceInstanceEvent list.
     * @return
     *         the ServiceInstanceEvent list.
     */
    public List<ServiceInstanceEvent> getServiceInstanceEvents() {
        return serviceInstanceEvents;
    }

    /**
     * Set the ServiceInstanceEvent list.
     *
     * @param serviceInstanceEvents
     *         the ServiceInstanceEvent list.
     */
    public void setServiceInstanceEvents(List<ServiceInstanceEvent> serviceInstanceEvents) {
        this.serviceInstanceEvents = serviceInstanceEvents;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("{");
        sb.append(",service=").append(service.getName()).append(",serviceInstanceEvents=[");
        if(serviceInstanceEvents != null && serviceInstanceEvents.size() > 0){
            for(ServiceInstanceEvent i : serviceInstanceEvents){
                sb.append(i).append(",");
            }
        }
        sb.append("]}");
        return sb.toString();
    }
}
