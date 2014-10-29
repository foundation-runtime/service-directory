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
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.ServiceDirectoryThread;
import com.cisco.oss.foundation.directory.async.Callback.ProtocolCallback;
import com.cisco.oss.foundation.directory.async.ServiceDirectoryFuture;
import com.cisco.oss.foundation.directory.async.Watcher;
import com.cisco.oss.foundation.directory.async.WatcherManager;
import com.cisco.oss.foundation.directory.entity.AuthScheme;
import com.cisco.oss.foundation.directory.entity.EventType;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstanceEvent;
import com.cisco.oss.foundation.directory.entity.WatchedMetadataKey;
import com.cisco.oss.foundation.directory.entity.WatchedService;
import com.cisco.oss.foundation.directory.entity.WatcherType;
import com.cisco.oss.foundation.directory.event.ConnectionStatus;
import com.cisco.oss.foundation.directory.event.ServiceDirectoryEvent;
import com.cisco.oss.foundation.directory.event.ServiceDirectoryEvent.ClientSessionEvent;
import com.cisco.oss.foundation.directory.event.ServiceDirectoryEvent.ClientSessionEvent.SessionEvent;
import com.cisco.oss.foundation.directory.event.ServiceDirectoryEvent.ClientStatusEvent;
import com.cisco.oss.foundation.directory.event.ServiceDirectoryListener;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceDirectoryError;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.exception.ServiceRuntimeException;
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

public class DirectoryConnection {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(DirectoryConnection.class);
    
    private final static int defaultSessionTimeOut = 4000; 

	public static final int packetLen = 4096*1024;
	
	private Object eventOfDeath = new Object();
	
	private AuthData authData = generateDirectoryAuthData("anonymous", null);
	
    private final LinkedList<Packet> pendingQueue = new LinkedList<Packet>();
    private final List<ServiceDirectoryListener> statusListeners = new ArrayList<ServiceDirectoryListener>();

    
    private volatile boolean closing = false;
    
    private Session session;
    
//    private Random random = new Random(System.nanoTime());
    
    private AtomicInteger xid = new AtomicInteger(1);
    
    private volatile long lastDxid = -1;
    
    private volatile ConnectionStatus status = ConnectionStatus.NEW;
    
    private EventThread eventThread;
    private ConnectionThread connectionThread;
    
    private DirectorySocket clientSocket;
    
    private DirectoryServers directoryServers;
    
    private WatcherManager watcherManager;
    
    public DirectoryConnection(){
    	
    }
    
	public DirectoryConnection(DirectoryServers directoryServers, 
			WatcherManager watcherManager, DirectorySocket clientSocket, String userName, String password){
		this.directoryServers = directoryServers;
		this.watcherManager = watcherManager;
		this.clientSocket = clientSocket;
		session = new Session();
		
		if(userName != null && ! userName.isEmpty()){
			this.authData = this.generateDirectoryAuthData(userName, password);
		}
		clientSocket.setConnection(this);
		clientSocket.setConnectTimeOut(session.timeOut *2 /3);
		eventThread = new EventThread();
	}
	
	public void setDirectoryServers(DirectoryServers directoryServers){
		this.directoryServers = directoryServers;
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
		synchronized(this){
			if (getStatus().isConnected()) {
				return;
			}
			try {
				this.wait();
			} catch (InterruptedException e) {
				LOGGER.warn("Block Util Connected interrupted.");
			}
		}
		
	}
	public void start() {
		setStatus(ConnectionStatus.NOT_CONNECTED);
		eventThread.start();
		connectionThread = new ConnectionThread();
		connectionThread.start();
    }
	
	public String getSessionId(){
		return session.id;
	}
    
