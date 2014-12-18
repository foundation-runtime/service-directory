package com.cisco.oss.foundation.directory.impl;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.async.Callback.GetServiceCallback;
import com.cisco.oss.foundation.directory.async.Callback.RegistrationCallback;
import com.cisco.oss.foundation.directory.async.ServiceDirectoryFuture;
import com.cisco.oss.foundation.directory.async.Watcher;
import com.cisco.oss.foundation.directory.connect.DirectorySocket;
import com.cisco.oss.foundation.directory.connect.TestDirectoryConnection.SocketThread;
import com.cisco.oss.foundation.directory.entity.ACL;
import com.cisco.oss.foundation.directory.entity.AuthScheme;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstanceEvent;
import com.cisco.oss.foundation.directory.entity.User;
import com.cisco.oss.foundation.directory.entity.WatchedService;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.proto.ConnectProtocol;
import com.cisco.oss.foundation.directory.proto.ConnectResponse;
import com.cisco.oss.foundation.directory.proto.GetACLResponse;
import com.cisco.oss.foundation.directory.proto.GetAllServicesResponse;
import com.cisco.oss.foundation.directory.proto.GetAllUserResponse;
import com.cisco.oss.foundation.directory.proto.GetServiceResponse;
import com.cisco.oss.foundation.directory.proto.GetUserResponse;
import com.cisco.oss.foundation.directory.proto.Protocol;
import com.cisco.oss.foundation.directory.proto.ProtocolHeader;
import com.cisco.oss.foundation.directory.proto.ProtocolType;
import com.cisco.oss.foundation.directory.proto.Response;
import com.cisco.oss.foundation.directory.proto.ResponseHeader;
import com.cisco.oss.foundation.directory.proto.ServiceInstanceOperate;
import com.cisco.oss.foundation.directory.proto.ServiceInstanceOperate.OperateType;
import com.cisco.oss.foundation.directory.proto.WatcherEvent;

public class TestDirectoryServiceClient {
	private final static Logger LOGGER = LoggerFactory.getLogger(TestDirectoryServiceClient.class);
	
	@Test
	public void testInit(){
		List<String> servers = new ArrayList<String>();
		servers.add("localhost:8901");
		
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
				t = new SocketThread(getDirectoryConnection());
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
				closeSocketThread();
			}
			
			boolean firstAuth = true;

			@Override
			public void sendPacket(ProtocolHeader header, Protocol p)
					throws IOException {
				LOGGER.info("DirectorySocket send packet type={}, xid={}", header.getType(), header.getXid());
				if(ProtocolType.CreateSession.equals((header.getType()))){
					if(firstAuth){
						Assert.assertEquals("admin", ((ConnectProtocol)p).getUserName());
						firstAuth = false;
					}
					Assert.assertTrue(((ConnectProtocol)p).isObfuscated());
					ConnectResponse response = new ConnectResponse(0, 4000, "1", null, 1);
					t.queueResonse(new ResponseHeader(header.getXid(), 1, ErrorCode.OK), response);
					
				} else if(ProtocolType.Ping.equals(header.getType())){
					t.queueResonse(new ResponseHeader(-2, 2, ErrorCode.OK), new Response());
				} else{
					ConnectResponse response = new ConnectResponse(0, 4000, "1", null, 1);
					if(t != null){
					t.queueResonse(new ResponseHeader(header.getXid(), 1, ErrorCode.OK), response);
					}
					
				}
			}
			
		};
		
		DirectoryServiceClient client = new DirectoryServiceClient(servers, "admin", "admin", socket);
		Assert.assertTrue(client.getStatus().isConnected());
		client.setUser("user1", "pass");
		
		Assert.assertTrue(client.getStatus().isConnected());
		client.close();
		
	}
	
	@Test
	public void testMethod() throws Exception{
		List<String> servers = new ArrayList<String>();
		servers.add("localhost:8901");
		ResponseHeader respHeader = new ResponseHeader(0, 1, ErrorCode.OK);
		
		CustomerDirectorySocket socket = new CustomerDirectorySocket();
		
		DirectoryServiceClient client = new DirectoryServiceClient(servers, "admin", "admin", socket);
		Assert.assertTrue(client.getStatus().isConnected());
		
//		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.GetMetadata));
//		GetMetadataResponse getMetadataResp = new GetMetadataResponse();
//		socket.setResponse(respHeader, getMetadataResp);
//		ServiceDirectoryFuture future = client.asyncGetMetadata("meta1", null);
//		Assert.assertTrue(future.get() == getMetadataResp);
		
		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.GetService));
		GetServiceResponse getServiceResp = new GetServiceResponse();
		socket.setResponse(respHeader, getServiceResp);
		ServiceDirectoryFuture future = client.asyncGetService("service1", null);
		Assert.assertTrue(future.get() == getServiceResp);
		
		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.CreateUser));
		User user = new User("user1", null);
		client.createUser(user, "password");
		
		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.DeleteUser));
		client.deleteUser("user1");
		
		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.GetACL));
		GetACLResponse getAclResponse = new GetACLResponse();
		socket.setResponse(respHeader, getAclResponse);
		ACL acl = client.getACL(AuthScheme.DIRECTORY, "user1");
		Assert.assertNull(acl);
		
		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.GetAllServices));
		GetAllServicesResponse getAllServicesResponse = new GetAllServicesResponse();
		socket.setResponse(respHeader, getAllServicesResponse);
		List<ModelServiceInstance> all = client.getAllInstances();
		Assert.assertNull(all);
		
		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.GetAllUser));
		GetAllUserResponse getAllUserResponse = new GetAllUserResponse();
		socket.setResponse(respHeader, getAllUserResponse);
		List<User> users = client.getAllUser();
		Assert.assertNull(users);
		
