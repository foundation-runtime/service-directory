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
package com.cisco.oss.foundation.directory.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.oss.foundation.directory.ServiceDirectory;
import com.cisco.oss.foundation.directory.ServiceInstanceHealth;
import com.cisco.oss.foundation.directory.connect.TestDirectoryConnection.CustomerDirectorySocket;
import com.cisco.oss.foundation.directory.entity.ACL;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.Permission;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.Role;
import com.cisco.oss.foundation.directory.entity.User;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceException;

public class RegistrationManagerImplTest {
	private final static Logger LOGGER = LoggerFactory.getLogger(RegistrationManagerImplTest.class);

	@Test
	public void testRegisterService() {

		ServiceDirectory.getServiceDirectoryConfig().setProperty(
				"heartbeat.interval", 1);
		ServiceDirectory.getServiceDirectoryConfig().setProperty(
				"registry.health.check.interval", 1);

		final ProvidedServiceInstance instance = new ProvidedServiceInstance(
				"odrm", "192.168.7.4", 8901);
		instance.setMonitorEnabled(true);
		instance.setStatus(OperationalStatus.UP);
		instance.setUri("http://cisco.com/vbo/odrm/setupsession");
		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put("datacenter", "dc01");
		metadata.put("solution", "core");
		instance.setMetadata(metadata);

		final ProvidedServiceInstance instance2 = new ProvidedServiceInstance(
				"odrm", "192.168.7.4", 8902);
		instance2.setMonitorEnabled(false);
		instance2.setStatus(OperationalStatus.UP);
		instance2.setUri("http://cisco.com/vbo/odrm/setupsession");
		instance2.setMetadata(metadata);

		final AtomicInteger registerInvoked = new AtomicInteger(0);
		final AtomicInteger hbInvoked = new AtomicInteger(0);
		final AtomicInteger healthInvoked = new AtomicInteger(0);
		final AtomicInteger statusInvoked = new AtomicInteger(0);
		final AtomicInteger unregisterInvoked = new AtomicInteger(0);

		RegistrationManagerImpl impl = new RegistrationManagerImpl(
				new DirectoryServiceClientManager() {
					DirectoryServiceClient client = null;

					@Override
					public DirectoryServiceClient getDirectoryServiceClient() {
						if (client == null) {
							List<String> servers = new ArrayList<String>();
							servers.add("localhost:8901");
							client = new CustomerDirectoryServiceClient(servers) {

								@Override
								public void registerServiceInstance(
										ProvidedServiceInstance inst) {
									Assert.assertTrue(instance == inst
											|| instance2 == inst);
									registerInvoked.incrementAndGet();
								}

								@Override
								public void updateServiceInstanceInternalStatus(String serviceName, String instanceId, OperationalStatus status){ 
									Assert.assertEquals("odrm", serviceName);
									Assert.assertEquals(
											"192.168.7.4-8901", instanceId);
									hbInvoked.incrementAndGet();
								}

								@Override
								public void updateServiceInstanceStatus(
										String serviceName, String instanceId,
										OperationalStatus status) {
									statusInvoked.incrementAndGet();
									Assert.assertEquals(serviceName, "odrm");
									Assert.assertEquals(instanceId,
											"192.168.7.4-8901");
									Assert.assertEquals(OperationalStatus.DOWN,
											status);
								}

								@Override
								public void unregisterServiceInstance(
										String serviceName, String instanceId) {
									unregisterInvoked.incrementAndGet();
									Assert.assertEquals(serviceName, "odrm");
									Assert.assertEquals(instanceId,
											"192.168.7.4-8901");
								}
							};
						}
						return client;
					}
				});
		impl.start();

		final AtomicBoolean ret = new AtomicBoolean(false);
		ServiceInstanceHealth health = new ServiceInstanceHealth() {
			@Override
			public boolean isHealthy() {
				healthInvoked.incrementAndGet();
				return ret.get();
			}
		};

		try {
			impl.registerService(instance);
			impl.registerService(instance2);
			instance.setStatus(OperationalStatus.UP);
			impl.registerService(instance);
			// impl.registerService(instance, OperationalStatus.UP, health);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LOGGER.info("Start to validate.....");
		// Check the RegistrationManager send heartbeat and do the register.
		Assert.assertTrue(registerInvoked.get() == 3);
		Assert.assertTrue(hbInvoked.get() == 0);

		try {
			impl.registerService(instance, health);
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Check the ServiceInstanceHealth invoked.
		Assert.assertTrue(healthInvoked.get() > 0);
		Assert.assertEquals(statusInvoked.get(), 0);

		// Since the ServiceInstanceHealth return false, the heartbeat should
		// stop fine.
		hbInvoked.set(0);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertEquals(hbInvoked.get(), 0);

		// set the ServiceInstanceHealth return true, check the heartbeat should
		// send again.
		ret.set(true);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertTrue(hbInvoked.get() > 0);

		try {
			impl.unregisterService(instance.getServiceName(),
					instance.getProviderId());
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		hbInvoked.set(0);

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertTrue(hbInvoked.get() == 0);
		Assert.assertTrue(unregisterInvoked.get() == 1);
		
		impl.stop();

	}

	@Test
	public void testUpdateService() {
		ServiceDirectory.getServiceDirectoryConfig().setProperty(
				"heartbeat.interval", 1);
		ServiceDirectory.getServiceDirectoryConfig().setProperty(
				"registry.health.check.interval", 1);

		final ProvidedServiceInstance instance = new ProvidedServiceInstance(
				"odrm", "192.168.7.4", 8901);
		instance.setMonitorEnabled(true);
		instance.setStatus(OperationalStatus.UP);
		instance.setUri("http://cisco.com/vbo/odrm/setupsession");
		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put("datacenter", "dc01");
		metadata.put("solution", "core");
		instance.setMetadata(metadata);

		final AtomicInteger updateInvoked = new AtomicInteger(0);
		final AtomicInteger uriInvoked = new AtomicInteger(0);
		final AtomicInteger statusInvoked = new AtomicInteger(0);
		final AtomicInteger hbInvoked = new AtomicInteger(0);

		RegistrationManagerImpl impl = new RegistrationManagerImpl(
				new DirectoryServiceClientManager() {
					DirectoryServiceClient client = null;

					@Override
					public DirectoryServiceClient getDirectoryServiceClient() {
						if (client == null) {
							List<String> servers = new ArrayList<String>();
							servers.add("localhost:8901");
							client = new CustomerDirectoryServiceClient(servers) {

								@Override
								public void registerServiceInstance(
										ProvidedServiceInstance inst) {
								}

								@Override
								public void updateServiceInstanceInternalStatus(String serviceName, String instanceId, OperationalStatus status){ 
									Assert.assertEquals("odrm", serviceName);
									Assert.assertEquals(
											"192.168.7.4-8901", instanceId);
									hbInvoked.incrementAndGet();
								}

								@Override
								public void updateServiceInstance(
										ProvidedServiceInstance inst) {
									updateInvoked.incrementAndGet();
									Assert.assertTrue(inst == instance);
								}

								@Override
								public void updateServiceInstanceStatus(
										String serviceName, String instanceId,
										OperationalStatus status) {
									statusInvoked.incrementAndGet();
									Assert.assertEquals(serviceName, "odrm");
									Assert.assertEquals(instanceId,
											"192.168.7.4-8901");
									Assert.assertEquals(OperationalStatus.DOWN,
											status);
								}

								@Override
								public void updateServiceInstanceUri(
										String serviceName, String instanceId,
										String uri) {
									uriInvoked.incrementAndGet();
									Assert.assertEquals(serviceName, "odrm");
									Assert.assertEquals(instanceId,
											"192.168.7.4-8901");
									Assert.assertEquals("new", uri);
								}

								@Override
								public void unregisterServiceInstance(
										String serviceName, String instanceId) {
									Assert.assertEquals(serviceName, "odrm");
									Assert.assertEquals(instanceId,
											"192.168.7.4-8901");
								}

							};
						}
						return client;
					}

				});
		impl.start();

		try {
			impl.registerService(instance);
			impl.updateService(instance);
			impl.updateServiceOperationalStatus("odrm", "192.168.7.4-8901",
					OperationalStatus.DOWN);
			impl.updateServiceUri("odrm", "192.168.7.4-8901", "new");
		} catch (ServiceException e) {
			e.printStackTrace();
		}

		Assert.assertTrue(updateInvoked.get() > 0);
		Assert.assertTrue(statusInvoked.get() > 0);
		Assert.assertTrue(uriInvoked.get() > 0);

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		hbInvoked.set(0);

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Updated the ServiceInstance to DOWN, it should have no heartbeat.
		Assert.assertTrue(hbInvoked.get() == 0);

		try {
			impl.unregisterService(instance.getServiceName(),
					instance.getProviderId());
		} catch (ServiceException e) {
			e.printStackTrace();
			Assert.assertTrue("unregisterService failed.", false);
		}

		try {
			impl.updateService(instance);
		} catch (ServiceException e) {
			e.printStackTrace();
			Assert.assertEquals(ErrorCode.ILLEGAL_SERVICE_INSTANCE_OWNER_ERROR,
					e.getServiceDirectoryError().getExceptionCode());
		}
		impl.stop();
	}
	
	@Test
	public void testUserMgmt() throws ServiceException{
		final User user1 = new User("User1", Role.CREATE.getPermission());
		
		RegistrationManagerImpl impl = new RegistrationManagerImpl(
				new DirectoryServiceClientManager() {
					DirectoryServiceClient client = null;

					@Override
					public DirectoryServiceClient getDirectoryServiceClient() {
						if (client == null) {
							List<String> servers = new ArrayList<String>();
							servers.add("localhost:8901");
							client = new CustomerDirectoryServiceClient(servers) {
								@Override
								public void createUser(User user, String password){
									Assert.assertTrue(user1 == user);
									Assert.assertEquals("password", password);
								}
								
								@Override
								public User getUser(String name) {
									Assert.assertEquals("User1", name);
									return user1;
								}
								
								@Override
								public void deleteUser(String name) {
									Assert.assertEquals("User1", name);
								}
								
								@Override
								public List<User> getAllUser(){
									List<User> list = new ArrayList<User>();
									list.add(user1);
									return list;
								}
								
								@Override
								public void setUserPassword(String userName, String password) {
									Assert.assertEquals("User1", userName);
									Assert.assertEquals("newpass", password);
								}
								
								@Override
								public void setACL(ACL acl){
									Assert.assertEquals("User1", acl.getId());
									Assert.assertEquals(1, acl.getPermission());
								}
								
								@Override
								public void updateUser(User user) {
									Assert.assertTrue(user1 == user);
								}
							};
						}
						return client;
					}

				});
		impl.start();
		
		impl.createUser(user1, "password");
		impl.deleteUser("User1");
		Assert.assertTrue(user1 == impl.getAllUsers().get(0));
		Assert.assertTrue(user1 == impl.getUser("User1"));
		impl.setUserPassword("User1", "newpass");
		List<Permission> permissions = new ArrayList<Permission>();
		permissions.add(Permission.READ);
		impl.setUserPermission("User1", permissions);
		impl.updateUser(user1);
		impl.stop();
	}

	static class CustomerDirectoryServiceClient extends DirectoryServiceClient {

		public CustomerDirectoryServiceClient(List<String> servers) {

			super(servers, "admin", "admin", new CustomerDirectorySocket());
		}

	}
}
