package com.cisco.oss.foundation.directory.proto;

public enum ProtocolType {
	None(0),
	CreateSession(1),
	RegisterServiceInstance(2),
	UpdateServiceInstance(3),
	UpdateServiceInstanceStatus(4),
	UpdateServiceInstanceUri(11),
	CloseSession(5),
	Ping(6),
	Auth(7),
	Sasl(8),
	UnregisterServiceInstance(9),
	GetService(10),
	GetMetadata(12),
	GetUser(13),
	GetAllUser(14),
	CreateUser(15),
	UpdateUser(16),
	DeleteUser(17),
	SetACL(18),
	GetACL(19),
	GetAllACL(20),
	SetUserPassword(21),
	GetAllServices(22),
	
	GetServiceChangingByTime(23),
	GetMetadataChangingByTime(24),
	GetServiceChangingByVersion(25),
	GetMetadataChangingByVersion(26),
	AttachSession(27),
	UpdateServiceInstanceInternalStatus(28),
	GetServiceInstance(29),
	;
	

	private int id = 0;
	ProtocolType(int id){
		this.id = id;
	}
	
	public int getId(){
		return this.id;
	}
	
	public static ProtocolType valueOf(int id){
		if(id >= 0){
			for(ProtocolType t : values()){
				if(t.id == id){
					return t;
				}
			}
		}
		return null;
	}
}