//		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.GetMetadata));
//		GetMetadataResponse getMetadataResponse = new GetMetadataResponse();
//		socket.setResponse(respHeader, getMetadataResponse);
//		ModelMetadataKey meta = client.getMetadata("meta1", null);
//		Assert.assertNull(meta);
		
		
//		client.getMetadata("meta1", new GetMetadataCallback(){
//
//			@Override
//			public void call(boolean result, ModelMetadataKey key,
//					ErrorCode error, Object ctx) {
//				Assert.assertNull(key);
//			}
//			
//		}, null);
		
		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.GetService));
		GetServiceResponse getServiceResponse = new GetServiceResponse();
		socket.setResponse(respHeader, getServiceResponse);
		ModelService service = client.getService("service1", null);
		Assert.assertNull(service);
		
		client.getService("service1", new GetServiceCallback(){

			@Override
			public void call(boolean result, ModelService service,
					ErrorCode error, Object ctx) {
				Assert.assertNull(service);
			}
			
		}, null);
		
		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.GetUser));
		GetUserResponse getUserResponse = new GetUserResponse();
		socket.setResponse(respHeader, getUserResponse);
		User user1 = client.getUser("user1");
		Assert.assertNull(user1);
		
		ProvidedServiceInstance instance = new ProvidedServiceInstance();
		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.RegisterServiceInstance));
		client.registerServiceInstance(instance);
		
		client.registerServiceInstance(instance, new RegistrationCallback(){

			@Override
			public void call(boolean result, ErrorCode error, Object ctx) {
				Assert.assertTrue(result);
			}
			
		}, null);
		
		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.SetACL));
		client.setACL(new ACL());
		
		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.SetUserPassword));
		client.setUserPassword("user1", "newpass");
		
		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.UnregisterServiceInstance));
		client.unregisterServiceInstance("service1", "instance1");
		
		client.unregisterServiceInstance("service1", "instance1", new RegistrationCallback(){

			@Override
			public void call(boolean result, ErrorCode error, Object ctx) {
				Assert.assertTrue(result);
			}
			
		}, null);
		
		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.UpdateServiceInstance));
		client.updateServiceInstance(instance);
		client.updateServiceInstance(instance, new RegistrationCallback(){

			@Override
			public void call(boolean result, ErrorCode error, Object ctx) {
				Assert.assertTrue(result);
			}
			
		}, null);
		
