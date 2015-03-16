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

import com.cisco.oss.foundation.directory.entity.EventType;

/**
 * The Event Response that DirectoryServer push to SD API client.
 *
 *
 */
public class Event extends Response{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The EventType.
     */
    private EventType type;

    /**
     * Constructor.
     *
     * @param type
     *         the EventType.
     */
    public Event(EventType type){
        this.setEventType(type);
    }

    /**
     * Get the EventType.
     *
     * @return
     *         the EventType.
     */
    public EventType getEventType() {
        return type;
    }

    /**
     * Set the EventType.
     *
     * @param type
     *         the EventType.
     */
    public void setEventType(EventType type) {
        this.type = type;
    }



}
