/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.AuthScheme;

/**
 * Directory Authenticate Protocol.
 *
 * @author zuxiang
 *
 */
public class DirectoryAuthProtocol extends AuthProtocol {
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

    /**
     * Indicate wheter secret obfuscated.
     */
    private boolean obfuscated;

    public DirectoryAuthProtocol() {
    }

    public DirectoryAuthProtocol(String userName,  byte[] secret, boolean obfuscated) {
        super(AuthScheme.DIRECTORY);
        this.userName = userName;
        this.secret = secret;
        this.setObfuscated(obfuscated);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public byte[] getSecret() {
        return secret;
    }

    public void setSecet(byte[] secret) {
        this.secret = secret;
    }

    public boolean isObfuscated() {
        return obfuscated;
    }

    public void setObfuscated(boolean obfuscated) {
        this.obfuscated = obfuscated;
    }
}
