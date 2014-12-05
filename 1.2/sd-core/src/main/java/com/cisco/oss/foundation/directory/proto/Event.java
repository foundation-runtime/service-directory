/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.EventType;

/**
 * The Event Response that DirectoryServer push to SD API client.
 * 
 * @author zuxiang
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
	 * 		the EventType.
	 */
	public Event(EventType type){
		this.setEventType(type);
	}

	/**
	 * Get the EventType.
	 * 
	 * @return
	 * 		the EventType.
	 */
	public EventType getEventType() {
		return type;
	}

	/**
	 * Set the EventType.
	 * 
	 * @param type
	 * 		the EventType.
	 */
	public void setEventType(EventType type) {
		this.type = type;
	}
	
	

}
