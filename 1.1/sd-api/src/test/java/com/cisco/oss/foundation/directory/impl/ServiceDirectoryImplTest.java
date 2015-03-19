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

import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.NotificationHandler;
import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceDirectory;
import com.cisco.oss.foundation.directory.ServiceDirectoryManagerFactory;
import com.cisco.oss.foundation.directory.ServiceInstanceHealth;
import com.cisco.oss.foundation.directory.config.ServiceDirectoryConfig;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.lifecycle.Closable;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;

public class ServiceDirectoryImplTest implements ServiceDirectoryManagerFactory, Closable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDirectoryImplTest.class);
    @Test
    public void testSetFactory() {
        ServiceDirectory.getServiceDirectoryConfig().setProperty(
                ServiceDirectoryImpl.SD_API_SERVICE_DIRECTORY_MANAGER_FACTORY_PROVIDER_PROPERTY,
                "com.cisco.oss.foundation.directory.impl.ServiceDirectoryImplTest");
        try {
            ServiceDirectory.reinitServiceDirectoryManagerFactory(new ServiceDirectoryImplTest());
        } catch (ServiceException e1) {
            Assert.assertTrue("reinitServiceDirectoryManagerFactory failed", false);
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

            ServiceDirectory.reinitServiceDirectoryManagerFactory(new DefaultServiceDirectoryManagerFactory());
        } catch (ServiceException e) {
            e.printStackTrace();
            Assert.assertTrue("reinitServiceDirectoryManagerFactory failed", false);
        }

        LookupManager lookupMgr = null;
        try {
            lookupMgr = ServiceDirectory.getLookupManager();
        } catch (ServiceException e2) {
            e2.printStackTrace();
            Assert.assertTrue("getLookupManager failed", false);
        }

        ServiceDirectory.shutdown();
        // When the ServiceDirectory shutdown, getRegistrationManager and getLookupManager should get SERVICE_DIRECTORY_IS_SHUTDOWN error.
        try {
            ServiceDirectory.getRegistrationManager();
            fail();
        } catch (ServiceException e) {
            Assert.assertEquals(ErrorCode.SERVICE_DIRECTORY_IS_SHUTDOWN, e.getServiceDirectoryError().getExceptionCode());
        }

        // When the DefaultServiceDirectoryManagerFactory, close lookupManager and registrationManager should get SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED.
        try {
            lookupMgr.getAllInstances();
            fail();
        } catch (ServiceException e1) {
            Assert.assertEquals(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED, e1.getServiceDirectoryError().getExceptionCode());
        }

        ServiceDirectoryImpl.getInstance().revertForUnitTest();

        try {
            Assert.assertNull(ServiceDirectory.getRegistrationManager());

        } catch (ServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

    @Override
    public void start() {
        System.out.println("ServiceDirectoryManagerFactory start.");
    }

    @Override
    public void stop() {
        System.out.println("ServiceDirectoryManagerFactory stop.");
    };

    class MockRegistration implements RegistrationManager{

        @Override
        public void registerService(ProvidedServiceInstance serviceInstance)
                throws ServiceException {
        }

        @Override
        public void registerService(
                ProvidedServiceInstance serviceInstance,
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
        public List<ServiceInstance> getAllInstances() throws ServiceException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<ServiceInstance> getAllInstancesByKey(
                ServiceInstanceQuery query) throws ServiceException {
            return null;
        }

        @Override
        public void addNotificationHandler(String serviceName,
                NotificationHandler handler) throws ServiceException {
            // TODO Auto-generated method stub

        }

        @Override
        public void removeNotificationHandler(String serviceName,
                NotificationHandler handler) throws ServiceException {
            // TODO Auto-generated method stub

        }

    }



}
