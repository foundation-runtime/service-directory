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
package com.cisco.oss.foundation.directory.event;

/**
 * The enum for the DirectoryConnection connect status.
 *
 *
 */
public enum ConnectionStatus {
    /**
     * The new created.
     */
    NEW,

    /**
     * Connected.
     */
    CONNECTED,

    /**
     * Connected with readonly.
     */
    CONNECTEDREADONLY,

    /**
     * Closed connection.
     */
    CLOSED,

    /**
     * Authentication failed.
     */
    AUTH_FAILED,

    /**
     * Not connected, it is connecting.
     */
    NOT_CONNECTED;

    /**
     * Whether the Connection is alive.
     *
     * @return
     *         true for alive.
     */
    public boolean isAlive() {
        return this != CLOSED;
    }

    /**
     * Whether the connected.
     *
     * @return
     *         true for connected.
     */
    public boolean isConnected() {
        return this == CONNECTED || this == CONNECTEDREADONLY;
    }
}
