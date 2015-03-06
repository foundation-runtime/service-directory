/**
 * Copyright 2014 Cisco Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
