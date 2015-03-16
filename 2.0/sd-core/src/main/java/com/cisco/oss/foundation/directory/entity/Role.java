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
import java.util.Arrays;
import java.util.List;

/**
 * The common Role used in ServiceDirectory.
 *
 *
 */
public enum Role {

    /**
     * The READ role.
     */
    READ(Permission.READ),

    /**
     * The WRITE role.
     */
    WRITE(Permission.READ, Permission.WRITE),

    /**
     * The CREATE role.
     */
    CREATE(Permission.READ, Permission.WRITE, Permission.CREATE),

    /**
     * The DELETE role.
     */
    DELETE(Permission.READ, Permission.WRITE, Permission.DELETE),

    /**
     * The ADMIN role.
     */
    ADMIN(Permission.READ, Permission.WRITE, Permission.DELETE, Permission.CREATE, Permission.ADMIN),
    ;

    /**
     * The permission list of the role.
     */
    private Permission[] permission;

    /**
     * Constructor.
     *
     * @param permission
     *         the permission list.
     */
    Role(Permission... permission){
        this.permission = permission;
    }

    /**
     * Get the permission list.
     *
     * @return
     *         the permission list.
     */
    public List<Permission> getPermission() {
        ArrayList<Permission> list = new ArrayList<Permission>();
        list.addAll(Arrays.asList(permission));
        return list;
    }

}
