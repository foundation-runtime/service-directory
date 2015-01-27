/**
 * Copyright 2014 Cisco Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cisco.oss.foundation.directory.connect;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import com.cisco.oss.foundation.directory.proto.Protocol;
import com.cisco.oss.foundation.directory.proto.ProtocolHeader;

/**
 * The abstract DirectorySocket.
 * 
 * Now it has a NIO and WebSocket implementation.
 * 
 * @author zuxiang
 *
 */
public abstract class DirectorySocket {

//	private final static Logger LOGGER = LoggerFactory.getLogger(DirectorySocket.class);
	
	/**
	 * Indicate whether is initialized.
	 */
	protected boolean initialized = false;
	
	/**
	 * The length Buffer.
	 */
	protected final ByteBuffer lenBuffer = ByteBuffer.allocate(4);
	
	/**
	 * The received buffer.
	 */
	protected ByteBuffer incomingBuffer = lenBuffer;
	
	/**
	 * Socket connect timeout.
	 */
	protected int connectTimeOut;
	
	/**
	 * All send count.
	 */
	protected long sentCount = 0;
	
	/**
	 * All receive count.
	 */
    protected long recvCount = 0;
    
    /**
     * The last receive response time in ms.
     */
    protected long lastHeard;
    
    /**
     * The last send request time in ms.
     */
    protected long lastSend;
    
    /**
     * The current time in ms.
     */
    protected long now;
    
    /**
     * The session id.
     */
    protected long sessionId;
    
    /**
     * The DirectoryConnection.
     */
    DirectoryConnection clientConnection;

    /**
     * Set the DirectoryConnection.
     * 
     * @param clientConnection
     * 		the DirectoryConnection.
     */
    void setConnection(DirectoryConnection clientConnection) {
        this.clientConnection = clientConnection;
    }
    
    /**
     * Get the DirectoryConnection.
     * 
     * @return
     * 		the DirectoryConnection.
     */
    protected DirectoryConnection getDirectoryConnection(){
    	return this.clientConnection;
    }
    
    /**
     * Update the current time.
     */
    void updateNow() {
        now = System.currentTimeMillis();
    }

    /**
     * Get the idle gap of the receive response.
     * 
     * @return
     * 		the idle time of receive response.
     */
    int getIdleRecv() {
        return (int) (now - lastHeard);
    }

    /**
     * Get the idle gap of the send request.
     * 
     * @return
     * 		the idle time of send request.
     */
    int getIdleSend() {
        return (int) (now - lastSend);
    }

    /**
     * Get the send count.
     * 
     * @return
     * 		the send count.
     */
    long getSentCount() {
        return sentCount;
    }

    /**
     * Get the receive count.
     * 
     * @return
     * 		the receive count.
     */
    long getRecvCount() {
        return recvCount;
    }

    /**
     * Update last receive response time.
     */
    void updateLastHeard() {
        this.lastHeard = now;
    }

    /**
     * update last send request time.
     */
    void updateLastSend() {
        this.lastSend = now;
    }
    
    /**
     * Set the connect timeout.
     * 
     * @param to
     * 		the connect timeout.
     */
    public void setConnectTimeOut(int to){
    	this.connectTimeOut = to;
    }
    
    /**
     * Update last send and receive time.
     */
    void updateLastSendAndHeard() {
        this.lastSend = now;
        this.lastHeard = now;
    }
    
    /**
     * Indicate whether Sockect connected.
     * 
     * @return
     * 		true for connected.
     */
    public abstract boolean isConnected();

    /**
     * Connect to the remote DirectoryServer.
     * 
     * When invoke the connect, the socket has to stop current connect,
     * and connect to new DirectoryServer.
     * 
     * @param address
     * 		the remote Directory Server address.
     * @return
     * 		true of success.
     */
    public abstract boolean connect(InetSocketAddress address);

    /**
     * Get the remote server SocketAddress.
     * 
     * @return
     * 		the remote Directory Server SocketAddress.
     */
    public abstract SocketAddress getRemoteSocketAddress();

    /**
     * Get the local SocketAddress.
     * 
     * @return
     * 		the local SocketAddress of the socket.
     */
    public abstract SocketAddress getLocalSocketAddress();

    /**
     * Cleanup the socket connection.
     */
    public abstract void cleanup();
    
    /**
     * Send a Packet.
     * 
     * @param header
     * 		the ProtocolHeader.
     * @param p
     * 		the Protocol.
     * @throws IOException
     * 		the IOException.
     */
    public abstract void sendPacket(ProtocolHeader header, Protocol p) throws IOException;
}
