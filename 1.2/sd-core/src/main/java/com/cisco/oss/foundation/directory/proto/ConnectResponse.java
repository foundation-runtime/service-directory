package com.cisco.oss.foundation.directory.proto;




public class ConnectResponse extends Response {

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int protocolVersion;
	private int timeOut;
	private String sessionId;
	private byte[] passwd;
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
	
	
}
