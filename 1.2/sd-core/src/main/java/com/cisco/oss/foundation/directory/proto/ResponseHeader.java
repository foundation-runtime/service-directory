package com.cisco.oss.foundation.directory.proto;

import java.io.Serializable;

import com.cisco.oss.foundation.directory.exception.ErrorCode;

public class ResponseHeader implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int xid;
	private long dxid;
	private ErrorCode err;

	public ResponseHeader() {
	}

	public ResponseHeader(int xid, long dxid, ErrorCode err) {
		this.xid = xid;
		this.dxid = dxid;
		this.err = err;
	}

	public int getXid() {
		return xid;
	}

	public void setXid(int xid) {
		this.xid = xid;
	}

	public long getDxid() {
		return dxid;
	}

	public void setDxid(long dxid) {
		this.dxid = dxid;
	}

	public ErrorCode getErr() {
		return err;
	}

	public void setErr(ErrorCode err) {
		this.err = err;
	}
	
	public String toString(){
		return "xid=" + xid + ", dxid=" + dxid + "err=" + err;
	}
	
}
