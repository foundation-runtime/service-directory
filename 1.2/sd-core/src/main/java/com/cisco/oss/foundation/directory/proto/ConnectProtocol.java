package com.cisco.oss.foundation.directory.proto;



public class ConnectProtocol extends Protocol {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int protocolVersion;
	private long lastDxidSeen;
	private int timeOut;
	private String sessionId;
	private byte[] passwd;
	
	// Auth data for the Directory Authentication.
	private String userName;
	private byte[] secret;
	private boolean obfuscated;

	public ConnectProtocol() {
	}

	public ConnectProtocol(int protocolVersion, long lastDxidSeen, int timeOut,
			String sessionId, byte[] passwd, String userName, byte[] secret, boolean obfuscated) {
		this.protocolVersion = protocolVersion;
		this.lastDxidSeen = lastDxidSeen;
		this.timeOut = timeOut;
		this.sessionId = sessionId;
		this.passwd = passwd;
		this.userName = userName;
		this.secret = secret;
		this.obfuscated = obfuscated;
	}

	public long getLastDxidSeen() {
		return lastDxidSeen;
	}

	public void setLastDxidSeen(long lastDxidSeen) {
		this.lastDxidSeen = lastDxidSeen;
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

	public int getProtocolVersion() {
		return protocolVersion;
	}

	public void setProtocolVersion(int protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public byte[] getSecret() {
		return secret;
	}

	public void setSecret(byte[] secret) {
		this.secret = secret;
	}

	public boolean isObfuscated() {
		return obfuscated;
	}

	public void setObfuscated(boolean obfuscated) {
		this.obfuscated = obfuscated;
	}
	
	
}
