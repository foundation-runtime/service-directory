package com.cisco.oss.foundation.directory.connect;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.async.Callback.ProtocolCallback;
import com.cisco.oss.foundation.directory.async.ServiceDirectoryFuture;
import com.cisco.oss.foundation.directory.async.Watcher;
import com.cisco.oss.foundation.directory.async.WatcherManager;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ServerStatus;
import com.cisco.oss.foundation.directory.entity.ServiceInstanceEvent;
import com.cisco.oss.foundation.directory.entity.WatchedService;
import com.cisco.oss.foundation.directory.event.ConnectionStatus;
import com.cisco.oss.foundation.directory.event.ServiceDirectoryEvent;
import com.cisco.oss.foundation.directory.event.ServiceDirectoryEvent.ClientSessionEvent;
import com.cisco.oss.foundation.directory.event.ServiceDirectoryEvent.ClientSessionEvent.SessionEvent;
import com.cisco.oss.foundation.directory.event.ServiceDirectoryEvent.ClientStatusEvent;
import com.cisco.oss.foundation.directory.event.ServiceDirectoryListener;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.impl.DirectoryServiceClient.WatcherRegistration;
import com.cisco.oss.foundation.directory.proto.ConnectProtocol;
import com.cisco.oss.foundation.directory.proto.ConnectResponse;
import com.cisco.oss.foundation.directory.proto.GetServiceProtocol;
import com.cisco.oss.foundation.directory.proto.Protocol;
import com.cisco.oss.foundation.directory.proto.ProtocolHeader;
import com.cisco.oss.foundation.directory.proto.ProtocolType;
import com.cisco.oss.foundation.directory.proto.Response;
import com.cisco.oss.foundation.directory.proto.ResponseHeader;
import com.cisco.oss.foundation.directory.proto.ServerEvent.ServerStatusEvent;
import com.cisco.oss.foundation.directory.proto.ServiceInstanceOperate;
import com.cisco.oss.foundation.directory.proto.ServiceInstanceOperate.OperateType;
import com.cisco.oss.foundation.directory.proto.UnregisterServiceInstanceProtocol;
import com.cisco.oss.foundation.directory.proto.WatcherEvent;

