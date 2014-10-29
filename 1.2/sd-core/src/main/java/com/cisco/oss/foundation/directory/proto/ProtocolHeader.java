package com.cisco.oss.foundation.directory.proto;

import java.io.Serializable;

public class ProtocolHeader implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int xid;
	private ProtocolType type;
	public long createTime;

	public ProtocolHeader() {
	}

	public ProtocolHeader(int xid, ProtocolType type) {
		this.xid = xid;
		this.type = type;
	}

	public int getXid() {
		return xid;
	}

	public void setXid(int xid) {
		this.xid = xid;
	}

	public ProtocolType getType() {
		return type;
	}

	public void setType(ProtocolType type) {
		this.type = type;
	}
	
	public String toString(){
		return "{xid=" + xid + ", type=" + type + "}";
	}
}
