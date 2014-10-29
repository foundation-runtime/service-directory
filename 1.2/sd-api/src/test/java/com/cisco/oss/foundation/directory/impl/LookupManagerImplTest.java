package com.cisco.oss.foundation.directory.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.oss.foundation.directory.ServiceDirectory;
import com.cisco.oss.foundation.directory.connect.DirectorySocket;
import com.cisco.oss.foundation.directory.connect.TestDirectoryConnection.SocketThread;
import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ServiceInstanceEvent;
import com.cisco.oss.foundation.directory.entity.WatchedMetadataKey;
import com.cisco.oss.foundation.directory.entity.WatchedService;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.proto.ConnectProtocol;
import com.cisco.oss.foundation.directory.proto.ConnectResponse;
import com.cisco.oss.foundation.directory.proto.GetMetadataResponse;
import com.cisco.oss.foundation.directory.proto.GetServiceResponse;
import com.cisco.oss.foundation.directory.proto.Protocol;
import com.cisco.oss.foundation.directory.proto.ProtocolHeader;
import com.cisco.oss.foundation.directory.proto.ProtocolType;
import com.cisco.oss.foundation.directory.proto.Response;
import com.cisco.oss.foundation.directory.proto.ResponseHeader;
import com.cisco.oss.foundation.directory.proto.ServiceInstanceOperate.OperateType;
import com.cisco.oss.foundation.directory.proto.WatcherEvent;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;

public class LookupManagerImplTest {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(LookupManagerImplTest.class);

