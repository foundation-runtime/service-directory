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

/**
 * OperationalStatus of the ServiceInstance.
 *
 * @author zuxiang
 *
 */
public enum OperationalStatus {

    /**
     * UP will be consumed in Service Consumer.
     */
    UP("UP"),

    /**
     * The dead ServiceInstance, will be deleted automatically after a while.
     */
    DOWN("DOWN");

    /**
     * the status name.
     */
    private String name;

    /**
     * Constructor.
     *
     * @param name
     *            the Status name.
     */
    private OperationalStatus(String name) {
        this.name = name;
    }

    /**
     * Get the OperationalStatus name.
     *
     * @return the name.
     */
    public String getName() {
        return this.name;
    }
}
