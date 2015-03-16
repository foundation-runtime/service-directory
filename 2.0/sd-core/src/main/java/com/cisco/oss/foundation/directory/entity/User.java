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

import java.util.List;

/**
 * The User Object.
 *
 *
 */
public class User {
    /**
     * the user name.
     */
    private String name;

    /**
     * The user permission list.
     */
    private List<Permission> permission;

    /**
     * Constructor.
     */
    public User(){

    }

    /**
     * Constructor.
     *
     * @param name
     *         the user name.
     * @param permission
     *         the permission list.
     */
    public User(String name, List<Permission> permission){
        this.setName(name);
        this.permission = permission;
    }

    /**
     * Get the user name.
     *
     * @return
     *         the user name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the user name.
     *
     * @param name
     *         user name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the user permission list.
     *
     * @return
     *         the user permission list.
     */
    public List<Permission> getPermission() {
        return permission;
    }

    /**
     * Set the user permission list.
     *
     * @param permission
     *         the user permission lit.
     */
    public void setPermission(List<Permission> permission) {
        this.permission = permission;
    }
}
