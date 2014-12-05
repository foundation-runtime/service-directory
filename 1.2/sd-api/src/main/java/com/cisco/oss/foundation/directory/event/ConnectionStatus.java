/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.event;

/**
 * The enum for the DirectoryConnection connect status.
 * 
 * @author zuxiang
 *
 */
public enum ConnectionStatus {
	/**
	 * The new created.
	 */
	NEW, 
	
	/**
	 * Connected.
	 */
	CONNECTED, 
	
	/**
	 * Connected with readonly.
	 */
	CONNECTEDREADONLY, 
	
	/**
	 * Closed connection.
	 */
	CLOSED, 
	
	/**
	 * Authentication failed.
	 */
	AUTH_FAILED, 
	
	/**
	 * Not connected, it is connecting.
	 */
	NOT_CONNECTED;

	/**
	 * Whether the Connection is alive.
	 * 
	 * @return
	 * 		true for alive.
	 */
	public boolean isAlive() {
		return this != CLOSED;
	}

	/**
	 * Whether the connected.
	 * 
	 * @return
	 * 		true for connected.
	 */
	public boolean isConnected() {
		return this == CONNECTED || this == CONNECTEDREADONLY;
	}
}
