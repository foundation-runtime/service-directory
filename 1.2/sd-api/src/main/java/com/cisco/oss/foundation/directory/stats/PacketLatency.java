package com.cisco.oss.foundation.directory.stats;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.connect.DirectoryConnection.Packet;



public class PacketLatency {
	private final static Logger LOGGER = LoggerFactory.getLogger(PacketLatency.class);
	private static boolean isEnabled = true;
	
	public static boolean isEnalbed(){
		return isEnabled;
	}
	
	public static void initPacket(Packet packet){
		if(isEnabled){
			packet.setCreateTime(System.currentTimeMillis());
		}
	}
	
	public static void queuePacket(Packet packet){
		if(isEnabled){
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("queue packet latency = " + (System.currentTimeMillis() - packet.getCreateTime()));
			}
		}
	}
	
	public static void sendPacket(Packet packet){
		if(isEnabled){
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("send packet latency = " + (System.currentTimeMillis() - packet.getCreateTime()));
			}
		}
	}
	
	public static void receivePacket(Packet packet){
		if(isEnabled){
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("receive packet latency = " + (System.currentTimeMillis() - packet.getCreateTime()));
			}
		}
	}
	
	public static void finishPacket(Packet packet){
		if(isEnabled){
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("finish packet latency = " + (System.currentTimeMillis() - packet.getCreateTime()));
			}
		}
	}
	
	public static void callbackPacket(Packet packet){
		if(isEnabled){
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("callback packet latency = " + (System.currentTimeMillis() - packet.getCreateTime()));
			}
		}
	}
}
