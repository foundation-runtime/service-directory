/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.connect;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.Configurations;
import com.cisco.oss.foundation.directory.ServiceDirectoryThread;
import com.cisco.oss.foundation.directory.async.Callback.ProtocolCallback;
import com.cisco.oss.foundation.directory.async.ServiceDirectoryFuture;
import com.cisco.oss.foundation.directory.async.Watcher;
import com.cisco.oss.foundation.directory.async.WatcherManager;
import com.cisco.oss.foundation.directory.entity.AuthScheme;
import com.cisco.oss.foundation.directory.entity.EventType;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstanceEvent;
import com.cisco.oss.foundation.directory.entity.WatchedService;
import com.cisco.oss.foundation.directory.event.ConnectionStatus;
import com.cisco.oss.foundation.directory.event.ServiceDirectoryEvent;
import com.cisco.oss.foundation.directory.event.ServiceDirectoryEvent.ClientSessionEvent;
import com.cisco.oss.foundation.directory.event.ServiceDirectoryEvent.ClientSessionEvent.SessionEvent;
import com.cisco.oss.foundation.directory.event.ServiceDirectoryEvent.ClientStatusEvent;
import com.cisco.oss.foundation.directory.event.ServiceDirectoryListener;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceDirectoryError;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.exception.SessionTimeOutException;
import com.cisco.oss.foundation.directory.impl.DirectoryServiceClient.WatcherRegistration;
import com.cisco.oss.foundation.directory.proto.ConnectProtocol;
import com.cisco.oss.foundation.directory.proto.ConnectResponse;
import com.cisco.oss.foundation.directory.proto.Event;
import com.cisco.oss.foundation.directory.proto.Protocol;
import com.cisco.oss.foundation.directory.proto.ProtocolHeader;
import com.cisco.oss.foundation.directory.proto.ProtocolType;
import com.cisco.oss.foundation.directory.proto.Response;
import com.cisco.oss.foundation.directory.proto.ResponseHeader;
import com.cisco.oss.foundation.directory.proto.ServerEvent;
import com.cisco.oss.foundation.directory.proto.ServerEvent.CloseSessionEvent;
import com.cisco.oss.foundation.directory.proto.ServiceInstanceOperate;
import com.cisco.oss.foundation.directory.proto.ServiceInstanceOperate.OperateType;
import com.cisco.oss.foundation.directory.proto.WatcherEvent;
import com.cisco.oss.foundation.directory.stats.PacketLatency;
import com.cisco.oss.foundation.directory.utils.ObfuscatUtil;

/**
 * The SD API Connection.
 * 
 * It maintains the SD API connection to Directory Server.
 * 
 * @author zuxiang
 *
 */
public class DirectoryConnection {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(DirectoryConnection.class);
	
	/**
	 * The session timeout value property name.
	 */
	public final static String SESSION_TIMEOUT_PROPERTY_NAME = "session.timeout";
    
	/**
	 * The default session timeout value.
	 */
    private final static int SESSION_TIMEOUT_DEFAULT_VALUE = 4000; 

    /**
     * The packet length limitation.
     */
	public static final int packetLen = 4096*1024;
	
	/**
	 * The death object for the EventManager. When EventManager see the Object,
	 * stop it self.
	 */
	private Object eventOfDeath = new Object();
	
	/**
	 * The AuthData of the Connection. It generates the anonymous by default. 
	 */
	private AuthData authData = generateDirectoryAuthData("anonymous", null);
	
	/**
	 * The Queue for the all pending Packet that submitted and not get response from Directory Server.
	 */
    private final LinkedList<Packet> pendingQueue = new LinkedList<Packet>();
    
    /**
     * The ServiceDirectoryListener list registered.
     */
    private final List<ServiceDirectoryListener> statusListeners = new ArrayList<ServiceDirectoryListener>();

    /**
     * Indicate whether the Connection is closing.
     */
    private volatile boolean closing = false;
    
    /**
     * The Session object of the Connection.
     */
    private Session session;
    
    /**
     * The xid of the packet, every time send a packet increase it by 1.
     */
    private AtomicInteger xid = new AtomicInteger(1);
    
    /**
     * Last xid of the Directory Server.
     */
    private volatile long lastDxid = -1;
    
    /**
     * The ConnectionStatus.
     */
    private volatile ConnectionStatus status = ConnectionStatus.NEW;
    
    /**
     * The EventThread to handle the Event.
     */
    private EventThread eventThread;
    
    /**
     * The deamon thread to maintain the Conenction.
     */
    private Thread connectionThread;
    
    private DirectorySocket clientSocket;
    
    /**
     * The Directory Server connected to.
     */
    private InetSocketAddress directoryServer;
    
    /**
     * The WatcherManager that manages all Service Watchers.
     */
    private WatcherManager watcherManager;
    
    /**
     * The AtomicReference to the Ping Response.
     */
    private AtomicReference<ResponseHeader> pingResponse = new AtomicReference<ResponseHeader>();
    
    /**
     * the time of the latest ping send time, use to measure Ping time.
     */
    private long lastPingSentNs = 0;
    /**
     * Default constructor.
     */
    public DirectoryConnection(){
    	
    }
    
