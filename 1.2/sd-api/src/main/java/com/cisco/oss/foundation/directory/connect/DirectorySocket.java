package com.cisco.oss.foundation.directory.connect;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import com.cisco.oss.foundation.directory.proto.Protocol;
import com.cisco.oss.foundation.directory.proto.ProtocolHeader;

public abstract class DirectorySocket {

//	private final static Logger LOGGER = LoggerFactory.getLogger(DirectorySocket.class);
	
	protected boolean initialized = false;
	
	
	protected final ByteBuffer lenBuffer = ByteBuffer.allocate(4);
	
	protected ByteBuffer incomingBuffer = lenBuffer;
	
	protected int connectTimeOut;
	
	protected long sentCount = 0;
    protected long recvCount = 0;
    protected long lastHeard;
    protected long lastSend;
    protected long now;
    
    protected long sessionId;
    
    DirectoryConnection clientConnection;

    void setConnection(DirectoryConnection clientConnection) {
        this.clientConnection = clientConnection;
    }
    
    protected DirectoryConnection getDirectoryConnection(){
    	return this.clientConnection;
    }
    
    void updateNow() {
        now = System.currentTimeMillis();
    }

    int getIdleRecv() {
        return (int) (now - lastHeard);
    }

    int getIdleSend() {
        return (int) (now - lastSend);
    }

    long getSentCount() {
        return sentCount;
    }

    long getRecvCount() {
        return recvCount;
    }

    void updateLastHeard() {
        this.lastHeard = now;
    }

    void updateLastSend() {
        this.lastSend = now;
    }
    
    public void setConnectTimeOut(int to){
    	this.connectTimeOut = to;
    }
    
    void updateLastSendAndHeard() {
        this.lastSend = now;
        this.lastHeard = now;
    }
    
    public abstract boolean isConnected();

    public abstract boolean connect(InetSocketAddress address) throws IOException;

    public abstract SocketAddress getRemoteSocketAddress();

    public abstract SocketAddress getLocalSocketAddress();

    public abstract void cleanup();
    public abstract void sendPacket(ProtocolHeader header, Protocol p) throws IOException;
}
