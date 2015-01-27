/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.entity;

/**
 * The Permission enum supported in ServiceDirectory.
 *
 * @author zuxiang
 *
 */
public enum Permission {
    /**
     * No permission.
     */
    NONE(0),
    /**
     * Permission to lookup ServiceInstance.
     */
    READ(1),
    /**
     * Permission to update ServiceInstance.
     */
    WRITE(2),

    /**
     * Permission to register ServiceInstance.
     */
    CREATE(4),

    /**
     * Permission to unregister ServiceInstance.
     */
    DELETE(8),

    /**
     * Permission of createUser, setACL.
     */
    ADMIN(16);

    /**
     * The permission id.
     */
    private int id;

    Permission(int id){
        this.id = id;
    }

    /**
     * Get the id.
     *
     * @return
     *         the id.
     */
    public int getId() {
        return id;
    }

}