    public void close() throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Closing client for session: 0x"
                      + getSessionId());
        }

        try {
        	setStatus(ConnectionStatus.CLOSED);
        	sendCloseSession();
            closeSession();
            eventThread.queueEventOfDeath();
        } catch (ServiceRuntimeException e) {
            LOGGER.warn("Execute the CloseSession Protocol failed when close", e);
        } 
    }
    
    public Response submitRequest(ProtocolHeader h, Protocol request, 
    		WatcherRegistration wr){
        Packet packet = queuePacket(h, request, null, null, null, wr);
        synchronized (packet) {
            while (!packet.finished) {
                try {
					packet.wait();
				} catch (InterruptedException e) {
					throw new ServiceRuntimeException(ErrorCode.REQUEST_INTERUPTED, e);
				}
            }
        }
        if(! packet.respHeader.getErr().equals(ErrorCode.OK)){
        	throw new ServiceRuntimeException(packet.respHeader.getErr());
        }
        return packet.response;
    }
    
    public void submitCallbackRequest(ProtocolHeader h, Protocol request, ProtocolCallback callBack, Object context){
    	queuePacket(h, request, callBack, context, null, null);
    }
    
    public ServiceDirectoryFuture submitAsyncRequest(ProtocolHeader h, Protocol request, WatcherRegistration wr){
    	ServiceDirectoryFuture future = new ServiceDirectoryFuture();
    	queuePacket(h, request, null, null, future, wr);
    	return future;
    }

    public Packet queuePacket(ProtocolHeader header, Protocol protocol,
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
        				+ " after " + ((System.nanoTime() - lastPingSentNs) / 1000000) + "ms");
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

    public ConnectionStatus getStatus() {
        return status;
    }
    
    public void setStatus(ConnectionStatus status){
    	if(LOGGER.isDebugEnabled()){
    		LOGGER.debug("Set status to - " + status);
    	}
    	if(! this.status.equals(status)){
    		ConnectionStatus pre = this.status;
    		this.status = status;
    		eventThread.queueClientEvent(new ClientStatusEvent(pre, status));
    	}
    }
    
    public void registerClientChangeListener(ServiceDirectoryListener listener){
    	synchronized(statusListeners){
    		this.statusListeners.add(listener);
    	}
    }
    
    public void unregisterClientChangeListener(ServiceDirectoryListener listener){
    	synchronized(statusListeners){
    		this.statusListeners.remove(listener);
    	}
    }
    
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
    
    public void onSocketError(){
    	reopenSession();
    }
    
    private void sendCloseSession() throws IOException{
    	ProtocolHeader h = new ProtocolHeader();
        h.setType(ProtocolType.CloseSession);

        sendAdminPacket(h, null);
    }
    
    /**
     * Connect to the remote DirectoryServer, it include two phase.
     * 1. socket connect.
     * 2. setup session and authentication.
     * 
     * This method block connection until success to Connect time out.
     * 
     * @throws IOException
     */
    private void doConnect() throws SessionTimeOutException{
    	boolean connected = false;
    	long to = getConnectTimeOut();
    	
		InetSocketAddress address = directoryServers.getNextDirectoryServer();
		long n = System.currentTimeMillis();
		long left = to;
		
		SessionTimeOutException e = null;
		while(connected == false && e == null){
			if(LOGGER.isTraceEnabled()){
				LOGGER.trace("Socket connect to - " + address + ", left=" + left);
			}
			try {
				if (clientSocket.connect(address)) {
					connected = true;
					LOGGER.info("Open socket to server - " + address);
				} else {
					LOGGER.error("Fail open socket to server - " + address);
				}
			} catch (IOException exception) {
				LOGGER.error("Fail open socket to server - " + exception.getMessage() +  " - " + address );
			}
			left = to - (System.currentTimeMillis() - n);
			
			if(left < 0 ){
				e = new SessionTimeOutException("Socket connect timeout.");
			}
		}
		
		while (! getStatus().isConnected() && e == null) {
			if(LOGGER.isTraceEnabled()){
				LOGGER.trace("CreateConnect to - " + address + ", left=" + left);
			}
			ErrorCode ec = sendConnectProtocol(to - (System.currentTimeMillis() - n));
			// CONNECTION_LOSS means client data is inconsistence with server. now we check the last dxid of client.
			// if dxid of the client is bigger than server actual dxid, return CONNECTION_LOSS.
			if(ErrorCode.SESSION_EXPIRED.equals(ec) || ErrorCode.CONNECTION_LOSS.equals(equals(ec))){
				LOGGER.info("Session Expired, cleanup the client session.");
				cleanupSession();
			} else if(! ErrorCode.OK.equals(ec)){
				left = to - (System.currentTimeMillis() - n);
				if (left < 0) {
					e = new SessionTimeOutException("create session timeout.");
				}
			}
		}
		
		synchronized(this){
			// Notify DirectoryConnection connect finished.
			this.notifyAll();
		}
		if(e != null){
			throw e;
		}
    }
    
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
    	
    	closing = true;
    	if(getStatus().isAlive()){
    		setStatus(ConnectionStatus.NOT_CONNECTED);
    	}
    	cleanupSession();
    	clientSocket.cleanup();
    	closing = false;
    }
    
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
    	closing = true;
    	if(getStatus().isAlive()){
    		setStatus(ConnectionStatus.NOT_CONNECTED);
    	}
