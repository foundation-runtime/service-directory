/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.ClientType;

/**
 * SD API connect Protocol.
 *
 * @author zuxiang
 *
 */
public class ConnectProtocol extends Protocol {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * protocolVersion.
     */
    private int protocolVersion;

    /**
     * Last server xid seen.
     */
    private long lastDxidSeen;

    /**
     * Sesion timeout.
     */
    private int timeOut;

    /**
     * Session id.
     */
    private String sessionId;

    /**
     * Session password.
     */
    private byte[] passwd;

    /**
     * SD API ClientType.
     */
    private ClientType clientType;

    // Auth data for the Directory Authentication.
    /**
     * User name.
     */
    private String userName;

    /**
     * The password.
     */
    private byte[] secret;

    /**
     * Whether the secret obfuscated.
     */
    private boolean obfuscated;

    public ConnectProtocol() {
    }

    public ConnectProtocol(int protocolVersion, long lastDxidSeen, int timeOut,
            String sessionId, byte[] passwd, String userName, byte[] secret, boolean obfuscated) {
        this.protocolVersion = protocolVersion;
        this.lastDxidSeen = lastDxidSeen;
        this.timeOut = timeOut;
        this.sessionId = sessionId;
        this.passwd = passwd;
        this.userName = userName;
        this.secret = secret;
        this.obfuscated = obfuscated;
        setClientType(ClientType.WEBSOCKET);
    }

    public long getLastDxidSeen() {
        return lastDxidSeen;
    }

    public void setLastDxidSeen(long lastDxidSeen) {
        this.lastDxidSeen = lastDxidSeen;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public byte[] getPasswd() {
        return passwd;
    }

    public void setPasswd(byte[] passwd) {
        this.passwd = passwd;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
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

    public void setSecret(byte[] secret) {
        this.secret = secret;
    }

    public boolean isObfuscated() {
        return obfuscated;
    }

    public void setObfuscated(boolean obfuscated) {
        this.obfuscated = obfuscated;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }


}
