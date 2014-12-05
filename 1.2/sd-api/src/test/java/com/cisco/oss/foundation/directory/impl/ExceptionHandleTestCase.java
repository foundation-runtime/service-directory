/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceDirectory;
import com.cisco.oss.foundation.directory.connect.DirectorySocket;
import com.cisco.oss.foundation.directory.connect.TestDirectoryConnection.SocketThread;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.proto.ConnectProtocol;
import com.cisco.oss.foundation.directory.proto.ConnectResponse;
import com.cisco.oss.foundation.directory.proto.Protocol;
import com.cisco.oss.foundation.directory.proto.ProtocolHeader;
import com.cisco.oss.foundation.directory.proto.ProtocolType;
import com.cisco.oss.foundation.directory.proto.Response;
import com.cisco.oss.foundation.directory.proto.ResponseHeader;

/**
 * Test Suite to test the Exception Handling in the Directory API.
 * 
 * @author zuxiang
 *
 */
public class ExceptionHandleTestCase  {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ExceptionHandleTestCase.class);
	
	@BeforeClass
	public static void setup() throws Exception{
		
		try {
			ServiceDirectory.reinitServiceDirectoryManagerFactory(new DefaultServiceDirectoryManagerFactory());
		} catch (ServiceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	/**
	 * Test the exception handling in register, update, unregister and lookup ServiceInstance.
	 * @throws ServiceException 
	 * 
	 */
	@Test
	public void testRegistrationManager() throws ServiceException {
		ServiceDirectory.getServiceDirectoryConfig().setProperty(DirectoryServiceClient.SD_API_DIRECTORY_SOCKET_PROVIDER_PROPERTY, 
				CustomerDirectorySocket.class.getName());
		((DirectoryServiceClientManager)ServiceDirectoryImpl.getInstance()).getDirectoryServiceClient();
		String serviceName = "mock-test01";
		final ProvidedServiceInstance instance = createInstance(serviceName);
		
		
		
		RegistrationManager registration = null;
		try {
			registration = ServiceDirectory.getRegistrationManager();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LOGGER.info("Do the negative test.....");

		CustomerDirectorySocket.instance.setResponse(new ResponseHeader(1, 1, ErrorCode.SERVICE_INSTANCE_NOT_EXIST), null);

		try {
			registration.registerService(instance);
		} catch (ServiceException e) {
			e.printStackTrace();
			Assert.assertEquals(
					e.getServiceDirectoryError().getExceptionCode(),
					ErrorCode.SERVICE_INSTANCE_NOT_EXIST);
		}

		CustomerDirectorySocket.instance.setResponse(new ResponseHeader(1, 1, ErrorCode.ILLEGAL_SERVICE_INSTANCE_OWNER_ERROR), null);
		try {
			registration.updateService(instance);
		} catch (ServiceException e) {
			e.printStackTrace();
			Assert.assertEquals(
					e.getServiceDirectoryError().getExceptionCode(),
					ErrorCode.ILLEGAL_SERVICE_INSTANCE_OWNER_ERROR);
		}

		CustomerDirectorySocket.instance.setResponse(new ResponseHeader(1, 1, ErrorCode.SERVICE_INSTANCE_ALREADY_EXIST), null);
		try {
			registration.registerService(instance);
			registration.registerService(instance);
		} catch (ServiceException e) {
			e.printStackTrace();
			Assert.assertEquals(
					e.getServiceDirectoryError().getExceptionCode(),
					ErrorCode.SERVICE_INSTANCE_ALREADY_EXIST);
		}
	}

	/**
	 * Test validate ProvidedServiceInstance.
	 */
	@Test
	public void testServiceInstanceValidate() {
		RegistrationManager registration = null;
		try {
			registration = ServiceDirectory.getRegistrationManager();
		} catch (ServiceException e) {
			e.printStackTrace();
		}

		LOGGER.info("Do the negative test.....");

		String serviceName = "mock-test01";
		
		try {
			ProvidedServiceInstance instance = createInstance("---negative_test");
			registration.registerService(instance);
		} catch (ServiceException e) {
			Assert.assertEquals(
					e.getServiceDirectoryError().getExceptionCode(),
					ErrorCode.SERVICE_INSTANCE_NAME_FORMAT_ERROR);
		}
		
		try {
			ProvidedServiceInstance instance = createInstance(serviceName);
			instance.getMetadata().put("--fortest", "vvvv");
			registration.registerService(instance);
		} catch (ServiceException e) {
			Assert.assertEquals(
					e.getServiceDirectoryError().getExceptionCode(),
					ErrorCode.SERVICE_INSTANCE_METAKEY_FORMAT_ERROR);
		}
	}

	private ProvidedServiceInstance createInstance(String serviceName) {

		String address = "127.0.0.1";
		int port = 8990;
		ProvidedServiceInstance si = new ProvidedServiceInstance(serviceName,
				address, port);
		si.setUri("http://www.sina.com.cn");
		Map<String, String> pair = new HashMap<String, String>();
		pair.put("meta1", "value1");
		pair.put("meta2", "value2");
		si.setMetadata(pair);
		si.setStatus(OperationalStatus.UP);
		return si;
	}
	
	public static class CustomerDirectorySocket extends DirectorySocket{

		private InetSocketAddress server = null;
		private SocketThread t ;
		private ResponseHeader respHeader = new ResponseHeader(0, 1, ErrorCode.OK);;
		private Response resp = new ConnectResponse(0, 4000, "1", null, 1);
		private int xid = 0;
		
		private static CustomerDirectorySocket instance;
		
		public CustomerDirectorySocket(){
			instance = this;
		}
		@Override
		public boolean isConnected() {
			return true;
		}

		@Override
		public boolean connect(InetSocketAddress address) {
			
			Assert.assertEquals("vcsdirsvc", address.getHostName());
			Assert.assertEquals(2013, address.getPort()); 
			
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
		public void sendPacket(ProtocolHeader header, Protocol p)
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
				respHeader.setXid(header.getXid());
				t.queueResonse(respHeader, resp);
			}
		}
		
		public void setResponse(ResponseHeader respHeader, Response resp){
			this.respHeader = respHeader;
			this.resp = resp;
		}
		
		public void sendResponse(ResponseHeader respHeader, Response resp){
			t.queueResonse(respHeader, resp);
		}
		
	}
}