//		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.UpdateHeartbeat));
//		client.updateServiceInstancesHeartbeat(new ArrayList<ServiceInstanceHeartbeat>(), System.currentTimeMillis());
//		client.updateServiceInstancesHeartbeat(new ArrayList<ServiceInstanceHeartbeat>(), System.currentTimeMillis(), new RegistrationCallback(){
//
//			@Override
//			public void call(boolean result, ErrorCode error, Object ctx) {
//				Assert.assertTrue(result);
//			}
//			
//		}, null);
		
		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.UpdateServiceInstanceStatus));
		client.updateServiceInstanceStatus("service", "instance1", OperationalStatus.UP);
		client.updateServiceInstanceStatus("service", "instance1", OperationalStatus.UP, new RegistrationCallback(){

			@Override
			public void call(boolean result, ErrorCode error, Object ctx) {
				Assert.assertTrue(result);
			}
			
		}, null);
		
		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.UpdateServiceInstanceUri));
		client.updateServiceInstanceUri("service", "insance1", "http://ddd.com/");
		client.updateServiceInstanceUri("service", "insance1", "http://ddd.com/", new RegistrationCallback(){

			@Override
			public void call(boolean result, ErrorCode error, Object ctx) {
				Assert.assertTrue(result);
			}
			
		}, null);
		
		socket.setPacketCompare(new ProtocolTypePacketCompare(ProtocolType.UpdateUser));
		client.updateUser(new User());
		
		client.close();
	}
	
	@Test
	public void testWatcher() throws Exception{
		List<String> servers = new ArrayList<String>();
		servers.add("localhost:8901");
		ResponseHeader respHeader = new ResponseHeader(0, 1, ErrorCode.OK);
		
		CustomerDirectorySocket socket = new CustomerDirectorySocket();
		
		DirectoryServiceClient client = new DirectoryServiceClient(servers, "admin", "admin", socket);
		Assert.assertTrue(client.getStatus().isConnected());
		final AtomicInteger serviceInvoked = new AtomicInteger(0);
		
		final ModelServiceInstance instance = new ModelServiceInstance(
				"service1", "127.2.3.1-8901", "0", "http://cisco.com/test/", OperationalStatus.UP, "127.2.3.1", 8901, null);
		Watcher watcher1 = new Watcher(){

			@Override
			public void process(String name, 
					ServiceInstanceOperate operate) {
				LOGGER.info("=============> name=" + name + ", operate=" + operate);
				
				Assert.assertEquals(OperateType.Add, operate.getType());
				Assert.assertTrue(operate.getServiceInstance()== instance);
				serviceInvoked.incrementAndGet();
			}
		};
		GetServiceResponse getServiceResponse = new GetServiceResponse();
		socket.setResponse(respHeader, getServiceResponse);
		ModelService service = client.getService("service1", watcher1);
		Assert.assertNull(service);
		
		ResponseHeader watcherHeader = new ResponseHeader(-1, 1, ErrorCode.OK);
		List<WatchedService> wServices = new ArrayList<WatchedService>();
		ModelService mService = new ModelService();
		mService.setName("service1");
		WatchedService wService = new WatchedService(mService);
		wService.getServiceInstanceEvents().add(new ServiceInstanceEvent("service", instance.getInstanceId(), OperateType.Add));
		wServices.add(wService);
		List<ModelServiceInstance> wInstances = new ArrayList<ModelServiceInstance>();
		wInstances.add(instance);
		WatcherEvent watcherEvent = new WatcherEvent(wServices, wInstances);
		socket.sendResponse(watcherHeader, watcherEvent);
		socket.sendResponse(watcherHeader, watcherEvent);
		socket.sendResponse(watcherHeader, watcherEvent);
		
		Thread.sleep(1000);
		
		Assert.assertEquals(3, serviceInvoked.get());
		
		Assert.assertTrue(client.validateServiceWatcher("service1", watcher1));
		Assert.assertFalse(client.validateServiceWatcher("service2", watcher1));
		
		client.deleteServiceWatcher("service1", watcher1);
		Assert.assertFalse(client.validateServiceWatcher("service1", watcher1));
//		final AtomicInteger metaInvoked = new AtomicInteger(0);
		
//		Watcher watcher2 = new Watcher(){
//
//			@Override
//			public void process(String name, WatcherType type,
//					ServiceInstanceOperate operate) {
//				LOGGER.info("=============> name=" + name + ", type=" + type + ", operate=" + operate);
//				Assert.assertEquals(WatcherType.METADATA, type);
//				Assert.assertEquals(OperateType.Add, operate.getType());
//				Assert.assertEquals("key1", name);
//				Assert.assertTrue(operate.getServiceInstance()== instance);
//				metaInvoked.incrementAndGet();
//			}
//			
//		};
//		GetMetadataResponse getMetadatResponse = new GetMetadataResponse();
//		socket.setResponse(respHeader, getMetadatResponse);
//		ServiceDirectoryFuture future = client.asyncGetMetadata("key1", watcher2);
//		Assert.assertNull(((GetMetadataResponse)future.get()).getMetadata());
		
//		List<WatchedMetadataKey> wKeys = new ArrayList<WatchedMetadataKey>();
//		ModelMetadataKey mKey = new ModelMetadataKey();
//		mKey.setName("key1");
//		WatchedMetadataKey wKey = new WatchedMetadataKey(mKey);
//		wKey.getServiceInstanceEvents().add(new ServiceInstanceEvent("service1", "127.2.3.1-8901", OperateType.Add));
//		wKeys.add(wKey);
//		wInstances = new ArrayList<ModelServiceInstance>();
//		wInstances.add(instance);
//		watcherEvent = new WatcherEvent(null, wKeys, wInstances);
//		socket.sendResponse(watcherHeader, watcherEvent);
//		socket.sendResponse(watcherHeader, watcherEvent);
//		socket.sendResponse(watcherHeader, watcherEvent);
//		
//		Thread.sleep(1000);
//		Assert.assertEquals(3, metaInvoked.get());
		
//		Assert.assertTrue(client.validateMetadataKeyWatcher("key1", watcher2));
//		Assert.assertFalse(client.validateMetadataKeyWatcher("key1", watcher1));
//		Assert.assertFalse(client.validateMetadataKeyWatcher("key2", watcher2));
		
//		client.deleteMetadataKeyWatcher("key1", watcher2);
//		Assert.assertFalse(client.validateMetadataKeyWatcher("key1", watcher2));
		client.close();
	}
	
	public static class CustomerDirectorySocket extends DirectorySocket{

		private InetSocketAddress server = null;
		private SocketThread t ;
		private volatile PacketCompare packetCompare;
		private volatile ResponseHeader respHeader = new ResponseHeader(0, 1, ErrorCode.OK);;
		private volatile Response resp = new ConnectResponse(0, 4000, "1", null, 1);
		private int xid = 0;
		@Override
		public boolean isConnected() {
			return true;
		}

		@Override
		public boolean connect(InetSocketAddress address) {
			
			Assert.assertEquals("localhost", address.getHostName());
			Assert.assertEquals(8901, address.getPort()); 
			
			closeSocketThread();
			t = new SocketThread(getDirectoryConnection());
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
			closeSocketThread();
		}

		@Override
		synchronized public void sendPacket(ProtocolHeader header, Protocol p)
				throws IOException {
			LOGGER.info("DirectorySocket send packet type={}, xid={}", header.getType(), header.getXid());
			if(ProtocolType.CreateSession.equals((header.getType()))){
				Assert.assertEquals("admin", ((ConnectProtocol)p).getUserName());
				Assert.assertTrue(((ConnectProtocol)p).isObfuscated());
				ConnectResponse response = new ConnectResponse(0, 4000, "1", null, 1);
				xid = header.getXid();
				t.queueResonse(new ResponseHeader(header.getXid(), 1, ErrorCode.OK), response);
				
			} else if(ProtocolType.Ping.equals(header.getType())){
				t.queueResonse(new ResponseHeader(-2, 2, ErrorCode.OK), new Response());
			} else if(ProtocolType.CloseSession.equals(header.getType())){
				
			} else{
				xid ++;
				Assert.assertEquals(xid, header.getXid());
				if(getPacketCompare() != null){
					Assert.assertTrue(this.getPacketCompare().compare(header, p));
				}
				ResponseHeader h = getResponseHeader();
				t.queueResonse(new ResponseHeader(header.getXid(), h.getDxid(), h.getErr()), getResponse());
			}
		}
		
		synchronized public PacketCompare getPacketCompare(){
			return packetCompare;
		}
		
		synchronized public ResponseHeader getResponseHeader(){
			return respHeader;
		}
		
		synchronized public Response getResponse(){
			return resp;
		}
		
		
		synchronized public void setPacketCompare(PacketCompare packetCompare){
			this.packetCompare = packetCompare;
		}
		
		synchronized public void setResponse(ResponseHeader respHeader, Response resp){
			this.respHeader = respHeader;
			this.resp = resp;
		}
		
		synchronized public void sendResponse(ResponseHeader respHeader, Response resp){
			t.queueResonse(respHeader, resp);
		}
		
	}
	
	public interface PacketCompare{
		public boolean compare(ProtocolHeader header, Protocol protocol);
	}
	
	public static class ProtocolTypePacketCompare implements PacketCompare{
		private final ProtocolType type;
		public ProtocolTypePacketCompare(ProtocolType type){
			this.type = type;
		}
		

		@Override
		public boolean compare(ProtocolHeader header, Protocol protocol) {
			return this.type.equals(header.getType());
		}
		
	}
}
