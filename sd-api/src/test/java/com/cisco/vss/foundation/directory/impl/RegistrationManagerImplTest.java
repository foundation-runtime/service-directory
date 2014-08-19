package com.cisco.vss.foundation.directory.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.cisco.vss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.vss.foundation.directory.ServiceDirectory;
import com.cisco.vss.foundation.directory.ServiceInstanceHealth;
import com.cisco.vss.foundation.directory.entity.OperationResult;
import com.cisco.vss.foundation.directory.entity.OperationalStatus;
import com.cisco.vss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.vss.foundation.directory.entity.ServiceInstanceHeartbeat;
import com.cisco.vss.foundation.directory.exception.ServiceException;

public class RegistrationManagerImplTest {
	
	@Test
	public void testRegisterService(){
		
		ServiceDirectory.getServiceDirectoryConfig().setProperty("heartbeat.interval", 1);
		ServiceDirectory.getServiceDirectoryConfig().setProperty("registry.health.check.interval", 1);
		
		final ProvidedServiceInstance instance = new ProvidedServiceInstance("odrm", "192.168.7.4", 8901);
		instance.setMonitorEnabled(true);
		instance.setStatus(OperationalStatus.UP);
		instance.setUri("http://cisco.com/vbo/odrm/setupsession");
		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put("datacenter", "dc01");
		metadata.put("solution", "core");
		instance.setMetadata(metadata);
		
		final ProvidedServiceInstance instance2 = new ProvidedServiceInstance("odrm", "192.168.7.4", 8902);
		instance2.setMonitorEnabled(false);
		instance2.setStatus(OperationalStatus.UP);
		instance2.setUri("http://cisco.com/vbo/odrm/setupsession");
		instance2.setMetadata(metadata);
		
		final AtomicInteger registerInvoked = new AtomicInteger(0);
		final AtomicInteger hbInvoked = new AtomicInteger(0);
		final AtomicInteger healthInvoked = new AtomicInteger(0);
		final AtomicInteger statusInvoked = new AtomicInteger(0);
		final AtomicInteger unregisterInvoked = new AtomicInteger(0);
		
		RegistrationManagerImpl impl = new RegistrationManagerImpl(new DirectoryServiceClientManager(){
			@Override
			public DirectoryServiceClient getDirectoryServiceClient() {
				return new DirectoryServiceClient(){
					@Override
					public void registerInstance(ProvidedServiceInstance inst){
						Assert.assertTrue(instance == inst || instance2 == inst);
						registerInvoked.incrementAndGet();
					}
					
					@Override
					public Map<String, OperationResult<String>> sendHeartBeat(Map<String, ServiceInstanceHeartbeat> heartbeatMap){
						
						Assert.assertEquals(heartbeatMap.size(), 1);
						Assert.assertTrue(heartbeatMap.containsKey("odrm-192.168.7.4-8901"));
						Map<String, OperationResult<String>> result = new HashMap<String, OperationResult<String>>();
						result.put("odrm-192.168.7.4-8901", new OperationResult<String>(true, "it is OK", null));
						hbInvoked.incrementAndGet();
						return result;
					}
					
					@Override
					public void updateInstanceStatus(String serviceName, String instanceId, OperationalStatus status){
						statusInvoked.incrementAndGet();
						Assert.assertEquals(serviceName, "odrm");
						Assert.assertEquals(instanceId, "192.168.7.4-8901");
						Assert.assertEquals(OperationalStatus.DOWN, status);
					}
					
					@Override
					public void unregisterInstance(String serviceName, String instanceId){
						unregisterInvoked.incrementAndGet();
						Assert.assertEquals(serviceName, "odrm");
						Assert.assertEquals(instanceId, "192.168.7.4-8901");
					}
				};
			}
		});
		
		final AtomicBoolean ret = new AtomicBoolean(false);
		ServiceInstanceHealth health = new ServiceInstanceHealth(){
			@Override
			public boolean isHealthy() {
				healthInvoked.incrementAndGet();
				return ret.get();
			}
		};
		
		try {
			impl.registerService(instance);
			impl.registerService(instance2);
			impl.registerService(instance, OperationalStatus.UP);
//			impl.registerService(instance, OperationalStatus.UP, health);
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
		
		// Check the RegistrationManager send heartbeat and do the register.
		Assert.assertTrue(registerInvoked.get()==3);
		Assert.assertTrue(hbInvoked.get() > 0);
		
		
		try {
			impl.registerService(instance, OperationalStatus.UP, health);
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
		
		// Since the ServiceInstanceHealth return false, the heartbeat should stop fine.
		hbInvoked.set(0);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertEquals(hbInvoked.get(), 0);
		
		//set the ServiceInstanceHealth return true, check the heartbeat should send again.
		ret.set(true);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Assert.assertTrue(hbInvoked.get()>0);
		
		try {
			impl.unregisterService(instance.getServiceName(), instance.getProviderId());
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
		Assert.assertTrue(hbInvoked.get()==0);
		Assert.assertTrue(unregisterInvoked.get()==1);
		
	}
	
	@Test
	public void testUpdateService(){
		ServiceDirectory.getServiceDirectoryConfig().setProperty("heartbeat.interval", 1);
		ServiceDirectory.getServiceDirectoryConfig().setProperty("registry.health.check.interval", 1);
		
		final ProvidedServiceInstance instance = new ProvidedServiceInstance("odrm", "192.168.7.4", 8901);
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
		
		RegistrationManagerImpl impl = new RegistrationManagerImpl(new DirectoryServiceClientManager(){
			@Override
			public DirectoryServiceClient getDirectoryServiceClient() {
				return new DirectoryServiceClient(){
					
					@Override
					public void registerInstance(ProvidedServiceInstance inst){
					}
					
					@Override
					public Map<String, OperationResult<String>> sendHeartBeat(Map<String, ServiceInstanceHeartbeat> heartbeatMap){
						
						Assert.assertEquals(heartbeatMap.size(), 1);
						Assert.assertTrue(heartbeatMap.containsKey("odrm-192.168.7.4-8901"));
						Map<String, OperationResult<String>> result = new HashMap<String, OperationResult<String>>();
						result.put("odrm-192.168.7.4-8901", new OperationResult<String>(true, "it is OK", null));
						hbInvoked.incrementAndGet();
						return result;
					}
					
					@Override
					public void updateInstance(ProvidedServiceInstance inst){
						updateInvoked.incrementAndGet();
						Assert.assertTrue(inst== instance);
					}
					
					@Override
					public void updateInstanceStatus(String serviceName, String instanceId, OperationalStatus status){
						statusInvoked.incrementAndGet();
						Assert.assertEquals(serviceName, "odrm");
						Assert.assertEquals(instanceId, "192.168.7.4-8901");
						Assert.assertEquals(OperationalStatus.DOWN, status);
					}
					
					@Override
					public void updateInstanceUri(String serviceName, String instanceId, String uri){
						uriInvoked.incrementAndGet();
						Assert.assertEquals(serviceName, "odrm");
						Assert.assertEquals(instanceId, "192.168.7.4-8901");
						Assert.assertEquals("new", uri);
					}
				};
			}
			
			
			
			
		});
		
		try {
			impl.registerService(instance);
			impl.updateService(instance);
			impl.updateServiceOperationalStatus("odrm", "192.168.7.4-8901", OperationalStatus.DOWN);
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
		Assert.assertTrue(hbInvoked.get() ==0);
		
	}

//	private static DirectoryServiceClientManager manager ;
//	public static DirectoryServiceClientManager getDirectoryServiceClientManager(){
//		if(manager == null){
//			manager = new DirectoryServiceClientManager(){
//				@Override
//				public DirectoryServiceClient getDirectoryServiceClient() {
//					// TODO Auto-generated method stub
//					return null;
//				}
//			};
//		}
//		return manager;
//	}
}
