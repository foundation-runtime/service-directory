/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

/**
 * The SD API client connect Response.
 * 
 * @author zuxiang
 *
 */
public class ConnectResponse extends Response {

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The ProtocolVersion.
	 */
	private int protocolVersion;
	
	/**
	 * The session timeout.
	 */
	private int timeOut;
	
	/**
	 * The session id.
	 */
	private String sessionId;
	
	/**
	 * The session password.
	 */
	private byte[] passwd;
	
	/**
	 * The serverId.
	 */
	private int serverId;

	public ConnectResponse() {
	}

	public ConnectResponse(int protocolVersion, int timeOut, String sessionId,
			byte[] passwd, int serverId) {
		this.protocolVersion = protocolVersion;
		this.timeOut = timeOut;
		this.sessionId = sessionId;
		this.passwd = passwd;
		this.serverId = serverId;
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(int protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public int getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public byte[] getPasswd() {
		return passwd;
	}

	public void setPasswd(byte[] passwd) {
		this.passwd = passwd;
	}
	
	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}
}