//    	onSessionClose();
    	clientSocket.cleanup();
    	closing = false;
    }
    
    private long lastPingSentNs = 0;
    
    private void sendPing() throws IOException {
        if(LOGGER.isTraceEnabled()){
    		LOGGER.trace("......................send Ping");
    	}
        lastPingSentNs = System.nanoTime();
        ProtocolHeader h = new ProtocolHeader(-2, ProtocolType.Ping);
        sendAdminPacket(h, null);
    }
    
    private int getConnectTimeOut(){
    	return session.timeOut;
    }
    
    private AuthData generateDirectoryAuthData(String userName, String password)  {
    	
    	if(password != null && ! password.isEmpty()){
    		byte[] secret = ObfuscatUtil.base64Encode(password.getBytes());
    		return new AuthData(AuthScheme.DIRECTORY, userName, secret, true);
    	} else {
    		return new AuthData(AuthScheme.DIRECTORY, userName, null, false);
    	}
    }
	
	
	
	private long now = 0;
	
	private long lastSendMilli = 0;
	
	private long lastRecvMilli = 0;
	
	private void updateNow(){
		now = System.currentTimeMillis();
	}
	
	private void updateSend(){
		now = System.currentTimeMillis();
		lastSendMilli = now;
	}
	
	private void updateSendAndNow(){
		lastSendMilli = System.currentTimeMillis();
	}
	
	private void updateRecv(){
		lastRecvMilli = System.currentTimeMillis();
	}
	
	private long getRecvIdle(){
		return now - lastRecvMilli;
	}
	
	private long getSendIdle(){
		return now - lastSendMilli;
	}
	
	public static class Packet {
        ProtocolHeader protoHeader;

        ResponseHeader respHeader;

        Protocol protocol;

        Response response;

        boolean finished;

        ProtocolCallback cb;

        ServiceDirectoryFuture future;
        
        Object context;

        public boolean readOnly;
        
        WatcherRegistration watcherRegistration;
        
        long createTime;

        Packet(ProtocolHeader requestHeader, 
        		Protocol request, WatcherRegistration watcherRegistration) {
            this(requestHeader,  request, 
            		watcherRegistration, false);
        }

        Packet(ProtocolHeader protoHeader, 
        		Protocol protocol, WatcherRegistration watcherRegistration, boolean readOnly) {

            this.protoHeader = protoHeader;
            this.protocol = protocol;
            this.readOnly = readOnly;
            this.watcherRegistration = watcherRegistration;
            this.respHeader = new ResponseHeader();
        }
        
        public void setCreateTime(long createTime){
        	this.createTime = createTime;
        }
        
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
	
	class Session {
		private String id = "";
	    private int serverId = -1;
	    
	    private byte password[] = null;
	    // Time out in milli seconds.
	    private int timeOut = defaultSessionTimeOut;
	}
	
	class EventThread extends Thread {
        private final LinkedBlockingQueue<Object> waitingEvents =
            new LinkedBlockingQueue<Object>();

       private volatile boolean wasKilled = false;
       private volatile boolean isRunning = false;

        EventThread() {
            super("SD-EventThread");
            setDaemon(true);
        }
        
        public void queueServerEvent(ServerEvent event){
        	waitingEvents.add(event);
        }
        
        public void queueClientEvent(ServiceDirectoryEvent event){
        	waitingEvents.add(event);
        }

        public void queueWatcherEvent(WatcherEvent event) {
            waitingEvents.add(event);
        }

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

        public void queueEventOfDeath() {
            waitingEvents.add(eventOfDeath);
        }

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
	            			  List<Watcher> watchers = watcherManager.getWatchers(serviceName, WatcherType.SERVICE);
	            			  
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
	            						  w.process(serviceName, WatcherType.SERVICE, o);
	            					  } catch(Exception e){
	            						  LOGGER.warn("Watcher process failed, name=" + serviceName + ", type=" + WatcherType.SERVICE, e);
	            					  }
	            				  }
	            			  }
	            		  }
            		  }
            		  
            		  if(watcherEvent.getMetadataKeys() != null && watcherEvent.getMetadataKeys().size() > 0){
	            		  for(WatchedMetadataKey wKey : watcherEvent.getMetadataKeys()){
	            			  String keyName = wKey.getMetadataKey().getName();
	            			  List<Watcher> watchers = watcherManager.getWatchers(keyName, WatcherType.METADATA);
	            			  
	            			  if(watchers == null || watchers.size() == 0
	            					  || wKey.getServiceInstanceEvents() == null 
	            					  || wKey.getServiceInstanceEvents().size() == 0){
	            				  continue;
	            			  }
	            			  
	            			  for(ServiceInstanceEvent instanceEvent : wKey.getServiceInstanceEvents()){
	            				  String serviceName = instanceEvent.getServiceName();
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
	            						  w.process(keyName, WatcherType.METADATA, o);
	            					  } catch(Exception e){
	            						  LOGGER.warn("Watcher process failed, name=" + keyName + ", type=" + WatcherType.METADATA, e);
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
	
	static class AuthData {
        AuthData(AuthScheme scheme, String userName, byte secret[], boolean obfuscated) {
            this.scheme = scheme;
            this.secret = secret;
            this.userName = userName;
            this.obfuscated = obfuscated;
        }
        
        

        AuthScheme scheme;
        String userName;

        byte secret[];
        boolean obfuscated;
    }
	
	class ConnectionThread extends Thread {
		
		public ConnectionThread(){
			super(ServiceDirectoryThread.getThreadName("Client_Connect_Thread"));
		}
		
		@Override
		public void run(){
			while(getStatus().isAlive() ){
				
				try {
					updateNow();
					if (! getStatus().isConnected() && !closing) {
						try{
							Thread.sleep(100);
						} catch(Exception e){
							// do nothing.
						}
						doConnect();
						if(getStatus().isConnected()){
							updateSendAndNow();
						}
						
						
					} else if(getStatus().isConnected()) {
						int to = (int) (session.timeOut - getRecvIdle());

						if (to < 0) {
							throw new SessionTimeOutException();
						}
						
						int sendTo = (int) (session.timeOut / 2 - getSendIdle());
						
						if(sendTo < 0){
							sendPing();
							updateSend();
						} 
						
						try{
							Thread.sleep(session.timeOut / 2);
						} catch(InterruptedException e){
							// do nothing
						}
						
						
					} 
				} catch (Throwable e) {
					if(! getStatus().isConnected()){
						LOGGER.error("Connect failed, reopen session.");
						if(LOGGER.isTraceEnabled()){
							LOGGER.trace("Connect failed.", e);
						}
						reopenSession();
					} else {
						
						if( e instanceof SessionTimeOutException){
							LOGGER.error("Session timeout exception, close the session.");
							if(LOGGER.isTraceEnabled()){
								LOGGER.trace("Session timeout exception", e);
							}
							reopenSession();
						} else if(e instanceof IOException){
							LOGGER.error("Send Ping failed, reopen the session.");
							if(LOGGER.isTraceEnabled()){
								LOGGER.trace("Send Ping failed.", e);
							}
							reopenSession();
						} else {
							LOGGER.error("Unexpected exception, reopen the session.", e);
							reopenSession();
						}
						
					}
				}
			}
			LOGGER.info("ConnectionThread shutdown");
		}
	}
}
