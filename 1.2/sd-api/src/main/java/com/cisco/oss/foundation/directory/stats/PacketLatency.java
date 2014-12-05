/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.connect.DirectoryConnection.Packet;

/**
 * Use to collect the Packet latency statistics.
 * 
 * @author zuxiang
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
	 * 		true for enabled.
	 */
	public static boolean isEnalbed(){
		return isEnabled;
	}
	
	/**
	 * Init the packet for latency collect.
	 * 
	 * @param packet
	 * 		the Packet.
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
	 * 		the Packet.
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
	 * 		the Packet.
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
	 * 		the Packet.
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
	 * 		the Packet.
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
	 * 		the Packet.
	 */
	public static void callbackPacket(Packet packet){
		if(isEnabled){
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("callback packet latency = " + (System.currentTimeMillis() - packet.getCreateTime()));
			}
		}
	}
}
