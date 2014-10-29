package com.cisco.oss.foundation.directory.event;

public enum ConnectionStatus {
	NEW, CONNECTED, CONNECTEDREADONLY, CLOSED, AUTH_FAILED, NOT_CONNECTED;

	public boolean isAlive() {
		return this != CLOSED;
	}

	public boolean isConnected() {
		return this == CONNECTED || this == CONNECTEDREADONLY;
	}
}
