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




package com.cisco.oss.foundation.directory.query;

import java.util.Map;

/**
 * The ModelServiceInstance QueryCriterion interface.
 *
 * The QueryCriterion provides a method to check whether a ModelServiceInstance satisfies the criterion or not.
 *
 * @author zuxiang
 *
 */
public interface QueryCriterion {

    /**
     * Filter the metadata Map of ServiceInstance.
     *
     * If it satisfies the query criterion, then true is returned, otherwise false is returned.
     *
     * @param metadataMap
     *         the metadata Map of ServiceInstance.
     * @return
     *         true if matched against the QueryCriterion.
     */
    public boolean isMatch(Map<String, String> metadataMap);

    /**
     * Get the metadata key String that the QueryCriterion validate against.
     *
     * @return
     *         the metadata key String.
     */
    public String getMetadataKey();

}
