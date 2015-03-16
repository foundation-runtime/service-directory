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
package com.cisco.oss.foundation.directory.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.connect.DirectoryConnection.Packet;

/**
 * Use to collect the Packet latency statistics.
 *
 *
 */
public class PacketLatency {
    private final static Logger LOGGER = LoggerFactory.getLogger(PacketLatency.class);

    /**
     * Indicate whether enabled packet latency collect.
     */
    private static boolean isEnabled = true;

    /**
     * check is enabled.
     *
     * @return
     *         true for enabled.
     */
    public static boolean isEnalbed(){
        return isEnabled;
    }

    /**
     * Init the packet for latency collect.
     *
     * @param packet
     *         the Packet.
     */
    public static void initPacket(Packet packet){
        if(isEnabled){
            packet.setCreateTime(System.currentTimeMillis());
        }
    }

    /**
     * Collect the queue Packet latency.
     *
     * @param packet
     *         the Packet.
     */
    public static void queuePacket(Packet packet){
        if(isEnabled){
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("queue packet latency = " + (System.currentTimeMillis() - packet.getCreateTime()));
            }
        }
    }

    /**
     * Collect the send Packet latency.
     *
     * @param packet
     *         the Packet.
     */
    public static void sendPacket(Packet packet){
        if(isEnabled){
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("send packet latency = " + (System.currentTimeMillis() - packet.getCreateTime()));
            }
        }
    }

    /**
     * Collect the receive Packet latency.
     *
     * @param packet
     *         the Packet.
     */
    public static void receivePacket(Packet packet){
        if(isEnabled){
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("receive packet latency = " + (System.currentTimeMillis() - packet.getCreateTime()));
            }
        }
    }

    /**
     * Collect the finish Packet latency.
     *
     * @param packet
     *         the Packet.
     */
    public static void finishPacket(Packet packet){
        if(isEnabled){
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("finish packet latency = " + (System.currentTimeMillis() - packet.getCreateTime()));
            }
        }
    }

    /**
     * Collect the callback Packet latency.
     *
     * @param packet
     *         the Packet.
     */
    public static void callbackPacket(Packet packet){
        if(isEnabled){
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("callback packet latency = " + (System.currentTimeMillis() - packet.getCreateTime()));
            }
        }
    }
}