    /**
     * The DirectoryConnection constructor.
     * 
     * @param directoryServer
     * 		the Directory Server to connect.
     * @param watcherManager
     * 		the WatcherManager.
     * @param clientSocket
     * 		the DirectorySocket implementation that used.
     * @param userName
     * 		the user name for auth.
     * @param password
     * 		the password.
     */
	public DirectoryConnection(InetSocketAddress directoryServer, 
			WatcherManager watcherManager, DirectorySocket clientSocket, String userName, String password){
		this.directoryServer = directoryServer;
		this.watcherManager = watcherManager;
		this.clientSocket = clientSocket;
		session = new Session();
		
		session.timeOut = Configurations.getInt(SESSION_TIMEOUT_PROPERTY_NAME, SESSION_TIMEOUT_DEFAULT_VALUE);
		
		if(userName != null && ! userName.isEmpty()){
			this.authData = this.generateDirectoryAuthData(userName, password);
		}
		clientSocket.setConnection(this);
		clientSocket.setConnectTimeOut(session.timeOut *2 /3);
		eventThread = new EventThread();
	}
	
	/**
	 * Set a new Directory Server address.
	 * 
	 * DirectoryConenction will reopen the session to the new Directory Server.
	 * 
	 * @param directoryServer
	 * 		the Directory Server address.
	 */
	public void setDirectoryServers(InetSocketAddress directoryServer){
		this.directoryServer = directoryServer;
		reopenSession();
	}
	
	/**
	 * Block util the connect complete.
	 * 
	 * It just make sure the connect finished, it doesn't mean the DirectoryConnection
	 * CONNECTED. Because DirectoryConnection will keep polling remote Directory Server, 
	 * if it is NOT_CONNECTED.
	 * And it doesn't block the Thread interruption.
	 */
	public void blockUtilConnected(){
		
		if(getStatus().isConnected()){
			return;
		}
			
		int to = getConnectTimeOut();
		final Object o = new Object();
		
		long now = System.currentTimeMillis();
		
		synchronized(o){
			
			ServiceDirectoryListener listener = new ServiceDirectoryListener(){

				@Override
				public void notify(ServiceDirectoryEvent event) {
					synchronized(o){
						o.notifyAll();
					}
				}
				
			};
			
			registerClientChangeListener(listener);
			try{
				while(to > 0){
					if (getStatus().isConnected()) {
						return;
					}
					try {
						o.wait(to);
					} catch (InterruptedException e) {
						LOGGER.warn("Block Util Connected interrupted.");
					}
					
					to -= (System.currentTimeMillis() - now);
				}
			}finally{
				unregisterClientChangeListener(listener);
			}
		}
		
		
	}
	
	/**
	 * Start the DirectoryConnection.
	 * 
	 * It is not thread safe.
	 */
	public void start() {
		setStatus(ConnectionStatus.NOT_CONNECTED);
		InetSocketAddress address = directoryServer;
		clientSocket.connect(address);
		eventThread.start();
		connectionThread = new Thread(new ConnectTask());
		connectionThread.setDaemon(true);
		connectionThread.setName(ServiceDirectoryThread.getThreadName("Client_Connect_Thread"));
		connectionThread.start();
    }
	
	/**
	 * Get the session id.
	 */
	public String getSessionId(){
		return session.id;
	}
    
