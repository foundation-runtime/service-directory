/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.entity;

/**
 * The ServerStatus of the DirectoryServer.
 *
 * @author zuxiang
 *
 */
public enum ServerStatus {
    /**
     * Unknown.
     */
    Unknown (-1),

    /**
     * Server disconnected.
     */
    Disconnected (0),

    /**
     * Sync in connected.
     */
    SyncConnected (3),

    /**
     * Authentication failed.
     */
    AuthFailed (4),

    /**
     * connect in readonly.
     */
    ConnectedReadOnly (5),

    /**
     * Sasl authenticated.
     */
    SaslAuthenticated(6),

    /**
     * expired.
     */
    Expired (-112);

    private final int code;     // Integer representation of value
                                    // for sending over wire

    ServerStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ServerStatus valueOf(int code) {
        switch(code) {
            case   -1: return ServerStatus.Unknown;
            case    0: return ServerStatus.Disconnected;
            case    3: return ServerStatus.SyncConnected;
            case    4: return ServerStatus.AuthFailed;
            case    5: return ServerStatus.ConnectedReadOnly;
            case    6: return ServerStatus.SaslAuthenticated;
            case -112: return ServerStatus.Expired;

            default:
                return null;
        }
    }
}
