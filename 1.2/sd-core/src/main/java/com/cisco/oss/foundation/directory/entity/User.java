/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.entity;

import java.util.List;

/**
 * The User Object.
 *
 * @author zuxiang
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