	/**
	 * Close the Directory Connection.
	 * 
	 * It is thread safe.
	 * 
	 * @throws IOException
	 * 		the IOException in closing.
	 */
    public synchronized void close() throws IOException {
    	if(getStatus().equals(ConnectionStatus.CLOSED)){
    		return ;
    	}
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Closing client for session: 0x"
                      + getSessionId());
        }
        connectRun.set(false);
        
        try{
        	clientSocket.cleanup();
        }catch(Exception e){
        	LOGGER.warn("Close the WSDirectorySocket get error.", e);
        }

        try {
        	setStatus(ConnectionStatus.CLOSED);
        	sendCloseSession();
            closeSession();
            eventThread.queueEventOfDeath();
        } catch (ServiceException e) {
            LOGGER.warn("Execute the CloseSession Protocol failed when close", e);
        } 
    }
    
    /**
     * Submit a Request.
     * 
     * @param h
     * 		the ProtocolHeader.
     * @param request
     * 		the Protocol.
     * @param wr
     * 		the WatcherRegistration
     * @return
     * 		the Response of the Request.
     */
    public Response submitRequest(ProtocolHeader h, Protocol request, 
    		WatcherRegistration wr){
        Packet packet = queuePacket(h, request, null, null, null, wr);
        synchronized (packet) {
            while (!packet.finished) {
                try {
					packet.wait();
				} catch (InterruptedException e) {
					ServiceDirectoryError sde = new ServiceDirectoryError(ErrorCode.REQUEST_INTERUPTED);
					throw new ServiceException(sde, e);
				}
            }
        }
        if(! packet.respHeader.getErr().equals(ErrorCode.OK)){
        	ServiceDirectoryError sde = new ServiceDirectoryError(packet.respHeader.getErr());
        	throw new ServiceException(sde);
        }
        return packet.response;
    }
    
    /**
     * Submit a Request with Callback.
     * 
     * It is a asynchronized method, it returns on until Request complete.
     * When the Request complete, SD API will invoke the Callback.
     * 
     * @param h
     * 		the ProtocolHeader.
     * @param request
     * 		the Protocol.
     * @param callBack
     * 		the Callback.
     * @param context
     * 		the Context object of the Callback.
     */
    public void submitCallbackRequest(ProtocolHeader h, Protocol request, ProtocolCallback callBack, Object context){
    	queuePacket(h, request, callBack, context, null, null);
    }
    
    /**
     * Submit a Request in asynchronizing, it return a Future for the 
     * Request Response.
     * 
     * @param h
     * 		the ProtocolHeader.
     * @param request
     * 		the Protocol.
     * @param wr
     * 		the WatcherRegistration of the Service.
     * @return
     * 		the Future.
     */
    public ServiceDirectoryFuture submitAsyncRequest(ProtocolHeader h, Protocol request, WatcherRegistration wr){
    	ServiceDirectoryFuture future = new ServiceDirectoryFuture();
    	queuePacket(h, request, null, null, future, wr);
    	return future;
    }

    /**
     * Queue the Packet to Connection.
     * 
     * The DirectoryConnect Queue the Packet to the internal Queue and send it to remote
     * Directory Server.
     * 
     * @param header
     * 		the ProtocolHeader.
     * @param protocol
     * 		the Protocol.
     * @param cb
     * 		the Callback.
     * @param context
     * 		the context Object of the Callback.
     * @param future
     * 		the Future.
     * @param wr
     * 		WatcherRegistration
     * @return
     * 		the queued Packet.
     */
    private Packet queuePacket(ProtocolHeader header, Protocol protocol,
    		ProtocolCallback cb, Object context, ServiceDirectoryFuture future, WatcherRegistration wr)
    {
        Packet packet = new Packet(header, protocol, wr);
        PacketLatency.initPacket(packet);
        header.createTime = packet.createTime;
		packet.cb = cb;
		packet.context = context;
		packet.future = future;

        if (! clientSocket.isConnected() || closing) {
			onLossPacket(packet);
		} else {
			
			synchronized (pendingQueue) {
				
				if (LOGGER.isTraceEnabled()) {
					LOGGER.trace("Add the packet in queuePacket, type=" + header.getType());
				}
				header.setXid(xid.incrementAndGet());
				try{
					PacketLatency.queuePacket(packet);
		        	clientSocket.sendPacket(header, protocol);
		        	pendingQueue.add(packet);
		        	PacketLatency.sendPacket(packet);
		        } catch(IOException e){
		        	LOGGER.error("ClientSocket send packet failed.");
		        	if(LOGGER.isTraceEnabled()){
		        		LOGGER.trace("ClientSocket send packet failed.", e);
		        	}
		        	if(packet != null){
		        		onLossPacket(packet);
		        	}
		        }
			}
		}
        
        return packet;
    }
    
    /**
     * On the DirectoryConnection receive Response from DirectorySocket.
     * 
     * @param header
     * 		the Response Header.
     * @param response
     * 		the Response.
     */
    public void onReceivedPesponse(ResponseHeader header, Response response){
    	Packet packet = null;
    	updateRecv();
    	
    	if (header.getXid() == -4) {
            // -4 is the xid for AuthPacket               
            if(ErrorCode.AUTHENT_FAILED.equals(header.getErr())) {
            	if(getStatus().isAlive()){
            		setStatus(ConnectionStatus.AUTH_FAILED);
            	}
            }
                LOGGER.info("Got auth sessionid:0x" + session.id + ", error=" + header.getErr());
            return;
        }
    	
        if (header.getXid() == -1) {
            // -1 means watcher notification
            WatcherEvent event = (WatcherEvent) response;
            if(LOGGER.isTraceEnabled()){
            	LOGGER.trace("Got Watcher " + event + " for sessionid 0x" + session.id);
            }
            eventThread.queueWatcherEvent( event );
            return;
        }
        
        if (header.getXid() == -8) {
            // -8 means server notification
        	ServerEvent event = (ServerEvent) response;
        	
        	if(response instanceof CloseSessionEvent){
        		closeSession();
        		return;
        	}
            	
        	if(LOGGER.isTraceEnabled()){
        		LOGGER.trace("Got Server " + event + " for sessionid 0x" + session.id);
        	}
            eventThread.queueServerEvent( event );
            return;
        }
    	
        if (header.getXid() == -2) {
            // -2 is the xid for pings
        	if(LOGGER.isTraceEnabled()){
        		LOGGER.info("Got ping response for sessionid: 0x" + session.id
        				+ " after " + (System.currentTimeMillis() - lastPingSentNs) + "ms");
        	}
        	
        	pingResponse.set(header);
        	synchronized(pingResponse){
        		pingResponse.notifyAll();
        	}
            return;
        }
        
        synchronized(pendingQueue){
        	// the XID out of order, we don't close session here.
        	// it doesn't mean the data integrate has problem.
    		if(pendingQueue.isEmpty()){
    			LOGGER.warn("The request queue is empty, but get packet xid=" + header.getXid());
    			// we don't  close session here.
    			return;
    		}
    		int recvXid = header.getXid();
    		packet = pendingQueue.remove();
    		
    		if (packet.protoHeader.getXid() != recvXid) {
    			if(LOGGER.isDebugEnabled()){
    				LOGGER.debug("Packet xid out of order, type=" + packet.protoHeader.getType() 
    						+ ", queuedXid=" + packet.protoHeader.getXid() + ", xid=" + header.getXid());
    			}
        		
        		// trim the pendingQueue to the received packet.
        		if(packet.protoHeader.getXid() > recvXid){
        			LOGGER.error("Packet xid out of order, drop the received packet, xid=" + header.getXid());
        			pendingQueue.addLast(packet);
        			packet = null;
        		} else {
        			while(packet.protoHeader.getXid() != recvXid){
        				LOGGER.error("Packet xid out of order, drop the queued packet, type=" + packet.protoHeader.getType() 
                				+ ", queuedXid=" + packet.protoHeader.getXid());
        				packet.respHeader.setErr(ErrorCode.CONNECTION_LOSS);
        				finishPacket(packet);
        				
        				if(! pendingQueue.isEmpty()){
        					packet = pendingQueue.remove();
        				} else {
        					return ;
        				}
        			}
        		}
            }
    	}
        
        PacketLatency.receivePacket(packet);
		packet.respHeader.setXid(header.getXid());
		packet.respHeader.setErr(header.getErr());
		packet.respHeader.setDxid(header.getDxid());
		if (header.getDxid() > 0) {
			lastDxid = header.getDxid();
		}
		if (ErrorCode.OK.equals(header.getErr())) {
			packet.response = response;
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Reading reply sessionid:0x"
					+ session.id + ", packet=" + packet);
		}
		finishPacket(packet);
    	
    }
    
    /**
     * Change the Directory Authentication. 
     * 
     * It will reopen session use the new authentication.
     * If failed, need to close the session.
     * 
     * @param userName
     * @param password
     */
    public void setDirectoryUser(String userName, String password){
    	this.authData = generateDirectoryAuthData(userName, password);
    	if(getStatus().isConnected()){
    		ErrorCode ec = sendConnectProtocol(this.getConnectTimeOut());
    		if(ErrorCode.SESSION_EXPIRED.equals(ec)){
				LOGGER.info("Session Expired, cleanup the client session.");
				closeSession();
			} else if(! ErrorCode.OK.equals(ec)){
				reopenSession();
			}
    	}
    }

    /**
     * Get the ConenctionStatus.
     * @return
     * 		the DirectoryConnection ConenctionStatus.
     */
    public ConnectionStatus getStatus() {
        return status;
    }
    
    /**
     * Set the DirectoryConnection ConenctionStatus.
     * 
     * @param status
     * 		the ConenctionStatus.
     */
    private void setStatus(ConnectionStatus status){
    	if(LOGGER.isDebugEnabled()){
    		LOGGER.debug("Set status to - " + status);
    	}
    	if(! this.status.equals(status)){
    		ConnectionStatus pre = this.status;
    		this.status = status;
    		eventThread.queueClientEvent(new ClientStatusEvent(pre, status));
    	}
    }
    
    /**
     * Register a ServiceDirectoryListener.
     * 
     * @param listener
     * 		the ServiceDirectoryListener.
     */
    public void registerClientChangeListener(ServiceDirectoryListener listener){
    	synchronized(statusListeners){
    		this.statusListeners.add(listener);
    	}
    }
    
    /**
     * Unregister the ServiceDirectoryListener.
     * 
     * @param listener
     * 		the ServiceDirectoryListener.
     */
    public void unregisterClientChangeListener(ServiceDirectoryListener listener){
    	synchronized(statusListeners){
    		this.statusListeners.remove(listener);
    	}
    }
    
    /**
     * On the DirectoryConnection setup connection to DirectoryServer.
     * 
     * @param serverSessionTimeout
     * 		the session timeout.
     * @param sessionId
     * 		the session id.
     * @param sessionPassword
     * 		the session password.
     * @param serverId
     * 		the remote server id.
     */
    public void onConnected(int serverSessionTimeout, String sessionId, byte[] sessionPassword, int serverId) {
    	
        if (serverSessionTimeout <= 0) {
        	closeSession();
            LOGGER.error("Unable to reconnect to Directory Server, session 0x" + sessionId + " has expired");
            return;
        }
        
        boolean reopen = session.id == null || session.id.equals("") ? false : true;
		session.timeOut = serverSessionTimeout;
		session.id = sessionId;
		session.password = sessionPassword;
		session.serverId = serverId;
		if(getStatus().isAlive()){
			setStatus(ConnectionStatus.CONNECTED);
		}
		if(reopen){
			eventThread.queueClientEvent(new ClientSessionEvent(SessionEvent.REOPEN));
		} else {
			eventThread.queueClientEvent(new ClientSessionEvent(SessionEvent.CREATED));
		}
		
        LOGGER.info("Session establishment complete on server " + this.clientSocket.getRemoteSocketAddress()
                + ", sessionid = 0x" + sessionId + ", session timeout = " + session.timeOut
                + ", serverId=" + session.serverId);
        
    }
    
    /**
     * On the DirectorySocket has error.
     */
    public void onSocketError(){
    	reopenSession();
    }
    
    /**
     * Send the CloseSession packet.
     * 
     * @throws IOException
     * 		the IOException.
     */
    private void sendCloseSession() throws IOException{
    	ProtocolHeader h = new ProtocolHeader();
        h.setType(ProtocolType.CloseSession);

        sendAdminPacket(h, null);
    }
    
    /**
     * Connect to the remote DirectoryServer.
     * 
     * @throws IOException
     */
    private void doConnect() throws SessionTimeOutException{
    	long to = getConnectTimeOut();
		
		if(clientSocket.isConnected()){
			ErrorCode ec = sendConnectProtocol(to);
			// CONNECTION_LOSS means client data is inconsistence with server. now we check the last dxid of client.
			// if dxid of the client is bigger than server actual dxid, return CONNECTION_LOSS.
			if(ErrorCode.SESSION_EXPIRED.equals(ec) || ErrorCode.CONNECTION_LOSS.equals(equals(ec))){
				LOGGER.info("Session Expired, cleanup the client session.");
				cleanupSession();
			} 
		}
    }
    
    /**
     * Send the Connect Protocol to the remote Directory Server.
     * 
     * @param to
     * 		the connection timeout.
     * @return
     * 		the ErrorCode of the request, OK for success.
     */
    private ErrorCode sendConnectProtocol(long to) {
    	String sessId = session.id;
    	ErrorCode ec = ErrorCode.OK;
        
        ConnectProtocol conReq = new ConnectProtocol(0, lastDxid,
                session.timeOut, sessId, session.password, authData.userName, authData.secret, authData.obfuscated);
        ServiceDirectoryFuture future = submitAsyncRequest(new ProtocolHeader(0, ProtocolType.CreateSession), conReq, null);
		
        try {
			ConnectResponse resp = null;
			if (future.isDone()) {
				resp = (ConnectResponse) future.get();
			} else {
				resp = (ConnectResponse) future.get(to, TimeUnit.MILLISECONDS);
			}
			onConnected(resp.getTimeOut(), resp.getSessionId(), resp.getPasswd(), resp.getServerId());
			return ec;
		} catch (ExecutionException e) {
			// ConnectResponse failed, renew session.
			ServiceException se = (ServiceException) e.getCause();
			ec = se.getServiceDirectoryError().getExceptionCode();
			
		} catch (Exception e) {
			ec = ErrorCode.GENERAL_ERROR;
		} 
        future.cancel(false);
        return ec;
    }
    /**
	 * Send the packet to Directory Server directory.
	 * 
	 * It doesn't queue the packet in the pendingQueue. It used internally to send 
	 * administration Protocol.
	 * 
	 * @param header
	 * @param protocol
	 * @throws IOException
	 */
	private void sendAdminPacket(ProtocolHeader header, Protocol protocol) throws IOException{
		clientSocket.sendPacket(header, protocol);
	}
	
	/**
	 * The Packet finished.
	 * 
	 * @param p
	 * 		the Packet.
	 */
	private void finishPacket(Packet p) {
		
        if (p.watcherRegistration != null) {
        	if(ErrorCode.OK.equals(p.respHeader.getErr())){
        		this.watcherManager.register(p.watcherRegistration);
        	}
        }

		synchronized (p) {
			p.finished = true;
			PacketLatency.finishPacket(p);
			if (p.cb == null && p.future == null) {
				p.notifyAll();
			}
		}
        
        eventThread.queuePacket(p);
        
    }

	/**
	 * On the Packet lost in the DirectoryConnection.
	 * 
	 * @param p
	 * 		the Packet.
	 */
    private void onLossPacket(Packet p) {
        if (p.respHeader == null) {
            p.respHeader = new ResponseHeader(-1, -1, ErrorCode.OK);
        }
        switch (getStatus()) {
        case AUTH_FAILED:
            p.respHeader.setErr(ErrorCode.AUTHENT_FAILED);
            break;
        case CLOSED:
            p.respHeader.setErr(ErrorCode.CLIENT_CLOSED);
            break;
        default:
            p.respHeader.setErr(ErrorCode.CONNECTION_LOSS);
        }
        finishPacket(p);
    }
    
    /**
     * On the session close.
     */
    private void onSessionClose(){
    	synchronized(pendingQueue){
    		while(! pendingQueue.isEmpty()){
    			Packet p = pendingQueue.remove();
    			onLossPacket(p);
    		}
    	}
    }
    
    /**
     * It cleans the session, including the sessionId, sessionPassword.
     * 
     * Close session doesn't close the whole connection, the ConnectionThread will
     * try to do connect soon.
     */
    private void closeSession(){
    	if(LOGGER.isDebugEnabled()){
    		LOGGER.debug("Close the session, sessionId=" + session.id + ", timeOut=" + session.timeOut);
    	}
    	if(! closing && getStatus().isConnected()){
	    	closing = true;
	    	if(getStatus().isAlive()){
	    		setStatus(ConnectionStatus.NOT_CONNECTED);
	    	}
	    	cleanupSession();
	    	closing = false;
    	}
    }
    
    /**
     * Cleanup the session in the DirectoryConnection.
     */
    private void cleanupSession(){
    	eventThread.queueClientEvent(new ClientSessionEvent(SessionEvent.CLOSED));
    	watcherManager.cleanup();
    	session.id = "";
    	session.password = null;
    	session.serverId = -1;
    	onSessionClose();
    }
    
    /**
     * When connect has exception and the session still not out of time.
     * Reopen the session to server.
     * 
     * It doesn't clean the sessionId, sessionPassword, authdata and serverId. the ConnectionThread can
     * reopen the session use them.
     * 
     * When send ping failed or ClientSocket detects fail, reopen the session again.
     * Send packet failed donot reopen the session.
     */
    private void reopenSession(){
    	if(! closing && getStatus().isConnected()){
	    	closing = true;
	    	if(getStatus().isAlive()){
	    		setStatus(ConnectionStatus.NOT_CONNECTED);
	    	}
	    	closing = false;
    	}
    }
    
    /**
     * Send the Ping Request.
     * 
     * @return
     * 		the ErrorCode, OK for success.
     * @throws IOException
     * 		the IOException.
     */
    private ErrorCode sendPing() throws IOException {
        if(LOGGER.isTraceEnabled()){
    		LOGGER.trace("......................send Ping");
    	}
        lastPingSentNs = System.currentTimeMillis();
        ProtocolHeader h = new ProtocolHeader(-2, ProtocolType.Ping);
        sendAdminPacket(h, null);
        
        int waitTime = session.pingWaitTimeOut;
        
        synchronized(pingResponse){
        	while(waitTime > 0){
        		try {
					pingResponse.wait(waitTime);
				} catch (InterruptedException e) {
					// Do nothing.
				}
        		
				ResponseHeader header = pingResponse.get();
				if (header != null) {
					pingResponse.set(null);
					return header.getErr();
				}
				waitTime -= (System.currentTimeMillis() - lastPingSentNs);
        	}
        }
        return ErrorCode.PING_TIMEOUT;
    }
    
    /**
     * Get the ConnectTimeOut.
     * 
     * @return
     * 		the Connect timeout.
     */
    private int getConnectTimeOut(){
    	return session.timeOut;
    }
    
    /**
     * Generate the obfuscated auth data.
     * 
     * @param userName
     * 		the user name.
     * @param password
     * 		the password.
     * @return
     * 		the AuthData.
     */
    private AuthData generateDirectoryAuthData(String userName, String password)  {
    	
    	if(password != null && ! password.isEmpty()){
    		byte[] secret = ObfuscatUtil.base64Encode(password.getBytes());
    		return new AuthData(AuthScheme.DIRECTORY, userName, secret, true);
    	} else {
    		return new AuthData(AuthScheme.DIRECTORY, userName, null, false);
    	}
    }
	
    /**
     * the current time in ms.
     */
	private long now = 0;
	
	/**
	 * last time receive response from Directory Server in ms.
	 */
	private long lastRecvMilli = 0;
	
	/**
	 * Update the current time.
	 */
	private void updateNow(){
		now = System.currentTimeMillis();
	}
	
	/**
	 * Update the last receive response time.
	 */
	private void updateRecv(){
		lastRecvMilli = System.currentTimeMillis();
	}
	
	/**
	 * Get the idle of the Receive.
	 * 
	 * @return
	 * 		the time gap in ms.
	 */
	private long getRecvIdle(){
		return now - lastRecvMilli;
	}
	
	/**
	 * The Packet of the Request.
	 * 
	 * @author zuxiang
	 *
	 */
	public static class Packet {
		/**
		 * The ProtocolHeader.
		 */
        ProtocolHeader protoHeader;

        /**
         * The ResponseHeader.
         */
        ResponseHeader respHeader;

        /**
         * The Protocol.
         */
        Protocol protocol;

        /**
         * The Response.
         */
        Response response;

        /**
         * Indicate whether the Packet complete.
         */
        boolean finished;

        /**
         * The Callback.
         */
        ProtocolCallback cb;

        /**
         * The ServiceDirectoryFuture.
         */
        ServiceDirectoryFuture future;
        
        /**
         * The context Object of the Callback.
         */
        Object context;

        /**
         * Indicate whether the request is readonly.
         */
        public boolean readOnly;
        
        /**
         * The WatcherRegistration.
         */
        WatcherRegistration watcherRegistration;
        
        /**
         * The create time of the Packet.
         */
        long createTime;

        /**
         * The constructor.
         * 
         * @param requestHeader
         * 		the ProtocolHeader.
         * @param request
         * 		the Protocol
         * @param watcherRegistration
         * 		the WatcherRegistration.
         */
        Packet(ProtocolHeader requestHeader, 
        		Protocol request, WatcherRegistration watcherRegistration) {
            this(requestHeader,  request, 
            		watcherRegistration, false);
        }

        /**
         * The constructor.
         * 
         * @param protoHeader
         * 		the ProtocolHeader.
         * @param protocol
         * 		the Protocol.
         * @param watcherRegistration
         * 		the WatcherRegistration.
         * @param readOnly
         * 		readOnly flag.
         */
        Packet(ProtocolHeader protoHeader, 
        		Protocol protocol, WatcherRegistration watcherRegistration, boolean readOnly) {

            this.protoHeader = protoHeader;
            this.protocol = protocol;
            this.readOnly = readOnly;
            this.watcherRegistration = watcherRegistration;
            this.respHeader = new ResponseHeader();
        }
        
        /**
         * Set the createTime.
         * 
         * @param createTime
         * 		the createTime.
         */
        public void setCreateTime(long createTime){
        	this.createTime = createTime;
        }
        
        /**
         * Get the createTime.
         * 
         * @return
         * 		the createTime.
         */
        public long getCreateTime(){
        	return this.createTime;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("finished=" + finished);
            sb.append(", protoHeader=" + protoHeader);
            sb.append(", respHeader=" + respHeader);
            sb.append(", protocol=" + protocol);
            sb.append(", response=" + response);

            return sb.toString();
        }
    }
	
	/**
	 * The Session of the DirectoryServer.
	 * 
	 * @author zuxiang
	 *
	 */
	class Session {
		/**
		 * The SessionId.
		 */
		private String id = "";
		
		/**
		 * The remote server id.
		 */
	    private int serverId = -1;
	    
	    /**
	     * The sesion password.
	     */
	    private byte password[] = null;
	    
	    // Time out in milli seconds.
	    private int timeOut = SESSION_TIMEOUT_DEFAULT_VALUE;
	    
	    // The time ping request wait for.
	    private int pingWaitTimeOut = timeOut / 4;
	}
	
	/**
	 * The EventThread to handle the event.
	 * 
	 * @author zuxiang
	 *
	 */
	class EventThread extends Thread {
		/**
		 * The event Queue.
		 */
        private final LinkedBlockingQueue<Object> waitingEvents =
            new LinkedBlockingQueue<Object>();

        /**
         * Indicate whether killed.
         */
       private volatile boolean wasKilled = false;
       
       /**
        * Indicate whether running.
        */
       private volatile boolean isRunning = false;

       /**
        * Constructor.
        */
       EventThread() {
    	   super("SD-EventThread");
    	   setDaemon(true);
       }
        
       /**
        * Queue a Server Event.
        * 
        * @param event
        * 		the ServerEvent.
        */
       public void queueServerEvent(ServerEvent event){
    	   waitingEvents.add(event);
       }
        
       /**
        * Queue a Client Event.
        * 
        * @param event
        * 		the ClientEvent.
        */
       public void queueClientEvent(ServiceDirectoryEvent event){
    	   waitingEvents.add(event);
       }

       /**
        * Queue a Watcher Event.
        * 	
        * @param event
        * 		the WatcherEvent.
        */
       public void queueWatcherEvent(WatcherEvent event) {
    	   waitingEvents.add(event);
       }

       /**
        * Queue a Packet.
        * 
        * @param packet
        * 	The Packet.
        */
       public void queuePacket(Packet packet) {
          if (wasKilled) {
             synchronized (waitingEvents) {
                if (isRunning) waitingEvents.add(packet);
                else processEvent(packet);
             }
          } else {
             waitingEvents.add(packet);
          }
       }

       /**
        * Queue the death event.
        */
        public void queueEventOfDeath() {
            waitingEvents.add(eventOfDeath);
        }

        /**
         * The main run method.
         */
        @Override
        public void run() {
           try {
              isRunning = true;
              while (true) {
                 Object event = waitingEvents.take();
                 if (event == eventOfDeath) {
                    wasKilled = true;
                 } else {
                    processEvent(event);
                 }
                 if (wasKilled)
                    synchronized (waitingEvents) {
                       if (waitingEvents.isEmpty()) {
                          isRunning = false;
                          break;
                       }
                    }
              }
           } catch (InterruptedException e) {
              LOGGER.error("Event thread exiting due to interruption", e);
           }

           LOGGER.info("EventThread shut down");
        }

        /**
         * Process the event.
         * 
         * The EventThread process the ServerEvent, ClientEvent and Packet.
         * 
         * @param event
         * 		the Event Object.
         */
        private void processEvent(Object event) {
        	try {
        		if(LOGGER.isDebugEnabled()){
        			LOGGER.debug("Process event - " + event);
        		}
              if (event instanceof Event) {
            	  EventType type = ((Event) event).getEventType();
            	  if(EventType.Server.equals(type)){
            		  
            	  } else if(EventType.Watcher.equals(type)){
            		  WatcherEvent watcherEvent = (WatcherEvent) event;
            		  
            		  if(watcherEvent.getServices() != null && watcherEvent.getServices().size() > 0){
	            		  for(WatchedService wService : watcherEvent.getServices()){
	            			  String serviceName = wService.getService().getName();
	            			  List<Watcher> watchers = watcherManager.getWatchers(serviceName);
	            			  
	            			  if(watchers == null || watchers.size() == 0
	            					  || wService.getServiceInstanceEvents() == null || wService.getServiceInstanceEvents().size() == 0){
	            				  continue;
	            			  }
	            			  
	            			  for(ServiceInstanceEvent instanceEvent : wService.getServiceInstanceEvents()){
	            				  String instanceId = instanceEvent.getInstanceId();
	            				  ModelServiceInstance serviceInstance = null;
	            				  if(! OperateType.Delete.equals(instanceEvent.getOperateType())){
	            					  for(ModelServiceInstance k : watcherEvent.getServiceInstances()){
	            						  if(serviceName.equals(k.getServiceName()) && instanceId.equals(k.getInstanceId())){
	            							  serviceInstance = k;
	            						  }
	            					  }
	            				  }
	            				  ServiceInstanceOperate o = new ServiceInstanceOperate(serviceName, instanceId, 
	            						  serviceInstance, instanceEvent.getOperateType());
	            				  for(Watcher w : watchers){
	            					  try{
	            						  w.process(serviceName, o);
	            					  } catch(Exception e){
	            						  LOGGER.warn("Watcher process failed, name=" + serviceName, e);
	            					  }
	            				  }
	            			  }
	            		  }
            		  }
            		  
            	  } else {
            		  LOGGER.error("Unkonwn event type - " + type);
            	  }
              } else if(event instanceof ServiceDirectoryEvent){
					ServiceDirectoryEvent clientEvent = (ServiceDirectoryEvent) event;
					List<ServiceDirectoryListener> list = null;
					synchronized (statusListeners) {
						if (statusListeners.isEmpty()) {
							list = Collections.emptyList();
						} else {
							list = new ArrayList<ServiceDirectoryListener>(
									statusListeners);
						}
					}
					for (ServiceDirectoryListener listener : list) {
						listener.notify(clientEvent);
					}
              }else {
                  Packet p = (Packet) event;
                  ErrorCode rc = ErrorCode.OK;
                  if (! ErrorCode.OK.equals(p.respHeader.getErr())) {
                      rc = p.respHeader.getErr();
                  }
                  PacketLatency.callbackPacket(p);
                  if(p.cb != null){
                	  Response rsp = p.response;
                	  ProtocolCallback cb = (ProtocolCallback) p.cb;
                	  if (ErrorCode.OK.equals(rc)) {
                		  cb.call(true, rsp, null, p.context);
                	  } else {
                		  cb.call(false, rsp, rc, p.context);
                	  }
                  }
                  
                  if(p.future != null){
                	  if(ErrorCode.OK.equals(rc)){
                		  if(! p.future.isCancelled()){
                			  p.future.complete(p.response);
                		  }
                		  
                	  } else {
                		  
                		  p.future.fail(new ServiceException(new ServiceDirectoryError(rc)));
                	  }
                  }
                  
              }
          } catch (Throwable t) {
        	  LOGGER.error("Caught unexpected throwable", t);
          }
       }
    }
	
	/**
	 * On the DirectoryConnection session timeout.
	 */
	private void onSessionTimeOut(){
		closeSession();
	}
	
	/**
	 * On the Ping failed.
	 * 
	 * @param code
	 * 		the ErrorCode.
	 */
	private void onPingFailed(ErrorCode code){
		
	}
	
	/**
	 * The AuthData.
	 * 
	 * @author zuxiang
	 *
	 */
	static class AuthData {
		/**
		 * The AuthScheme.
		 */
		AuthScheme scheme;
		
		/**
		 * The userName.
		 */
        String userName;

        /**
         * The secret byte array.
         */
        byte secret[];
        
        /**
         * Whether obfuscated.
         */
        boolean obfuscated;
        
		/**
		 * The Constructor.
		 * 
		 * @param scheme
		 * 		the AuthScheme.
		 * @param userName
		 * 		the userName.
		 * @param secret
		 * 		the secret byte array.
		 * @param obfuscated
		 * 		whether it obfuscated.
		 */
        AuthData(AuthScheme scheme, String userName, byte secret[], boolean obfuscated) {
            this.scheme = scheme;
            this.secret = secret;
            this.userName = userName;
            this.obfuscated = obfuscated;
        }
    }
	
	/**
	 * Control whether the deamon connect task to run.
	 */
	private AtomicBoolean connectRun = new AtomicBoolean(true);
	
	/**
	 * The deamon connection thread task.
	 * 
	 * @author zuxiang
	 *
	 */
	class ConnectTask implements Runnable {

		@Override
		public void run() {
			while(connectRun.get()){
				try{
					if (!getStatus().isConnected() && !closing) {

						doConnect();
					} else if(getStatus().isConnected()) {
						try {
							updateNow();
							long idle = getRecvIdle();
							
							if(idle > session.timeOut){
								LOGGER.info("Doesn't heared response from Directory Server in timeout " + session.timeOut + ", close the session.");
								onSessionTimeOut();
							} else {
								int sleepTO = (int) (session.timeOut / 2 - idle);
								if(sleepTO > 0){
									try{
										Thread.sleep(sleepTO);
									}catch(Exception e){
										// do noting
									}
								}
								ErrorCode error = sendPing();
								if(! ErrorCode.OK.equals(error)){
									onPingFailed(error);
								}
							}
							
						} catch (IOException ioe) {
							if(LOGGER.isTraceEnabled()){
								LOGGER.trace("Ping get exception.", ioe);
							}
							onPingFailed(null);
						} catch (Exception e){
							if(LOGGER.isTraceEnabled()){
								LOGGER.trace("Ping get exception.", e);
							}
							onPingFailed(null);
						}
					} 
					try {
						Thread.sleep(50);
					} catch (Exception e) {
						// do nothing.
					}
					
				} catch(Throwable e){
					if(LOGGER.isTraceEnabled()){
						LOGGER.trace("connect get error.", e);
					}
				}
			}
			
		}
		
	}
	
}
