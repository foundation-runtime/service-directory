package com.cisco.oss.foundation.directory.connect;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.proto.Protocol;
import com.cisco.oss.foundation.directory.proto.ProtocolHeader;
import com.cisco.oss.foundation.directory.utils.WebSocketSerializer;
import com.cisco.oss.foundation.directory.utils.WebSocketSerializer.ResponseDeserializer;

public class WSDirectorySocket extends DirectorySocket implements WebSocketListener {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(WSDirectorySocket.class);
	private Session session;
	private WebSocketClient client;
	private Future<Session> sessionFuture;

	@Override
	public boolean isConnected() {
		if(client != null && session != null){
			return session.isOpen();
		}
		return false;
	}

	/**
	 * Connect to the remote socket.
	 * 
	 * once fail or exception, it do the cleanup itself.
	 */
	@Override
	synchronized public boolean connect(InetSocketAddress addr) throws IOException{
		String host = addr.getHostName();
		int port = addr.getPort();

		URI uri = URI.create("ws://" + host + ":" + port + "/ws/service/");

		if(LOGGER.isTraceEnabled()){
			LOGGER.trace("WebSocket connect to timeOut=" + connectTimeOut + " - " + uri.toString());
		}
		client = new WebSocketClient();
		try {
			client.start();
			sessionFuture = client.connect(this, uri);
				
			Session ss = sessionFuture.get(this.connectTimeOut, TimeUnit.MILLISECONDS);
			if(ss != null){
				return true;
			} else {
				cleanup();
				return false;
			}
		} catch (Exception e) {
			cleanup();
			throw new IOException(e);
		}
		
	}

	@Override
	public SocketAddress getRemoteSocketAddress() {
		if(session != null && session.isOpen()){
			return session.getRemoteAddress();
		}
		return null;
	}

	@Override
	public SocketAddress getLocalSocketAddress() {
		if(session != null && session.isOpen()){
			return session.getLocalAddress();
		}
		return null;
	}

	/**
	 * Cleanup the DirectorySocket to original status to connect
	 * to another Socket.
	 */
	@Override
	synchronized public void cleanup() {
		LOGGER.info("Cleanup the DirectorySocket.");
		if(client != null){
			try {
				client.stop();
			} catch (Exception e) {
				LOGGER.warn("Close WebSocketClient get exception.", e);
			}
			client = null;
		}
		
		try {
			if(session != null){
				session.close();
			}
		} catch (IOException e) {
			LOGGER.warn("Close the WebSocket Session get exception.", e);
		}
		
		if(sessionFuture != null){
			try {
			sessionFuture.cancel(true);
			} catch (Exception e) {
				// do nothing
			}
		}
		
		
		session = null;
		sessionFuture = null;
	}

	@Override
	public void sendPacket(ProtocolHeader header, Protocol p) throws IOException {
		String s = WebSocketSerializer.getProtocolSerializer(header, p).searializerAsString();
		if(LOGGER.isTraceEnabled()){
			LOGGER.trace("Web socket send - " + s);
		}
		session.getRemote().sendString(s);
		
	}

	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		// We only use the text message.
		LOGGER.warn("No binary message...");
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		LOGGER.info("Close the WebSocket, statusCode=" + statusCode + ", reason=" + reason);
		if(this.session != null){
			try {
				this.session.close();
			} catch (IOException e) {
				LOGGER.warn("Close WebSocket session get exception.");
				if(LOGGER.isTraceEnabled()){
					LOGGER.trace("Close WebSocket session get exception.", e);
				}
			}
			this.session = null;
			cleanup();
		}
	}

	@Override
	public void onWebSocketConnect(Session session){
		this.session = session;
		if(LOGGER.isTraceEnabled()){
			LOGGER.trace("WebSocket client receive connection " + session.getRemoteAddress() + " - " +session.getLocalAddress());
		}
	}

	@Override
	public void onWebSocketError(Throwable error) {
		LOGGER.error("WebSocket client get error.");
		if(LOGGER.isTraceEnabled()){
			LOGGER.trace("WebSocket client get error.", error);
		}
		cleanup();
		clientConnection.onSocketError();
	}

	@Override
	public void onWebSocketText(String text) {
		if(LOGGER.isTraceEnabled()){
			LOGGER.trace("WebSocket client received - " + text);
		}
		ResponseDeserializer ds = null;
		try {
			ds = WebSocketSerializer.getResponseDeserializer(text);
			clientConnection.onReceivedPesponse(ds.deserializerResponseHeader(), ds.deserializerResponse());
		} catch (IOException e) {
			LOGGER.error("Parse response get exception", e);
		}
		
	}

}
