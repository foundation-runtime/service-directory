/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.User;

/**
 * Get User Response.
 *
 * @author zuxiang
 *
 */
public class GetUserResponse extends Response {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The User Object.
     */
    private User user;
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public GetUserResponse(){

    }

    public GetUserResponse(User user){
        this.user = user;
    }
}
