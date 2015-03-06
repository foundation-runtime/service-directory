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
 * The ServiceDirectory EventType.
 *
 * @author zuxiang
 *
 */
public enum EventType {
    /**
     * The Server Event.
     */
    Server(0),

    /**
     * The Client Event.
     */
    Client(1),

    /**
     * The Watcher Event.
     */
    Watcher(2),
    ;

    /**
     * The event code.
     */
    private int code;

    /**
     * Constructor.
     *
     * @param code
     *         the event code.
     */
    EventType(int code){
        this.code = code;
    }

    /**
     * Get the event code.
     *
     * @return
     *         the event code.
     */
    public int getCode() {
        return code;
    }
}
