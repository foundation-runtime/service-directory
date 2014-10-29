package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.EventType;



public class Event extends Response{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private EventType type;
	
	public Event(EventType type){
		this.setEventType(type);
	}

	public EventType getEventType() {
		return type;
	}

	public void setEventType(EventType type) {
		this.type = type;
	}
	
	

}
