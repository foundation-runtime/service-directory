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

import java.io.Serializable;

/**
 * The ProtocolHeader Object.
 *
 *
 */
public class ProtocolHeader implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The Client xid.
     */
    private int xid;

    /**
     * The ProtocolType.
     */
    private ProtocolType type;

    /**
     * The Protocol CreateTime in ms.
     */
    public long createTime;

    /**
     * Constructor.
     */
    public ProtocolHeader() {
    }

    /**
     * Constructor.
     *
     * @param xid
     *         the Sd API client xid.
     * @param type
     *         the ProtocolType.
     */
    public ProtocolHeader(int xid, ProtocolType type) {
        this.xid = xid;
        this.type = type;
    }

    /**
     * Get the SD API client xid.
     *
     * @return
     *         the SD PAI Client xid.
     */
    public int getXid() {
        return xid;
    }

    /**
     * Set the the SD PAI Client xid.
     *
     * @param xid
     *         the SD PAI Client xid.
     */
    public void setXid(int xid) {
        this.xid = xid;
    }

    /**
     * Get the ProtocolType.
     *
     * @return
     *         The ProtocolType.
     */
    public ProtocolType getType() {
        return type;
    }

    /**
     * Set the ProtocolType.
     *
     * @param type
     *         the ProtocolType.
     */
    public void setType(ProtocolType type) {
        this.type = type;
    }

    @Override
    public String toString(){
        return "{xid=" + xid + ", type=" + type + "}";
    }
}
