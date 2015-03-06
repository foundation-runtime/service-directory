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
 * The immutable Object of Info to distribute.
 *
 * @author zuxiang
 *
 */
public class BaseInfo {
    /**
     * The create transaction Xid.
     */
    private long createXid;

    /**
     * The modify transaction Xid.
     */
    private long modifyXid;

    /**
     * The create time.
     */
    private long createTime;

    /**
     * The latest modification time.
     */
    private long modifyTime;

    /**
     * Object version number.
     */
    private long version;

    /**
     * Constructor.
     */
    public BaseInfo(){};

    /**
     * Constructor.
     *
     * @param createXid
     *         create xid.
     * @param modifyXid
     *         latest modify xid.
     * @param createTime
     *         the create time.
     * @param modifyTime
     *         the latest modify time.
     * @param version
     *         the version.
     */
    public BaseInfo(long createXid, long modifyXid, long createTime,
            long modifyTime, long version) {
        this.createXid = createXid;
        this.modifyXid = modifyXid;
        this.createTime = createTime;
        this.modifyTime = modifyTime;
        this.version = version;
    }

    /**
     * Get the create xid.
     *
     * @return
     *         the create xid.
     */
    public long getCreateXid() {
        return createXid;
    }

    /**
     * Get the latest modify xid.
     *
     * @return
     *         the latest modify xid.
     */
    public long getModifyXid() {
        return modifyXid;
    }

    /**
     * Get create time.
     *
     * @return
     *         the create time.
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * Get latest modify time.
     *
     * @return
     *         the latest modify time.
     */
    public long getModifyTime() {
        return modifyTime;
    }

    /**
     * Get the version.
     *
     * @return
     *         the version.
     */
    public long getVersion() {
        return version;
    }
}
