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




package com.cisco.oss.foundation.directory.impl;

import java.util.ArrayList;
import java.util.List;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.query.QueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;

/**
 * The helper class to do the ServiceInstances filter against the QueryCriterion.
 *
 *
 */
public class ServiceInstanceQueryHelper {

    /**
     * Filter the ModelServiceInstance list against the ServiceInstanceQuery.
     *
     * @param query
     *         the ServiceInstanceQuery matchers.
     * @param list
     *         the ModelServiceInstance list.
     * @return
     *         the matched ModelServiceInstance list.
     */
    public static List<ModelServiceInstance> filter(ServiceInstanceQuery query, List<ModelServiceInstance> list) {

        List<ModelServiceInstance> instances = new ArrayList<ModelServiceInstance>();
        for (ModelServiceInstance instance : list) {
            boolean passed = true;
            for (QueryCriterion criterion : query.getCriteria()) {
                if (criterion.isMatch(instance.getMetadata()) == false) {
                    passed = false;
                    break;
                }
            }
            if (passed) {
                instances.add(instance);
            }
        }
        return instances;

    }

    /**
     * Filter the ServiceInstance list against the ServiceInstanceQuery.
     *
     * @param query
     *         the ServiceInstanceQuery matchers.
     * @param list
     *         the ServiceInstance list.
     * @return
     *         the matched ServiceInstance list.
     */
    public static List<ServiceInstance> filterServiceInstance(ServiceInstanceQuery query, List<ServiceInstance> list) {

        List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
        for (ServiceInstance instance : list) {
            boolean passed = true;
            for (QueryCriterion criterion : query.getCriteria()) {
                if (criterion.isMatch(instance.getMetadata()) == false) {
                    passed = false;
                    break;
                }
            }
            if (passed) {
                instances.add(instance);
            }
        }
        return instances;
    }
}
