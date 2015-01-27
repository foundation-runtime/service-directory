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
package com.cisco.oss.foundation.directory.proto;

/**
 * The SD API client connect Response.
 *
 * @author zuxiang
 *
 */
public class ConnectResponse extends Response {



    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The ProtocolVersion.
     */
    private int protocolVersion;

    /**
     * The session timeout.
     */
    private int timeOut;

    /**
     * The session id.
     */
    private String sessionId;

    /**
     * The session password.
     */
    private byte[] passwd;

    /**
     * The serverId.
     */
    private int serverId;

    public ConnectResponse() {
    }

    public ConnectResponse(int protocolVersion, int timeOut, String sessionId,
            byte[] passwd, int serverId) {
        this.protocolVersion = protocolVersion;
        this.timeOut = timeOut;
        this.sessionId = sessionId;
        this.passwd = passwd;
        this.serverId = serverId;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
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

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }
}
