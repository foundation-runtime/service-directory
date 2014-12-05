/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

import java.io.Serializable;

import com.cisco.oss.foundation.directory.exception.ErrorCode;

/**
 * The Response Header Object.
 * 
 * @author zuxiang
 *
 */
public class ResponseHeader implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The SD API client xid.
	 */
	private int xid;
	
	/**
	 * THe SD Server xid.
	 */
	private long dxid;
	
	/**
	 * The Response ErrorCode.
	 */
	private ErrorCode err;

	/**
	 * Constructor.
	 */
	public ResponseHeader() {
	}

	/**
	 * Constructor.
	 * 
	 * @param xid
	 * 		the SD API xid.
	 * @param dxid
	 * 		the SD Server xid.
	 * @param err
	 * 		the ErrorCode.
	 */
	public ResponseHeader(int xid, long dxid, ErrorCode err) {
		this.xid = xid;
		this.dxid = dxid;
		this.err = err;
	}

	/**
	 * Get the SD API xid.
	 * @return
	 * 		the SD API xid.
	 */
	public int getXid() {
		return xid;
	}

	/**
	 * Set the SD API xid.
	 * 
	 * @param xid
	 * 		the SD API xid.
	 */
	public void setXid(int xid) {
		this.xid = xid;
	}

	/**
	 * Get the SD Server xid.
	 * 
	 * @return
	 * 		the SD Server xid.
	 */
	public long getDxid() {
		return dxid;
	}

	/**
	 * Set the SD Server xid.
	 * 
	 * @param dxid
	 * 		the SD Server xid.
	 */
	public void setDxid(long dxid) {
		this.dxid = dxid;
	}

	/**
	 * Get The Response ErrorCode.
	 * 
	 * @return
	 * 		The Response ErrorCode.
	 */
	public ErrorCode getErr() {
		return err;
	}

	/**
	 * Set The Response ErrorCode.
	 * 
	 * @param err
	 * 		The Response ErrorCode.
	 */
	public void setErr(ErrorCode err) {
		this.err = err;
	}
	
	public String toString(){
		return "xid=" + xid + ", dxid=" + dxid + "err=" + err;
	}
	
}
