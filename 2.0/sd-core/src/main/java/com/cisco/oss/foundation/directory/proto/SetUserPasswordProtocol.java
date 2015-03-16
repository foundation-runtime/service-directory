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
 * Set User password Protocol.
 *
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
