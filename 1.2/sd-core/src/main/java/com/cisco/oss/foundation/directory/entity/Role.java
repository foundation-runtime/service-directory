/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The common Role used in ServiceDirectory.
 *
 * @author zuxiang
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
