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
 * The ProtocolType enum.
 *
 * @author zuxiang
 *
 */
public enum ProtocolType {
    /**
     * Empty Protocol.
     */
    None(0),

    /**
     * CreateSession Protocol.
     */
    CreateSession(1),

    /**
     * RegisterServiceInstance Protocol.
     */
    RegisterServiceInstance(2),

    /**
     * UpdateServiceInstance protocol.
     */
    UpdateServiceInstance(3),
    UpdateServiceInstanceStatus(4),
    UpdateServiceInstanceUri(11),
    CloseSession(5),
    Ping(6),
    Auth(7),
    Sasl(8),
    UnregisterServiceInstance(9),
    GetService(10),
    GetMetadata(12),
    GetUser(13),
    GetAllUser(14),
    CreateUser(15),
    UpdateUser(16),
    DeleteUser(17),
    SetACL(18),
    GetACL(19),
    GetAllACL(20),
    SetUserPassword(21),
    GetAllServices(22),

    GetServiceChangingByTime(23),
    GetMetadataChangingByTime(24),
    GetServiceChangingByVersion(25),
    GetMetadataChangingByVersion(26),
    AttachSession(27),
    UpdateServiceInstanceInternalStatus(28),
    GetServiceInstance(29),
    QueryService(30),
    ;


    private int id = 0;
    ProtocolType(int id){
        this.id = id;
    }

    public int getId(){
        return this.id;
    }

    public static ProtocolType valueOf(int id){
        if(id >= 0){
            for(ProtocolType t : values()){
                if(t.id == id){
                    return t;
                }
            }
        }
        return null;
    }
}