public class TestDirectoryConnection {
	private final static Logger LOGGER = LoggerFactory.getLogger(TestDirectoryConnection.class);

//	@Ignore
	@Test
	public void testInit(){
		LOGGER.info("============testInit=====================");
		final AtomicBoolean cleanupInvoked = new AtomicBoolean(false);
//		final Map<ProtocolType, Integer> sendPacket = new HashMap<ProtocolType, Integer>();
		final Map<String, Integer> eventMap = new ConcurrentHashMap<String, Integer>();
		
		List<String> servers = new ArrayList<String>();
		servers.add("localhost:8901");
		DirectoryServers directoryServers = new DirectoryServers(servers);
		
		DirectorySocket socket = new DirectorySocket(){

			private InetSocketAddress server = null;
			private volatile SocketThread t ;
			@Override
			public boolean isConnected() {
				return true;
			}

			@Override
			public boolean connect(InetSocketAddress address) {
				
				Assert.assertEquals("localhost", address.getHostName());
				Assert.assertEquals(8901, address.getPort()); 
				
				closeSocketThread();
				t = new SocketThread(this.clientConnection);
				t.start();
				this.server = address;
				LOGGER.info("DirectorySocket connect to {}", address);
				return true;
			}
			
			private void closeSocketThread(){
				if(t != null){
					t.toStop();
					t = null;
				}
			}

			@Override
			public SocketAddress getRemoteSocketAddress() {
				return server;
			}

			@Override
			public SocketAddress getLocalSocketAddress() {
				return InetSocketAddress.createUnresolved("localhost", 23456);
			}

			@Override
			public void cleanup() {
				cleanupInvoked.set(true);
				closeSocketThread();
			}

			@Override
			public void sendPacket(ProtocolHeader header, Protocol p)
					throws IOException {
				LOGGER.info("DirectorySocket send packet type={}, xid={}", header.getType(), header.getXid());
				int i = 0;
				if(eventMap.containsKey(header.getType().name())){
					i = eventMap.get(header.getType().name());
				}
				i ++;
				LOGGER.info(header.getType().name() + "<========>" + i);
				eventMap.put(header.getType().name(), i);
				if(ProtocolType.CreateSession.equals((header.getType()))){
					
					ConnectResponse response = new ConnectResponse(0, 4000, "100", null, 1);
					t.queueResonse(new ResponseHeader(header.getXid(), 1, ErrorCode.OK), response);
				}
//				else if(ProtocolType.Ping.equals(header.getType())){
//					if(t != null){
//						t.queueResonse(new ResponseHeader(-2, 1, ErrorCode.OK), null);
//					}
//				}
			}
			
		};
		
		socket.getRemoteSocketAddress();
		
		DirectoryConnection connection = new DirectoryConnection(directoryServers.getNextDirectoryServer(), new WatcherManager(), socket, "user", "password");
		socket.setConnection(connection);
		ServiceDirectoryListener listener = new ServiceDirectoryListener(){

			@Override
			public void notify(ServiceDirectoryEvent event) {
				String et = null;
				if(event instanceof ClientStatusEvent){
					ClientStatusEvent e = (ClientStatusEvent)event;
					et = e.getCurrentStatus().name();
					LOGGER.info("================>ClientStatusEvent, prev={}, current={}", e.getPreviousStatus(), e.getCurrentStatus());
				} else if(event instanceof ClientSessionEvent){
					ClientSessionEvent e = (ClientSessionEvent) event;
					et = "S_" + e.getSessionEvent().name();
					LOGGER.info("================>ClientSessionEvent, event={}", e.getSessionEvent());
				}
				
				
				int i = 0;
				if(eventMap.containsKey(et)){
					i = eventMap.get(et);
				}
				i++;
				LOGGER.info(et + "<========>" + i);
				eventMap.put(et, i);
			}
			
		};
		connection.registerClientChangeListener(listener);
		connection.start();
		
		try {
			boolean b = true;
			for(int j = 0; j < 12; j++){
				Thread.sleep(1000);
				if(b){
					Assert.assertEquals(ConnectionStatus.CONNECTED, connection.getStatus());
					b = false;
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Assert.assertTrue(cleanupInvoked.get());
		Assert.assertTrue(eventMap.get(ProtocolType.CreateSession.name()).intValue() > 1);
		Assert.assertTrue(eventMap.get(ProtocolType.Ping.name()).intValue() > 1);
		Assert.assertTrue(eventMap.get(ConnectionStatus.NOT_CONNECTED.name()).intValue() > 1);
		Assert.assertTrue(eventMap.get(ConnectionStatus.CONNECTED.name()).intValue() > 1);
		Assert.assertTrue(eventMap.get(ConnectionStatus.CLOSED.name()).intValue() == 1);
		Assert.assertTrue(eventMap.get("S_" + SessionEvent.CREATED.name()).intValue() > 1);
		Assert.assertTrue(eventMap.get("S_" + SessionEvent.CLOSED.name()).intValue() > 1);
		Assert.assertTrue(eventMap.get("S_" + SessionEvent.REOPEN.name()) == null);
		
		
	}
	
//	@Ignore
	@Test
	public void testSubmitRequest() throws InterruptedException, ExecutionException{
		LOGGER.info("============testSubmitRequest=====================");
		List<String> servers = new ArrayList<String>();
		servers.add("localhost:8901");
		DirectoryServers directoryServers = new DirectoryServers(servers);
		
		final CustomerDirectorySocket socket = new CustomerDirectorySocket();
		
		final DirectoryConnection connection = new DirectoryConnection(directoryServers.getNextDirectoryServer(), new WatcherManager(), socket, "user", "password");
		socket.setConnection(connection);
		connection.start();
		
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Assert.assertEquals(ConnectionStatus.CONNECTED, connection.getStatus());
		
		ProtocolHeader header = new ProtocolHeader(-100, ProtocolType.GetService);
		Protocol protocol = new GetServiceProtocol();
		
		socket.setProtocolCompare(header, protocol);
		Assert.assertEquals(ConnectResponse.class, connection.submitRequest(header, protocol, null).getClass());
		Assert.assertTrue(header == socket.getLast());
		
		header = new ProtocolHeader(-100, ProtocolType.DeleteUser);
		protocol = new GetServiceProtocol();
		
		socket.setProtocolCompare(header, protocol);
		ServiceDirectoryFuture future = connection.submitAsyncRequest(header, protocol, null);
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(ConnectResponse.class, future.get().getClass());
		Assert.assertTrue(header == socket.getLast());
		
		header = new ProtocolHeader(-100, ProtocolType.UnregisterServiceInstance);
		protocol = new UnregisterServiceInstanceProtocol();
		
		final AtomicInteger cbInvoked = new AtomicInteger(0);
		socket.setProtocolCompare(header, protocol);
		connection.submitCallbackRequest(header, protocol, new ProtocolCallback(){

			@Override
			public void call(boolean result, Response response,
					ErrorCode error, Object ctx) {
				cbInvoked.incrementAndGet();
			}
			
		}, null);
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(ConnectResponse.class, future.get().getClass());
		Assert.assertTrue(header == socket.getLast());
		Assert.assertEquals(1, cbInvoked.get());
		
		try {
			connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
//	@Ignore
	@Test
	public void testSetUser(){
		LOGGER.info("============testSetUser=====================");
		List<String> servers = new ArrayList<String>();
		servers.add("localhost:8901");
		DirectoryServers directoryServers = new DirectoryServers(servers);
		
		DirectorySocket socket = new DirectorySocket(){

			private InetSocketAddress server = null;
			private SocketThread t ;
			@Override
			public boolean isConnected() {
				return true;
			}

			@Override
			public boolean connect(InetSocketAddress address) {
				
				Assert.assertEquals("localhost", address.getHostName());
				Assert.assertEquals(8901, address.getPort()); 
				
				closeSocketThread();
				t = new SocketThread(this.clientConnection);
				t.start();
				this.server = address;
				LOGGER.info("DirectorySocket connect to {}", address);
				return true;
			}
			
			private void closeSocketThread(){
				if(t != null){
					t.toStop();
					t.interrupt();
					t.interrupt();
					t = null;
				}
			}

			@Override
			public SocketAddress getRemoteSocketAddress() {
				return server;
			}

			@Override
			public SocketAddress getLocalSocketAddress() {
				return InetSocketAddress.createUnresolved("localhost", 23456);
			}

			@Override
			public void cleanup() {
				closeSocketThread();
			}

			private boolean first = false;
			@Override
			public void sendPacket(ProtocolHeader header, Protocol p)
					throws IOException {
				LOGGER.info("DirectorySocket send packet type={}, xid={}", header.getType(), header.getXid());
				if(ProtocolType.CreateSession.equals((header.getType()))){
					ConnectProtocol proto = (ConnectProtocol)p;
					if(first){
						Assert.assertEquals("newuser", proto.getUserName());
					} else {
						Assert.assertEquals("user", proto.getUserName());
						first = true;
					}
					ConnectResponse response = new ConnectResponse(0, 4000, "1", null, 1);
					t.queueResonse(new ResponseHeader(header.getXid(), 1, ErrorCode.OK), response);
				}
			}
			
		};
		
		socket.getRemoteSocketAddress();
		
		DirectoryConnection connection = new DirectoryConnection(directoryServers.getNextDirectoryServer(), new WatcherManager(), socket, "user", "password");
		socket.setConnection(connection);
		final AtomicInteger reopenInvoked = new AtomicInteger(0);
		ServiceDirectoryListener listener = new ServiceDirectoryListener(){

			@Override
			public void notify(ServiceDirectoryEvent event) {
				if(event instanceof ClientStatusEvent){
					ClientStatusEvent e = (ClientStatusEvent)event;
					LOGGER.info("================>ClientStatusEvent, prev={}, current={}", e.getPreviousStatus(), e.getCurrentStatus());
				} else if(event instanceof ClientSessionEvent){
					ClientSessionEvent e = (ClientSessionEvent) event;
					LOGGER.info("================>ClientSessionEvent, event={}", e.getSessionEvent());
					if(SessionEvent.REOPEN.equals(e.getSessionEvent())){
						reopenInvoked.incrementAndGet();
					}
				}
			}
			
		};
		connection.registerClientChangeListener(listener);
		connection.start();
		
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Assert.assertEquals(ConnectionStatus.CONNECTED, connection.getStatus());
		connection.setDirectoryUser("newuser", "password");
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(1, reopenInvoked.get());
		
		try {
			connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testWatcher(){
		LOGGER.info("============testWatcher=====================");
		List<String> servers = new ArrayList<String>();
		servers.add("localhost:8901");
		DirectoryServers directoryServers = new DirectoryServers(servers);
		
		DirectorySocket socket = new DirectorySocket(){

			private InetSocketAddress server = null;
			private SocketThread t ;
			@Override
			public boolean isConnected() {
				return true;
			}

			@Override
			public boolean connect(InetSocketAddress address) {
				
				Assert.assertEquals("localhost", address.getHostName());
				Assert.assertEquals(8901, address.getPort()); 
				
				closeSocketThread();
				t = new SocketThread(this.clientConnection);
				t.start();
				this.server = address;
				LOGGER.info("DirectorySocket connect to {}", address);
				return true;
			}
			
			private void closeSocketThread(){
				if(t != null){
					t.toStop();
					t.interrupt();
					t.interrupt();
					t = null;
				}
			}

			@Override
			public SocketAddress getRemoteSocketAddress() {
				return server;
			}

			@Override
			public SocketAddress getLocalSocketAddress() {
				return InetSocketAddress.createUnresolved("localhost", 23456);
			}

			@Override
			public void cleanup() {
				closeSocketThread();
			}

			@Override
			public void sendPacket(ProtocolHeader header, Protocol p)
					throws IOException {
				LOGGER.info("DirectorySocket send packet type={}, xid={}", header.getType(), header.getXid());
				if(ProtocolType.CreateSession.equals((header.getType()))){
					ConnectResponse response = new ConnectResponse(0, 4000, "1", null, 1);
					t.queueResonse(new ResponseHeader(header.getXid(), 1, ErrorCode.OK), response);
				}else if(ProtocolType.GetService.equals(header.getType())){
					ConnectResponse response = new ConnectResponse(0, 4000, "1", null, 1);
					t.queueResonse(new ResponseHeader(header.getXid(), 1, ErrorCode.OK), response);
					
					// -1 means watcher notification
					ModelServiceInstance instance = new ModelServiceInstance("mocksvc", "129.1.1.1-9080", "op", null, 
            				OperationalStatus.UP, "129.1.1.1", 9080, null);
					List<ModelServiceInstance> wInstances = new ArrayList<ModelServiceInstance>();
					wInstances.add(instance);
					List<WatchedService> os = new ArrayList<WatchedService>();
					ModelService mService = new ModelService();
					mService.setName("mocksvc");
					WatchedService wService = new WatchedService(mService);
					wService.getServiceInstanceEvents().add(new ServiceInstanceEvent("mocksvc", "129.1.1.1-9080", OperateType.Update));
					os.add(wService);
		            t.queueResonse(new ResponseHeader(-1, 1, ErrorCode.OK), new WatcherEvent(os, wInstances));
		            
		            
		            instance = new ModelServiceInstance("othersvc", "129.1.1.1-9080", "op", null, 
            				OperationalStatus.UP, "129.1.1.1", 9080, null);
//		            os = new ArrayList<WatchedService>();
//		            os.add(new WatchedService("othersvc", WatcherType.SERVICE, OperateType.Update, 1));
		            wInstances = new ArrayList<ModelServiceInstance>();
					wInstances.add(instance);
					os = new ArrayList<WatchedService>();
					mService = new ModelService();
					mService.setName("othersvc");
					wService = new WatchedService(mService);
					wService.getServiceInstanceEvents().add(new ServiceInstanceEvent("othersvc", "129.1.1.1-9080", OperateType.Update));
					os.add(wService);
		            t.queueResonse(new ResponseHeader(-1, 1, ErrorCode.OK), new WatcherEvent(os, wInstances));
		            
		            
		            instance = new ModelServiceInstance("mocksvc", "ddd", "op", null, 
            				null, "129.1.1.1", 9080, null);
//		            os = new ArrayList<WatchedService>();
//		            os.add(new WatchedService("mocksvc", WatcherType.SERVICE, OperateType.Add, 2));
		            wInstances = new ArrayList<ModelServiceInstance>();
					wInstances.add(instance);
					os = new ArrayList<WatchedService>();
					mService = new ModelService();
					mService.setName("mocksvc");
					wService = new WatchedService(mService);
					wService.getServiceInstanceEvents().add(new ServiceInstanceEvent("mocksvc", "ddd", OperateType.Add));
					os.add(wService);
		            t.queueResonse(new ResponseHeader(-1, 1, ErrorCode.OK), new WatcherEvent(os, wInstances));
		            
		            
//		            os = new ArrayList<WatchedService>();
//		            os.add(new WatchedService("mocksvc", WatcherType.SERVICE, OperateType.Delete, 3));
//		            
					os = new ArrayList<WatchedService>();
					mService = new ModelService();
					mService.setName("mocksvc");
					wService = new WatchedService(mService);
					wService.getServiceInstanceEvents().add(new ServiceInstanceEvent("mocksvc", "ddd", OperateType.Delete));
					os.add(wService);
		            t.queueResonse(new ResponseHeader(-1, 1, ErrorCode.OK), new WatcherEvent(os, null));
				}
			}
			
		};
		
		socket.getRemoteSocketAddress();
		
		DirectoryConnection connection = new DirectoryConnection(directoryServers.getNextDirectoryServer(), new WatcherManager(), socket, "user", "password");
		socket.setConnection(connection);
		ServiceDirectoryListener listener = new ServiceDirectoryListener(){

			@Override
			public void notify(ServiceDirectoryEvent event) {
				if(event instanceof ClientStatusEvent){
					ClientStatusEvent e = (ClientStatusEvent)event;
					LOGGER.info("================>ClientStatusEvent, prev={}, current={}", e.getPreviousStatus(), e.getCurrentStatus());
				} else if(event instanceof ClientSessionEvent){
					ClientSessionEvent e = (ClientSessionEvent) event;
					LOGGER.info("================>ClientSessionEvent, event={}", e.getSessionEvent());
				}
			}
			
		};
		connection.registerClientChangeListener(listener);
		connection.start();
		
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		final Map<String, Integer> watchers = new HashMap<String, Integer>();
		
		Assert.assertEquals(ConnectionStatus.CONNECTED, connection.getStatus());
		WatcherRegistration wr = new WatcherRegistration("mocksvc", new Watcher(){

			@Override
			public void process(String name, 
					ServiceInstanceOperate operate) {
				LOGGER.info("Watcher event - name=" + name + ", OperateType={}", operate.getType()); 
				String n = operate.getType().name();
				int i = 0;
				if(watchers.containsKey(n)){
					i = watchers.get(n);
				}
				i ++;
				watchers.put(n, i);
			}
			
		});
		connection.submitRequest(new ProtocolHeader(3, ProtocolType.GetService), new GetServiceProtocol("mocksvc"), wr) ;
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Assert.assertEquals(3, watchers.size());
		Assert.assertEquals(1, watchers.get("Add").intValue());
		Assert.assertEquals(1, watchers.get("Update").intValue());
		Assert.assertEquals(1, watchers.get("Delete").intValue());
		try {
			connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	@Ignore
	@Test
	public void testServerNotification(){
		LOGGER.info("============testServerNotification=====================");
		List<String> servers = new ArrayList<String>();
		servers.add("localhost:8901");
		DirectoryServers directoryServers = new DirectoryServers(servers);
		
		DirectorySocket socket = new DirectorySocket(){

			private InetSocketAddress server = null;
			private SocketThread t ;
			@Override
			public boolean isConnected() {
				return true;
			}

			@Override
			public boolean connect(InetSocketAddress address) {
				
				Assert.assertEquals("localhost", address.getHostName());
				Assert.assertEquals(8901, address.getPort()); 
				
				closeSocketThread();
				t = new SocketThread(this.clientConnection);
				t.start();
				this.server = address;
				LOGGER.info("DirectorySocket connect to {}", address);
				return true;
			}
			
			private void closeSocketThread(){
				if(t != null){
					t.toStop();
					t.interrupt();
					t.interrupt();
					t = null;
				}
			}

			@Override
			public SocketAddress getRemoteSocketAddress() {
				return server;
			}

			@Override
			public SocketAddress getLocalSocketAddress() {
				return InetSocketAddress.createUnresolved("localhost", 23456);
			}

			@Override
			public void cleanup() {
				closeSocketThread();
			}

			@Override
			public void sendPacket(ProtocolHeader header, Protocol p)
					throws IOException {
				LOGGER.info("DirectorySocket send packet type={}, xid={}", header.getType(), header.getXid());
				if(ProtocolType.CreateSession.equals((header.getType()))){
					ConnectResponse response = new ConnectResponse(0, 4000, "1", null, 1);
					t.queueResonse(new ResponseHeader(header.getXid(), 1, ErrorCode.OK), response);
				}else if(ProtocolType.GetService.equals(header.getType())){
					ConnectResponse response = new ConnectResponse(0, 4000, "1", null, 1);
					t.queueResonse(new ResponseHeader(header.getXid(), 1, ErrorCode.OK), response);
					
					// -1 means watcher notification
					ServerStatusEvent event = new ServerStatusEvent(ServerStatus.SyncConnected);
		            t.queueResonse(new ResponseHeader(-8, 1, ErrorCode.OK), event);
				}
			}
			
		};
		
		socket.getRemoteSocketAddress();
		
		DirectoryConnection connection = new DirectoryConnection(directoryServers.getNextDirectoryServer(), new WatcherManager(), socket, "user", "password");
		socket.setConnection(connection);
		ServiceDirectoryListener listener = new ServiceDirectoryListener(){

			@Override
			public void notify(ServiceDirectoryEvent event) {
				if(event instanceof ClientStatusEvent){
					ClientStatusEvent e = (ClientStatusEvent)event;
					LOGGER.info("================>ClientStatusEvent, prev={}, current={}", e.getPreviousStatus(), e.getCurrentStatus());
				} else if(event instanceof ClientSessionEvent){
					ClientSessionEvent e = (ClientSessionEvent) event;
					LOGGER.info("================>ClientSessionEvent, event={}", e.getSessionEvent());
				}
			}
			
		};
		connection.registerClientChangeListener(listener);
		connection.start();
		
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Assert.assertEquals(ConnectionStatus.CONNECTED, connection.getStatus());
		connection.submitRequest(new ProtocolHeader(3, ProtocolType.GetService), new GetServiceProtocol("mocksvc"), null) ;
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	@Ignore
	@Test
	public void testPackageSequence(){
		LOGGER.info("============testPackageSequence=====================");
		List<String> servers = new ArrayList<String>();
		servers.add("localhost:8901");
		DirectoryServers directoryServers = new DirectoryServers(servers);
		
		final AtomicInteger count = new AtomicInteger(0);
		DirectorySocket socket = new DirectorySocket(){

			private InetSocketAddress server = null;
			private SocketThread t ;
			@Override
			public boolean isConnected() {
				return true;
			}

			@Override
			public boolean connect(InetSocketAddress address) {
				
				Assert.assertEquals("localhost", address.getHostName());
				Assert.assertEquals(8901, address.getPort()); 
				
				closeSocketThread();
				t = new SocketThread(this.clientConnection);
				t.start();
				this.server = address;
				LOGGER.info("DirectorySocket connect to {}", address);
				return true;
			}
			
			private void closeSocketThread(){
				if(t != null){
					t.toStop();
					t.interrupt();
					t.interrupt();
					t = null;
				}
			}

			@Override
			public SocketAddress getRemoteSocketAddress() {
				return server;
			}

			@Override
			public SocketAddress getLocalSocketAddress() {
				return InetSocketAddress.createUnresolved("localhost", 23456);
			}

			@Override
			public void cleanup() {
				closeSocketThread();
			}

			
			@Override
			public void sendPacket(ProtocolHeader header, Protocol p)
					throws IOException {
				LOGGER.info("DirectorySocket send packet type={}, xid={}", header.getType(), header.getXid());
				if(ProtocolType.CreateSession.equals((header.getType()))){
					ConnectResponse response = new ConnectResponse(0, 4000, "1", null, 1);
					t.queueResonse(new ResponseHeader(header.getXid(), 1, ErrorCode.OK), response);
					count.set(header.getXid());
				} else if(ProtocolType.CloseSession.equals(header.getType())){
					
				} else if(ProtocolType.Ping.equals(header.getType())){
					t.queueResonse(new ResponseHeader(-2, 2, ErrorCode.OK), new Response());
				} else{
					ConnectResponse response = new ConnectResponse(0, 4000, "1", null, 1);
					Assert.assertEquals(count.incrementAndGet(), header.getXid());
					t.queueResonse(new ResponseHeader(header.getXid(), 1, ErrorCode.OK), response);
					
				}
			}
			
		};
		
		socket.getRemoteSocketAddress();
		
		final DirectoryConnection connection = new DirectoryConnection(directoryServers.getNextDirectoryServer(), new WatcherManager(), socket, "user", "password");
		socket.setConnection(connection);
		ServiceDirectoryListener listener = new ServiceDirectoryListener(){

			@Override
			public void notify(ServiceDirectoryEvent event) {
				if(event instanceof ClientStatusEvent){
					ClientStatusEvent e = (ClientStatusEvent)event;
					LOGGER.info("================>ClientStatusEvent, prev={}, current={}", e.getPreviousStatus(), e.getCurrentStatus());
				} else if(event instanceof ClientSessionEvent){
					ClientSessionEvent e = (ClientSessionEvent) event;
					LOGGER.info("================>ClientSessionEvent, event={}", e.getSessionEvent());
				}
			}
			
		};
		connection.registerClientChangeListener(listener);
		connection.start();
		
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		final AtomicInteger finish = new AtomicInteger(0);
		Assert.assertEquals(ConnectionStatus.CONNECTED, connection.getStatus());
		new Thread(){
			@Override
			public void run(){
				for(int j = 0; j < 100; j++){
					connection.submitRequest(new ProtocolHeader(3, ProtocolType.GetService), new GetServiceProtocol("mocksvc"), null) ;
				}
				finish.incrementAndGet();
			}
		}.start();
//		
		Thread t1 = new Thread(){
			@Override
			public void run(){
				for(int j = 0; j < 100; j++){
					connection.submitAsyncRequest(new ProtocolHeader(3, ProtocolType.GetService), new GetServiceProtocol("mocksvc"), null) ;
				}
				finish.incrementAndGet();
			}
		};
		t1.setName("LOOP_THREAD_1");
		t1.start();
		
		Thread t = new Thread(){
			@Override
			public void run(){
				for(int j = 0; j < 100; j++){
					connection.submitCallbackRequest(new ProtocolHeader(3, ProtocolType.GetService), new GetServiceProtocol("mocksvc"), new ProtocolCallback(){

						@Override
						public void call(boolean result, Response response,
								ErrorCode error, Object ctx) {
							// TODO Auto-generated method stub
							
						}
						
					}, null) ;
				}
				finish.incrementAndGet();
			}
		};
		t.setName("LOOP_THREAD_2");
		t.start();
		
		try {
			t.join();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Assert.assertEquals(3, finish.get());
		Assert.assertEquals(302, count.get());
		try {
			connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	@Ignore
	@Test
	public void testPacketLoss(){
		LOGGER.info("============testPacketLoss=====================");
		List<String> servers = new ArrayList<String>();
		servers.add("localhost:8901");
		DirectoryServers directoryServers = new DirectoryServers(servers);
		
		final AtomicInteger count = new AtomicInteger(0);
		DirectorySocket socket = new DirectorySocket(){

			private InetSocketAddress server = null;
			private SocketThread t ;
			@Override
			public boolean isConnected() {
				return true;
			}

			@Override
			public boolean connect(InetSocketAddress address) {
				
				Assert.assertEquals("localhost", address.getHostName());
				Assert.assertEquals(8901, address.getPort()); 
				
				closeSocketThread();
				t = new SocketThread(this.clientConnection);
				t.start();
				this.server = address;
				LOGGER.info("DirectorySocket connect to {}", address);
				return true;
			}
			
			private void closeSocketThread(){
				if(t != null){
					t.toStop();
					t.interrupt();
					t.interrupt();
					t = null;
				}
			}

			@Override
			public SocketAddress getRemoteSocketAddress() {
				return server;
			}

			@Override
			public SocketAddress getLocalSocketAddress() {
				return InetSocketAddress.createUnresolved("localhost", 23456);
			}

			@Override
			public void cleanup() {
				closeSocketThread();
			}

			
			@Override
			public void sendPacket(ProtocolHeader header, Protocol p)
					throws IOException {
				LOGGER.info("DirectorySocket send packet type={}, xid={}", header.getType(), header.getXid());
				if(ProtocolType.CreateSession.equals((header.getType()))){
					ConnectResponse response = new ConnectResponse(0, 4000, "1", null, 1);
					t.queueResonse(new ResponseHeader(header.getXid(), 1, ErrorCode.OK), response);
					count.set(header.getXid());
				} else if(ProtocolType.CloseSession.equals(header.getType())){
					
				} else if(ProtocolType.Ping.equals(header.getType())){
					t.queueResonse(new ResponseHeader(-2, 2, ErrorCode.OK), new Response());
				} else{
					ConnectResponse response = new ConnectResponse(0, 4000, "1", null, 1);
					Assert.assertEquals(count.incrementAndGet(), header.getXid());
					if(header.getXid() > 4){
						t.queueResonse(new ResponseHeader(header.getXid(), 1, ErrorCode.OK), response);
					}
					
				}
			}
			
		};
		
		socket.getRemoteSocketAddress();
		
		final DirectoryConnection connection = new DirectoryConnection(directoryServers.getNextDirectoryServer(), new WatcherManager(), socket, "user", "password");
		socket.setConnection(connection);
		ServiceDirectoryListener listener = new ServiceDirectoryListener(){

			@Override
			public void notify(ServiceDirectoryEvent event) {
				if(event instanceof ClientStatusEvent){
					ClientStatusEvent e = (ClientStatusEvent)event;
					LOGGER.info("================>ClientStatusEvent, prev={}, current={}", e.getPreviousStatus(), e.getCurrentStatus());
				} else if(event instanceof ClientSessionEvent){
					ClientSessionEvent e = (ClientSessionEvent) event;
					LOGGER.info("================>ClientSessionEvent, event={}", e.getSessionEvent());
				}
			}
			
		};
		connection.registerClientChangeListener(listener);
		connection.start();
		
		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Assert.assertEquals(ConnectionStatus.CONNECTED, connection.getStatus());
		
		ServiceDirectoryFuture f1 = connection.submitAsyncRequest(new ProtocolHeader(3, ProtocolType.GetService), new GetServiceProtocol("mocksvc"), null) ;
		ServiceDirectoryFuture f2 = connection.submitAsyncRequest(new ProtocolHeader(3, ProtocolType.GetService), new GetServiceProtocol("mocksvc"), null) ;
		ServiceDirectoryFuture f3 = connection.submitAsyncRequest(new ProtocolHeader(3, ProtocolType.GetService), new GetServiceProtocol("mocksvc"), null) ;
		ServiceDirectoryFuture f4 = connection.submitAsyncRequest(new ProtocolHeader(3, ProtocolType.GetService), new GetServiceProtocol("mocksvc"), null) ;
		
		try {
			f1.get();
			Assert.assertTrue(false);
		} catch (Exception e2) {
			LOGGER.error("future1 error.", e2);
			Assert.assertEquals(ErrorCode.CONNECTION_LOSS, ((ServiceException) e2.getCause()).getServiceDirectoryError().getExceptionCode());
		}
		try {
			f2.get();
			Assert.assertTrue(false);
		} catch (Exception e1) {
			LOGGER.error("future2 error.", e1);
			Assert.assertEquals(ErrorCode.CONNECTION_LOSS, ((ServiceException) e1.getCause()).getServiceDirectoryError().getExceptionCode());
		}
		try {
			f3.get();
		} catch (Exception e1) {
			LOGGER.error("future3 error.", e1);
			Assert.assertTrue(false);
		}  
		try {
			f4.get();
		} catch (Exception e1) {
			LOGGER.error("future4 error.", e1);
			Assert.assertTrue(false);
		} 
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static class SocketThread extends Thread{
		
		private DirectoryConnection connection ;
		private LinkedList<Object> queue = new LinkedList<Object>();
		boolean toRun = true;
		
		public SocketThread(DirectoryConnection connection){
			this.connection = connection;
		}
		
		public void queueResonse(ResponseHeader header, Response response){
			synchronized(queue){
				queue.add(header);
				queue.add(response);
			}
		}
		
		public void toStop(){
			toRun = false;
		}
		@Override
		public void run(){
			while(toRun){
					Object header = null;
					Object response = null;
					synchronized(queue){
						if(! queue.isEmpty()){
							header = queue.poll();
							response = queue.poll();
						}
						if(header != null){
							String type = null;
							if(response != null){
								type = response.getClass().getName();
							}
							LOGGER.info("Get the header from queue, type={}, xid={}", type, ((ResponseHeader) header).getXid());
						}
					}
					if(header != null){
						String type = null;
						if(response != null){
							type = response.getClass().getName();
						}
						LOGGER.info("Send the response, type={}, xid={}", type, ((ResponseHeader) header).getXid());
						connection.onReceivedPesponse((ResponseHeader) header, (Response)response);
					}
				
			}
			LOGGER.info("SocketThread go to dead......");
		}
	}
	
	public static class CustomerDirectorySocket extends DirectorySocket{

		private InetSocketAddress server = null;
		private SocketThread t ;
		@Override
		public boolean isConnected() {
			return true;
		}

		@Override
		public boolean connect(InetSocketAddress address) {
			
			Assert.assertEquals("localhost", address.getHostName());
			Assert.assertEquals(8901, address.getPort()); 
			
			closeSocketThread();
			t = new SocketThread(this.clientConnection);
			t.start();
			this.server = address;
			LOGGER.info("DirectorySocket connect to {}", address);
			return true;
		}
		
		private void closeSocketThread(){
			if(t != null){
				t.toStop();
				t.interrupt();
				t.interrupt();
				LOGGER.info("interrupted=" + Thread.interrupted() + ", isAlive=" + t.isAlive() 
						+ ", getState=" + t.getState() + ", isInterrupted=" + t.isInterrupted());
				t = null;
			}
		}

		@Override
		public SocketAddress getRemoteSocketAddress() {
			return server;
		}

		@Override
		public SocketAddress getLocalSocketAddress() {
			return InetSocketAddress.createUnresolved("localhost", 23456);
		}

		@Override
		public void cleanup() {
			closeSocketThread();
		}

		private ProtocolHeader last = null;
		@Override
		public void sendPacket(ProtocolHeader header, Protocol p)
				throws IOException {
			last = header;
			LOGGER.info("DirectorySocket send packet type={}, xid={}", header.getType(), header.getXid());
			if(ProtocolType.CreateSession.equals((header.getType()))){
				
				ConnectResponse response = new ConnectResponse(0, 4000, "1", null, 1);
				t.queueResonse(new ResponseHeader(header.getXid(), 1, ErrorCode.OK), response);
			} else if(ProtocolType.Ping.equals(header.getType())){
				t.queueResonse(new ResponseHeader(-2, 2, ErrorCode.OK), new Response());
			} else if(ProtocolType.CloseSession.equals(header.getType())){
				
			} else {
				Assert.assertTrue(this.proto == p);
				Assert.assertTrue(this.protoHeader == header);
				t.queueResonse(new ResponseHeader(header.getXid(), 1, ErrorCode.OK), new ConnectResponse(0, 4000, "1", null, 1));
			}
		}
		
		public ProtocolHeader getLast(){
			return last;
		}
		
		private ProtocolHeader protoHeader;
		private Protocol proto;
		
		public void setProtocolCompare(ProtocolHeader header, Protocol protocol){
			this.proto = protocol;
			this.protoHeader = header;
		}
		
	}
}
