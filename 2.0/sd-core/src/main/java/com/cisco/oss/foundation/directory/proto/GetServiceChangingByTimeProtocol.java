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

import java.util.Map;

import com.cisco.oss.foundation.directory.entity.ModelService;

/**
 * Get the Service Change by time Protocol.
 *
 *
 */
public class GetServiceChangingByTimeProtocol extends Protocol {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * the ModelService map.
     */
    private Map<String, ModelService> services;

    public GetServiceChangingByTimeProtocol(){

    }

    public GetServiceChangingByTimeProtocol(Map<String, ModelService> services){
        this.services = services;
    }

    public Map<String, ModelService> getServices() {
        return services;
    }

    public void setServices(Map<String, ModelService> services) {
        this.services = services;
    }


}
