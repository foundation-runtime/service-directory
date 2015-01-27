/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.proto;

/**
 * Set User password Protocol.
 *
 * @author zuxiang
 *
 */
public class SetUserPasswordProtocol extends Protocol{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * User name.
     */
    private String userName;

    /**
     * User secret.
     */
    private byte[] secret;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public byte[] getSecret() {
        return secret;
    }

    public void setSecret(byte[] secret) {
        this.secret = secret;
    }

    public SetUserPasswordProtocol(){

    }

    public SetUserPasswordProtocol(String userName, byte[] secret){
        this.userName = userName;
        this.secret = secret;
    }
}
