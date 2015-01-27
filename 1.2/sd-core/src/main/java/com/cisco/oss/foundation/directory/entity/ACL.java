/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.entity;

/**
 * The Access Control List for the Authentication.
 *
 * We support ACL for the user and ServiceInstance.
 * The ACL for user use to authenticate operate.
 * The ACL for ServiceInstance use to authenticate data.
 *
 * And the ACL support different AuthScheme, now we only support user name/password auth.
 *
 * @author zuxiang
 *
 */
public class ACL {
    /**
     * The Permission.
     */
    private int permission;

    /**
     * The AuthScheme.
     */
    public AuthScheme scheme;

    /**
     * The Identity.
     */
    private String id;

    /**
     * Constructor.
     */
    public ACL() {
    }

    /**
     * The constructor.
     *
     * @param scheme
     *         the AuthScheme.
     * @param id
     *         the identity.
     * @param permission
     *         the permission.
     */
    public ACL(AuthScheme scheme, String id, int permission ) {
        this.permission = permission;
        this.scheme = scheme;
        this.id = id;
    }

    /**
     * Get the permission.
     *
     * @return
     *         the permission.
     */
    public int getPermission() {
        return permission;
    }

    /**
     * Set the permission.
     *
     * @param permission
     *         the permission.
     */
    public void setPermission(int permission) {
        this.permission = permission;
    }

    /**
     * Get the AuthScheme.
     *
     * @return
     *         the AuthScheme
     */
    public AuthScheme getScheme() {
        return scheme;
    }

    /**
     * Set the AuthScheme.
     *
     * @param scheme
     *         the AuthScheme.
     */
    public void setScheme(AuthScheme scheme) {
        this.scheme = scheme;
    }

    /**
     * Get the identity.
     *
     * @return
     *         the identity.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the identity.
     *
     * @param id
     *         the identity.
     */
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString(){
        return "{scheme=" + scheme.name() + ",id=" + id + "}";
    }

}
