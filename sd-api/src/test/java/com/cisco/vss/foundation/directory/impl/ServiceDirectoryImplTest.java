package com.cisco.vss.foundation.directory.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.cisco.vss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.vss.foundation.directory.LookupManager;
import com.cisco.vss.foundation.directory.RegistrationManager;
import com.cisco.vss.foundation.directory.ServiceDirectory;
import com.cisco.vss.foundation.directory.ServiceDirectoryManagerFactory;
import com.cisco.vss.foundation.directory.ServiceInstanceHealth;
import com.cisco.vss.foundation.directory.config.ServiceDirectoryConfig;
import com.cisco.vss.foundation.directory.entity.OperationalStatus;
import com.cisco.vss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.vss.foundation.directory.entity.ServiceInstance;
import com.cisco.vss.foundation.directory.exception.ServiceException;
import com.cisco.vss.foundation.directory.query.ServiceInstanceQuery;

public class ServiceDirectoryImplTest implements ServiceDirectoryManagerFactory {

	@Test
	public void testSetFactory() {
		ServiceDirectory.getServiceDirectoryConfig().setProperty(
				ServiceDirectoryImpl.SD_API_SERVICE_DIRECTORY_MANAGER_FACTORY_PROVIDER_PROPERTY,
				"com.cisco.vss.foundation.directory.impl.ServiceDirectoryImplTest");
		try {
			ServiceDirectory.reinitServiceDirectoryManagerFactory(new ServiceDirectoryImplTest());
		} catch (ServiceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			Assert.assertNull(ServiceDirectory.getLookupManager());
			Assert.assertNull(ServiceDirectory.getRegistrationManager());
		} catch (ServiceException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
		
		final AtomicInteger initializeInvoked = new AtomicInteger(0);
		final AtomicInteger setInvoked = new AtomicInteger(0);
		
		final RegistrationManager registration = new MockRegistration();
		
		final LookupManager lookup = new MockLookup();
		
		ServiceDirectoryManagerFactory factory = new ServiceDirectoryManagerFactory(){

			@Override
			public RegistrationManager getRegistrationManager()
					throws ServiceException {
				return registration;
			}

			@Override
			public LookupManager getLookupManager() throws ServiceException {
				return lookup;
			}

			@Override
			public void initialize(DirectoryServiceClientManager manager) {
				initializeInvoked.incrementAndGet();
			}

			@Override
			public void setServiceDirectoryConfig(ServiceDirectoryConfig config) {
				setInvoked.incrementAndGet();
			}};
		try {
			ServiceDirectory.reinitServiceDirectoryManagerFactory(factory);
			Assert.assertEquals(setInvoked.get(), 0);
			Assert.assertEquals(initializeInvoked.get(), 1);
			
			Assert.assertTrue(ServiceDirectory.getLookupManager() == lookup);
			Assert.assertTrue(ServiceDirectory.getRegistrationManager() == registration);
		} catch (ServiceException e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}
		
		
	}

	@Override
	public RegistrationManager getRegistrationManager() throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LookupManager getLookupManager() throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialize(DirectoryServiceClientManager manager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setServiceDirectoryConfig(ServiceDirectoryConfig config) {
		// TODO Auto-generated method stub

	}
	
	class MockRegistration implements RegistrationManager{

		@Override
		public void registerService(ProvidedServiceInstance serviceInstance)
				throws ServiceException {
		}

		@Override
		public void registerService(
				ProvidedServiceInstance serviceInstance,
				OperationalStatus status) throws ServiceException {
		}

		@Override
		public void registerService(
				ProvidedServiceInstance serviceInstance,
				OperationalStatus status,
				ServiceInstanceHealth registryHealth)
				throws ServiceException {
		}

		@Override
		public void updateServiceOperationalStatus(String serviceName,
				String providerId, OperationalStatus status)
				throws ServiceException {
		}

		@Override
		public void updateServiceUri(String serviceName, String providerId,
				String uri) throws ServiceException {
		}

		@Override
		public void updateService(ProvidedServiceInstance serviceInstance)
				throws ServiceException {
			
		}

		@Override
		public void unregisterService(String serviceName, String providerId)
				throws ServiceException {
			
		}
		
	};
	
	class MockLookup implements LookupManager{

		@Override
		public ServiceInstance lookupInstance(String serviceName)
				throws ServiceException {
			return null;
		}

		@Override
		public List<ServiceInstance> lookupInstances(String serviceName)
				throws ServiceException {
			return null;
		}

		@Override
		public ServiceInstance queryInstanceByName(String serviceName,
				ServiceInstanceQuery query) throws ServiceException {
			return null;
		}

		@Override
		public List<ServiceInstance> queryInstancesByName(
				String serviceName, ServiceInstanceQuery query)
				throws ServiceException {
			return null;
		}

		@Override
		public ServiceInstance queryInstanceByKey(ServiceInstanceQuery query)
				throws ServiceException {
			return null;
		}

		@Override
		public List<ServiceInstance> queryInstancesByKey(
				ServiceInstanceQuery query) throws ServiceException {
			return null;
		}

		@Override
		public ServiceInstance getInstance(String serviceName,
				String instanceId) throws ServiceException {
			return null;
		}

		@Override
		public List<ServiceInstance> getAllInstances(String serviceName)
				throws ServiceException {
			return null;
		}

		@Override
		public List<ServiceInstance> getAllInstances(String serviceName,
				ServiceInstanceQuery query) throws ServiceException {
			return null;
		}

		@Override
		public List<ServiceInstance> getAllInstancesByKey(
				ServiceInstanceQuery query) throws ServiceException {
			return null;
		}
		
	};

}
