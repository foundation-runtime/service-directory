/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.User;

/**
 * The Update user protocol.
 *
 * @author zuxiang
 *
 */
public class UpdateUserProtocol extends Protocol {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The User.
     */
    private User user;

    public UpdateUserProtocol(){

    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public UpdateUserProtocol(User user){
        this.user = user;
    }

}
