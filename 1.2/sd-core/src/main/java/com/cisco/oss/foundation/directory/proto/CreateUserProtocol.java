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

import com.cisco.oss.foundation.directory.entity.User;

/**
 * The Create User Protocol.
 *
 * @author zuxiang
 *
 */
public class CreateUserProtocol extends Protocol {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The User object.
     */
    private User user;

    /**
     * The User password.
     */
    private byte[] password;

    public byte[] getPassword() {
        return password;
    }
    public void setPassword(byte[] password) {
        this.password = password;
    }
    public CreateUserProtocol(){

    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public CreateUserProtocol(User user, byte[] password){
        this.user = user;
        this.password = password;
    }

}
