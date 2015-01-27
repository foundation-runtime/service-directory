/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.User;

/**
 * The Create User Protocol.
 *
 * @author zuxiang
 *
 */
public class CreateUserProtocol extends Protocol {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The User object.
     */
    private User user;

    /**
     * The User password.
     */
    private byte[] password;

    public byte[] getPassword() {
        return password;
    }
    public void setPassword(byte[] password) {
        this.password = password;
    }
    public CreateUserProtocol(){

    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public CreateUserProtocol(User user, byte[] password){
        this.user = user;
        this.password = password;
    }

}
