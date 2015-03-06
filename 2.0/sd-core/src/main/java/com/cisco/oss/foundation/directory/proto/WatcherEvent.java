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
package com.cisco.oss.foundation.directory.proto;

import java.util.List;

import com.cisco.oss.foundation.directory.entity.EventType;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.WatchedService;

/**
 * The WatcherEvent that directory server push to SD API.
 *
 * @author zuxiang
 *
 */
public class WatcherEvent extends Event {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The WatchedService list.
     */
    private List<WatchedService> services;

    /**
     * The touched ServiceInstance list.
     */
    private List<ModelServiceInstance> serviceInstances;

    public WatcherEvent(){
        super(EventType.Watcher);
    }
    public WatcherEvent(List<WatchedService> services, List<ModelServiceInstance> instances) {
        super(EventType.Watcher);
        this.serviceInstances = instances;
        this.services = services;
    }

    public List<WatchedService> getServices() {
        return services;
    }
    public void setServices(List<WatchedService> services) {
        this.services = services;
    }
    public List<ModelServiceInstance> getServiceInstances() {
        return serviceInstances;
    }

    public void setServiceInstances(List<ModelServiceInstance> serviceInstances) {
        this.serviceInstances = serviceInstances;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("{");
        sb.append("services=");
        if(services == null){
            sb.append("null");
        }else{
            sb.append("[");
            for(WatchedService o : services){
                sb.append(o.toString()).append(",");
            }
            sb.append("]");
        }
        sb.append(",serviceInstances=");
        if(serviceInstances == null){
            sb.append("null");
        }else{
            sb.append("[");
            if(serviceInstances.size() > 0){
                for(ModelServiceInstance o : serviceInstances){
                    if(o != null){
                        sb.append(o.toString()).append(",");
                    }
                }
            }
            sb.append("]");
        }

        sb.append("}");
        return sb.toString();
    }

}