	@Test
	public void test01() {
		ServiceDirectory.getServiceDirectoryConfig().setProperty(
				"heartbeat.interval", 1);
		ServiceDirectory.getServiceDirectoryConfig().setProperty(
				"registry.health.check.interval", 1);

		final String serviceName = "odrm";
		final String instanceId = "192.168.2.3-8901";
		final String keyName = "solution";

		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put("datacenter", "dc01");
		metadata.put("solution", "core");
		List<ModelServiceInstance> instances = new ArrayList<ModelServiceInstance>();
		ModelServiceInstance instance = new ModelServiceInstance("odrm",
				"192.168.2.3-8901", "192.168.2.3-8901",
				"http://cisco.com/vbo/odrm/setupsession", OperationalStatus.UP,metadata);
		instances.add(instance);
		final ModelService result = new ModelService("odrm", "odrm");
		result.setServiceInstances(instances);

		final ModelMetadataKey keyResult = new ModelMetadataKey(keyName,
				keyName);
		keyResult.setServiceInstances(instances);
		
		final CustomerDirectorySocket socket = new CustomerDirectorySocket();

		LookupManagerImpl impl = new LookupManagerImpl(
				new DirectoryServiceClientManager() {
					DirectoryServiceClient client = null;

					@Override
					public DirectoryServiceClient getDirectoryServiceClient() {
						if (client == null) {
							List<String> servers = new ArrayList<String>();
							servers.add("localhost:8901");
							
							
							
							client = new DirectoryServiceClient(servers, "admin", "admin", socket);
						}
						return client;
					}
				});

		impl.start();

		ServiceInstanceQuery query = new ServiceInstanceQuery()
				.getEqualQueryCriterion("solution", "core");
		try {
			Assert.assertEquals(impl.getAllInstances(serviceName).get(0)
					.getInstanceId(), instanceId);
			Assert.assertTrue(impl.getAllInstances(serviceName, query).get(0)
					.getInstanceId().equals(instanceId));
			Assert.assertTrue(impl.getAllInstancesByKey(query).get(0)
					.getInstanceId().equals(instanceId));
			Assert.assertTrue(impl.getInstance(serviceName, instanceId)
					.getInstanceId().equals(instanceId));
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LOGGER.info("--------------->send notification"); 
		// Send watch notification.
		ResponseHeader watcherHeader = new ResponseHeader(-1, 1, ErrorCode.OK);
		
		Map<String, String> changedMetadata = new HashMap<String, String>();
		changedMetadata.put("datacenter", "dc02");
		changedMetadata.put("solution", "core02");
		
		
		ModelServiceInstance newInstance = duplicateInstance(instance);
		ModelService service = new ModelService();
		service.setName(serviceName);
		
		WatchedService wService = new WatchedService(service);
		wService.getServiceInstanceEvents().add(new ServiceInstanceEvent(serviceName, instanceId, OperateType.Update));
		List<WatchedService> os = new ArrayList<WatchedService>();
		os.add(wService);
		newInstance.setMetadata(changedMetadata); 
		List<ModelServiceInstance> wInstances = new ArrayList<ModelServiceInstance>();
		wInstances.add(newInstance);
		WatcherEvent watcherEvent = new WatcherEvent(os, null, wInstances);
		socket.sendResponse(watcherHeader, watcherEvent);
		
		newInstance = duplicateInstance(newInstance);
		newInstance.setUri("http://cisco.com/vbo/odrm/setupsession/v02"); 
		wService = new WatchedService(service);
		wService.getServiceInstanceEvents().add(new ServiceInstanceEvent(serviceName, instanceId, OperateType.Update));
		os = new ArrayList<WatchedService>();
		os.add(wService);
		wInstances = new ArrayList<ModelServiceInstance>();
		wInstances.add(newInstance);
		watcherEvent = new WatcherEvent(os, null, wInstances);
		socket.sendResponse(watcherHeader, watcherEvent);
		
		ModelServiceInstance changedInstance = new ModelServiceInstance(serviceName, instanceId, "192.168.2.3-8901", "http://cisco.com/vbo/odrm/setupsession/v03", 
				OperationalStatus.UP, changedMetadata);
		
		ModelMetadataKey key = new ModelMetadataKey();
		key.setName(keyName);
		WatchedMetadataKey wKey = new WatchedMetadataKey(key);
		wKey.getServiceInstanceEvents().add(new ServiceInstanceEvent(serviceName, instanceId, OperateType.Add));
		List<WatchedMetadataKey> wKeys = new ArrayList<WatchedMetadataKey>();
		wKeys.add(wKey);
		wInstances = new ArrayList<ModelServiceInstance>();
		wInstances.add(changedInstance);
		watcherEvent = new WatcherEvent(null, wKeys, wInstances);
		
		socket.sendResponse(watcherHeader, watcherEvent);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		List<String> list = new ArrayList<String>();
		list.add("core02");
		list.add("core03");
		query = new ServiceInstanceQuery()
				.getInQueryCriterion("solution", list);
		try {
			Assert.assertEquals(impl.getAllInstances(serviceName).get(0).getInstanceId(), instanceId);
			Assert.assertEquals(impl.getAllInstances(serviceName, query).get(0).getUri(), "http://cisco.com/vbo/odrm/setupsession/v02");
			Assert.assertEquals(impl.getAllInstancesByKey(query).get(0).getInstanceId(),instanceId);
			Assert.assertEquals(impl.getAllInstancesByKey(query).get(0).getUri(), "http://cisco.com/vbo/odrm/setupsession/v03");
			Assert.assertEquals(impl.getInstance(serviceName, instanceId).getInstanceId(), instanceId);
			Assert.assertEquals(impl.getInstance(serviceName, instanceId).getMetadata().get("solution"), "core02");
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		impl.stop();
	}
	
	private ModelServiceInstance duplicateInstance(ModelServiceInstance instance){
		Map<String, String> m = null;
		if(instance.getMetadata() != null){
			m = new HashMap<String, String>();
			for(Entry<String, String> entry : instance.getMetadata().entrySet()){
				m.put(entry.getKey(), entry.getValue());
			}
		}
		ModelServiceInstance newInstance = new ModelServiceInstance(instance.getServiceName(), instance.getInstanceId(),
				instance.getId(), instance.getUri(), instance.getStatus(), m);
		return newInstance;
	}

	public static class CustomerDirectorySocket extends DirectorySocket {

		private InetSocketAddress server = null;
		private SocketThread t;
		private ResponseHeader respHeader = new ResponseHeader(0, 1,
				ErrorCode.OK);;
		private Response resp = new ConnectResponse(0, 4000, "1", null, 1);

		@Override
		public boolean isConnected() {
			return true;
		}

		@Override
		public boolean connect(InetSocketAddress address) throws IOException {

			Assert.assertEquals("localhost", address.getHostName());
			Assert.assertEquals(8901, address.getPort());

			closeSocketThread();
			t = new SocketThread(getDirectoryConnection());
			t.start();
			this.server = address;
			LOGGER.info("DirectorySocket connect to {}", address);
			return true;
		}

		private void closeSocketThread() {
			if (t != null) {
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
		public void sendPacket(ProtocolHeader header, Protocol p)
				throws IOException {
			LOGGER.info("DirectorySocket send packet type={}, xid={}",
					header.getType(), header.getXid());
			if (ProtocolType.CreateSession.equals((header.getType()))) {
				Assert.assertEquals("admin",
						((ConnectProtocol) p).getUserName());
				Assert.assertTrue(((ConnectProtocol) p).isObfuscated());
				ConnectResponse response = new ConnectResponse(0, 4000, "1",
						null, 1);
				t.queueResonse(new ResponseHeader(header.getXid(), 1,
						ErrorCode.OK), response);

			} else if (ProtocolType.Ping.equals(header.getType())) {
				t.queueResonse(new ResponseHeader(-2, 2, ErrorCode.OK),
						new Response());
			} else if (ProtocolType.CloseSession.equals(header.getType())) {

			} else if (ProtocolType.GetService.equals(header.getType())) {

				Map<String, String> metadata = new HashMap<String, String>();
				metadata.put("datacenter", "dc01");
				metadata.put("solution", "core");
				List<ModelServiceInstance> instances = new ArrayList<ModelServiceInstance>();
				ModelServiceInstance instance = new ModelServiceInstance(
						"odrm", "192.168.2.3-8901", "192.168.2.3-8901",
						"http://cisco.com/vbo/odrm/setupsession",
						OperationalStatus.UP, metadata);
//				instance.setHeartbeatTime(date);
				instances.add(instance);
				ModelService result = new ModelService("odrm", "odrm");
				result.setServiceInstances(instances);

				GetServiceResponse response = new GetServiceResponse(result);
				t.queueResonse(new ResponseHeader(header.getXid(), 1,
						ErrorCode.OK), response);
			} else if (ProtocolType.GetMetadata.equals(header.getType())) {

				Map<String, String> metadata = new HashMap<String, String>();
				metadata.put("datacenter", "dc01");
				metadata.put("solution", "core");
				List<ModelServiceInstance> instances = new ArrayList<ModelServiceInstance>();
				ModelServiceInstance instance = new ModelServiceInstance(
						"odrm", "192.168.2.3-8901", "192.168.2.3-8901",
						"http://cisco.com/vbo/odrm/setupsession",
						OperationalStatus.UP, metadata);
//				instance.setHeartbeatTime(date);
				instances.add(instance);

				ModelMetadataKey keyResult = new ModelMetadataKey("solution",
						"solution");
				keyResult.setServiceInstances(instances);

				GetMetadataResponse response = new GetMetadataResponse(
						keyResult);
				t.queueResonse(new ResponseHeader(header.getXid(), 1,
						ErrorCode.OK), response);
			} else {
				respHeader.setXid(header.getXid());
				t.queueResonse(respHeader, resp);
			}
		}

		public void setResponse(ResponseHeader respHeader, Response resp) {
			this.respHeader = respHeader;
			this.resp = resp;
		}

		public void sendResponse(ResponseHeader respHeader, Response resp) {
			t.queueResonse(respHeader, resp);
		}

	}
}
