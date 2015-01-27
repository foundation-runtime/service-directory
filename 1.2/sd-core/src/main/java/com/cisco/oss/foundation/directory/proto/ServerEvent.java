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
import com.cisco.oss.foundation.directory.entity.ServerStatus;

/**
 * The Directory Server Event push to SD API.
 *
 * @author zuxiang
 *
 */
public class ServerEvent extends Event{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public ServerEvent(){
        super(EventType.Server);
    }

    /**
     * The ServerStatus change event.
     *
     * @author zuxiang
     *
     */
    public static class ServerStatusEvent extends ServerEvent{

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /**
         * The current ServerStatue.
         */
        private ServerStatus serverStatus;

        /**
         * Constructor.
         */
        public ServerStatusEvent(){

        }

        /**
         * Constructor.
         *
         * @param serverStatus
         *         The current ServerStatue.
         */
        public ServerStatusEvent(ServerStatus serverStatus){
            super();
            this.setServerStatus(serverStatus);
        }

        /**
         * Get The current ServerStatue.
         *
         * @return
         *         The current ServerStatue.
         */
        public ServerStatus getServerStatus() {
            return serverStatus;
        }

        /**
         * Set The current ServerStatue.
         *
         * @param serverStatus
         *         The current ServerStatue.
         */
        public void setServerStatus(ServerStatus serverStatus) {
            this.serverStatus = serverStatus;
        }
    }

    /**
     * Sever ask SD API Close Session event.
     *
     * @author zuxiang
     *
     */
    public static class CloseSessionEvent extends ServerEvent {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        /**
         * the extra message.
         */
        private String message;

        /**
         * Get extra message.
         *
         * @return
         *         the extra message.
         */
        public String getMessage() {
            return message;
        }

        /**
         * Set the extra message.
         *
         * @param message
         *         the extra message.
         */
        public void setMessage(String message) {
            this.message = message;
        }

        /**
         * Constructor.
         */
        public CloseSessionEvent(){

        }

        /**
         * Constructor.
         *
         * @param message
         *         the extra message.
         */
        public CloseSessionEvent(String message){
            this.message = message;
        }


    }


}
