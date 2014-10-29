package com.cisco.oss.foundation.directory.entity;

public enum EventType {
	Server(0),
	Client(1),
	Watcher(2),
	;
	
	
	private int code;
	
	EventType(int code){
		this.code = code;
	}

	public static EventType valueOf(int type) {
//		switch(type){
//		case 0:
//			return None;
//		case 1:
//			return ServiceInstanceCreated;
//		case 2:
//			return ServiceInstanceDeleted;
//		case 3: 
//			return ServiceInstanceChanged;
//		case 4:
//			return ServiceInstanceStatusChanged;
//		case 5:
//			return ServiceInstanceURIChanged;
//		case 6:
//			return ServiceInstanceMetadataChanged;
//		case 7:
//			return ServiceInstanceAndMetadataChanged;
//			default:
//				return null;
//		}
		return null;
	}

	public int getIntValue() {
		// TODO Auto-generated method stub
		return code;
	}
}
