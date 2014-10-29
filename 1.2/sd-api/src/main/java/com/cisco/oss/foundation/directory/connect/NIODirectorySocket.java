package com.cisco.oss.foundation.directory.connect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.ServiceDirectoryThread;
import com.cisco.oss.foundation.directory.exception.ConnectTimeOutException;
import com.cisco.oss.foundation.directory.proto.Protocol;
import com.cisco.oss.foundation.directory.proto.ProtocolHeader;
import com.cisco.oss.foundation.directory.proto.Response;
import com.cisco.oss.foundation.directory.proto.ResponseHeader;

public class NIODirectorySocket extends DirectorySocket {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(NIODirectorySocket.class);

	private Selector selector;

	private SelectionKey selectionKey;
	
	private NIOThread nioThread;
	
	private Object connectHolder = new Object();
	
	private volatile boolean isConnected = false;
	
	private volatile boolean isConnecting = false;

	public NIODirectorySocket() {
		try {
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}

	@Override
	public boolean isConnected() {
		return this.isConnected;
	}

	@Override
	public boolean connect(InetSocketAddress address) throws IOException {
		nioThread = new NIOThread(address);
		nioThread.start();
		
		isConnecting = true;
		long timeToSleep = this.connectTimeOut;
		LOGGER.info("NIO connect go to sleep - " + timeToSleep);
		while(isConnecting && timeToSleep > 0){
			long now = System.currentTimeMillis();
			
			try {
				synchronized(connectHolder){
					connectHolder.wait(timeToSleep);
				}
			} catch (InterruptedException e) {
				// do nothing.
			}
			timeToSleep = timeToSleep - (System.currentTimeMillis() - now);
			
			LOGGER.info("NIO connect go to sleep - " + timeToSleep + ", isConnecting=" + isConnecting);
		}
		
		
		
		if(! isConnected()){
			System.out.println("-------------------------" );
			LOGGER.info("not", new Exception("debug"));
			onConnectFailed();
			return false;
		}else{
			lenBuffer.clear();
			incomingBuffer = lenBuffer;
			return true;
		}
		
	}
	
	

	void registerAndConnect(SocketChannel sock, InetSocketAddress addr)
			throws IOException {
		selectionKey = sock.register(selector, SelectionKey.OP_CONNECT);
		
		
        
		boolean immediateConnect = sock.connect(addr);
		LOGGER.info("Connect to host=" + addr.getHostName() + ", hostString=" + addr.getHostString() + ", port=" + addr.getPort() + ", all=" + addr.getAddress() + ", local=" + sock.socket().getLocalSocketAddress());
		if (immediateConnect) {
			onConnectSucceeded();
		}
		
		
	}

	SocketChannel createSock() throws IOException {
		SocketChannel sock;
		sock = SocketChannel.open();
		sock.configureBlocking(false);
		sock.socket().setSoLinger(false, -1);
		sock.socket().setTcpNoDelay(true);
		return sock;
	}

	@Override
	public SocketAddress getRemoteSocketAddress() {
		try {
            return ((SocketChannel) selectionKey.channel()).socket()
                    .getRemoteSocketAddress();
        } catch (NullPointerException e) {
            return null;
        }
	}

	@Override
	public SocketAddress getLocalSocketAddress() {
		try {
            return ((SocketChannel) selectionKey.channel()).socket()
                    .getLocalSocketAddress();
        } catch (NullPointerException e) {
            return null;
        }
	}

	@Override
	public void cleanup() {
		if(nioThread != null){
			nioThread.toStop();
		}
		
		if (selectionKey != null) {
			
            SocketChannel sock = (SocketChannel) selectionKey.channel();
            selectionKey.cancel();
            try {
                sock.socket().shutdownInput();
            } catch (IOException e) {
                if (LOGGER.isDebugEnabled()) {
                	LOGGER.debug("Ignoring exception during shutdown input", e);
                }
            }
            try {
                sock.socket().shutdownOutput();
            } catch (IOException e) {
                if (LOGGER.isDebugEnabled()) {
                	LOGGER.debug("Ignoring exception during shutdown output",
                            e);
                }
            }
            try {
                sock.socket().close();
            } catch (IOException e) {
                if (LOGGER.isDebugEnabled()) {
                	LOGGER.debug("Ignoring exception during socket close", e);
                }
            }
            try {
                sock.close();
            } catch (IOException e) {
                if (LOGGER.isDebugEnabled()) {
                	LOGGER.debug("Ignoring exception during channel close", e);
                }
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            if (LOGGER.isDebugEnabled()) {
            	LOGGER.debug("SendThread interrupted during sleep, ignoring");
            }
        }
        selectionKey = null;
	}

	@SuppressWarnings("unused")
	private void close() {
		try {
            if (LOGGER.isTraceEnabled()) {
            	LOGGER.trace("Doing client selector close");
            }
            selector.close();
            if (LOGGER.isTraceEnabled()) {
            	LOGGER.trace("Closed client selector");
            }
        } catch (IOException e) {
        	LOGGER.warn("Ignoring exception during selector close", e);
        }
	}

	private void enableReadOnly() {
//		int i = selectionKey.interestOps();
//		if ((i & SelectionKey.OP_READ) == 0) {
			selectionKey.interestOps(SelectionKey.OP_READ);
//		}
	}
	
	@SuppressWarnings("unused")
	private void wakeupCnxn() {
		selector.wakeup();
	}

	protected void readLength() throws IOException {
		int len = incomingBuffer.getInt();
		LOGGER.info("Read length in client - " + len);
		if (len < 0 || len >= DirectoryConnection.packetLen) {
			throw new IOException("Packet len" + len + " is out of range!");
		}
		incomingBuffer = ByteBuffer.allocate(len);
	}

	@Override
	public void sendPacket(ProtocolHeader header, Protocol protocol) throws IOException {
		SocketChannel sock = (SocketChannel) selectionKey.channel();
        if (sock == null) {
            throw new IOException("Socket is null!");
        }
        
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(header);
            oos.writeObject(protocol);
            
            StringBuilder sb = new StringBuilder("Create ByteBuffer, RequestHeader[");
            if(header != null){
            	sb.append("xid=").append(header.getXid());
            	sb.append(", Type=").append(header.getType());
            }else{
            	sb.append("NULL");
            }
            sb.append("], Request["); 
            if(protocol != null){
            	sb.append(protocol.getClass().getName());
            } else {
            	sb.append("NULL");
            }
            sb.append("]");
            LOGGER.info(sb.toString());
            
            byte[] bytes = baos.toByteArray();
            int l = bytes.length + 4;
            System.out.println("The target l=" + l + ", bl=" + bytes.length);
            ByteBuffer bb = ByteBuffer.allocate(l);
            bb.putInt(bytes.length);
            bb.put(bytes);
            LOGGER.info(".................Send packet, xid=" + header.getXid() + ", type=" + header.getType());
            bb.flip();
            sock.write(bb);
        } catch (IOException e) {
            LOGGER.warn("Ignoring unexpected exception", e);
        } finally {
        	try {
        		if(baos != null){
        			baos.close();
        		}
			} catch (IOException e) {
				// do nothing.
			}
        }
        
	}

	public Selector getSelector() {
		return selector;
	}
	
	private void doSocketConnect(InetSocketAddress addr) throws IOException{
		SocketChannel sock = createSock();
		registerAndConnect(sock, addr);
	}
	
	private void onConnectSucceeded(){
		LOGGER.info("Accepted the connection.");
		
		isConnected = true;
		isConnecting = false;
		enableReadOnly();
		synchronized(connectHolder){
			connectHolder.notifyAll();
		}
	}
	
	private void onConnectFailed(){
		LOGGER.info("Socket failed");
		isConnecting = false;
		isConnected = false;
		cleanup();
		synchronized(connectHolder){
			connectHolder.notifyAll();
		}
	}
	class NIOThread extends Thread {
		
		private final InetSocketAddress server;
		
		private boolean toStop = false;
		
		public NIOThread(InetSocketAddress server){
			super(ServiceDirectoryThread.getThreadName("NIO_Connection_Socket"));
			this.server = server;
		}
		
		public void toStop(){
			this.toStop = true;
		}
		@Override
		public void run(){
			LOGGER.info("NIO Thread do connection");
			try {
				doSocketConnect(server);
			} catch (IOException e1) {
				LOGGER.error("Socket connect to server failed - " + server);
				onConnectFailed();
			}
			
			isConnecting = true;
			updateNow();
			updateLastSendAndHeard();
			
			while(! toStop){
				try{
					int timeOut = connectTimeOut;
					if(isConnecting){
						timeOut = connectTimeOut - getIdleRecv();
					}
					
					if(selectionKey != null){
						LOGGER.info("NIO Thread kick off, timeOut=" + timeOut + ", ops=" + selectionKey.interestOps());// + ", keySize=" + selected.size());}
					} else {
						LOGGER.info("NIO Thread kick off, timeOut=" + timeOut );	
					}
					
					selector.select(timeOut);
					Set<SelectionKey> selected;
					synchronized (this) {
						selected = selector.selectedKeys();
					}
					LOGGER.info("NIO Thread kick off, size=" + selected.size() );
					
					
					updateNow();
					for (SelectionKey k : selected) {
						SocketChannel sc = ((SocketChannel) k.channel());
						if ((k.readyOps() & SelectionKey.OP_CONNECT) != 0) {
							if (sc.finishConnect()) {
								LOGGER.info("The local socket is " + sc.socket().getLocalSocketAddress());
								updateLastHeard();
								onConnectSucceeded();
							}
						} else if ((k.readyOps() & (SelectionKey.OP_READ )) != 0) {
							LOGGER.info("NIO Thread, doIO");
							 doIO();
						}
					}
					
					if(isConnecting){
						int to = (int) (System.currentTimeMillis() - getIdleRecv());
						if(to < 0){
							throw new ConnectTimeOutException();
						}
					}
					selected.clear();

				} catch (Exception e) {
					LOGGER.info("NIO Exception.", e);
					if(! isConnected()){
						if (e instanceof ConnectTimeOutException) {
							
						}
						onConnectFailed();
					}else{
						
					}
					
					
				}
			}
			
		}
		
		
		
		private void doIO() throws IOException {
			LOGGER.info("doIO, readable=" + selectionKey.isReadable() + ", writable=" + selectionKey.isWritable());
			SocketChannel sc = (SocketChannel) selectionKey.channel();
			if (selectionKey.isReadable()) {
				sc.read(incomingBuffer);
				if (!incomingBuffer.hasRemaining()) {
					incomingBuffer.flip();
					if (incomingBuffer == lenBuffer) {
						recvCount++;
						readLength();
					} else {
						readResponse();
						lenBuffer.clear();
						incomingBuffer = lenBuffer;
						updateLastHeard();
					}
				}
			}

		}
		
		private void readResponse() throws IOException{
			
			ByteArrayInputStream bbis = new ByteArrayInputStream(
	                incomingBuffer.array());
	        ObjectInputStream ois = new ObjectInputStream(bbis);
	        ResponseHeader header = null;
	        Response resp = null;

	        try {
	        	header = (ResponseHeader) ois.readObject();
				resp = (Response) ois.readObject();
			} catch (ClassNotFoundException e1) {
				LOGGER.error("Deserialize response failed - " + e1.getMessage());
				throw new IOException("Desierialize Response failed - " + e1.getMessage(), e1);
				
			}
	        
	        LOGGER.info("......................Received response, xid=" + header.getXid() +", dxid=" + header.getDxid() + ", time=" + System.currentTimeMillis());
	       
	        
	        clientConnection.onReceivedPesponse(header, resp);
		}
			
	}

	

}
