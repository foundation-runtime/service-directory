/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

import java.io.Serializable;

/**
 * The ProtocolHeader Object.
 * 
 * @author zuxiang
 *
 */
public class ProtocolHeader implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The Client xid.
	 */
	private int xid;
	
	/**
	 * The ProtocolType.
	 */
	private ProtocolType type;
	
	/**
	 * The Protocol CreateTime in ms.
	 */
	public long createTime;

	/**
	 * Constructor.
	 */
	public ProtocolHeader() {
	}

	/**
	 * Constructor.
	 * 
	 * @param xid
	 * 		the Sd API client xid.
	 * @param type
	 * 		the ProtocolType.
	 */
	public ProtocolHeader(int xid, ProtocolType type) {
		this.xid = xid;
		this.type = type;
	}

	/**
	 * Get the SD API client xid.
	 * 
	 * @return
	 * 		the SD PAI Client xid.
	 */
	public int getXid() {
		return xid;
	}

	/**
	 * Set the the SD PAI Client xid.
	 * 
	 * @param xid
	 * 		the SD PAI Client xid.
	 */
	public void setXid(int xid) {
		this.xid = xid;
	}

	/**
	 * Get the ProtocolType.
	 * 
	 * @return
	 * 		The ProtocolType.
	 */
	public ProtocolType getType() {
		return type;
	}

	/**
	 * Set the ProtocolType.
	 * 
	 * @param type
	 * 		the ProtocolType.
	 */
	public void setType(ProtocolType type) {
		this.type = type;
	}
	
	@Override
	public String toString(){
		return "{xid=" + xid + ", type=" + type + "}";
	}
}
