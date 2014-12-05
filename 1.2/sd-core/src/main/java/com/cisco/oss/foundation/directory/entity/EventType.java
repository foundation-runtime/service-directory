/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
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
	 * 		the event code.
	 */
	EventType(int code){
		this.code = code;
	}

	/**
	 * Get the event code.
	 * 
	 * @return
	 * 		the event code.
	 */
	public int getCode() {
		return code;
	}
}
