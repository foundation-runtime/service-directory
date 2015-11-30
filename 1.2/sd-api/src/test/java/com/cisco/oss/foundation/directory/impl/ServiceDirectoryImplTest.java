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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.cisco.oss.foundation.directory.client.DirectoryServiceDummyClient;
import com.cisco.oss.foundation.directory.entity.*;
import org.junit.Assert;
import org.junit.Test;

import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.NotificationHandler;
import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceDirectory;
import com.cisco.oss.foundation.directory.ServiceDirectoryManagerFactory;
import com.cisco.oss.foundation.directory.ServiceInstanceChangeListener;
import com.cisco.oss.foundation.directory.ServiceInstanceHealth;
import com.cisco.oss.foundation.directory.client.DirectoryServiceClient;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;

import javax.ws.rs.NotSupportedException;

import static org.junit.Assert.fail;

public class ServiceDirectoryImplTest implements ServiceDirectoryManagerFactory {

    private final static DirectoryServiceClient dummyClient = new DirectoryServiceDummyClient();
    private final static RegistrationManager mockReg = new MockRegistration();
    private final static LookupManager mockLookup = new MockLookup();

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
            Assert.assertEquals(mockLookup, ServiceDirectory.getLookupManager());
            Assert.assertEquals(mockReg, ServiceDirectory.getRegistrationManager());
        } catch (ServiceException e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }

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
            public DirectoryServiceClient getDirectoryServiceClient() {
                return dummyClient;
            }

            @Override
            public void stop() {
                //to nothing
            }

            @Override
            public void start() {
                //to nothing
            }
        };

        try {
            ServiceDirectory.reinitServiceDirectoryManagerFactory(factory);

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

        // need to call the method to set the isStarted to true
        // otherwise other unit-test will be failed
        // TODO: re-implement the shutdown()/start() method properly so that unit-test not depends on each other.
        ServiceDirectoryImpl.getInstance().restart();
    }

    @Override
    public RegistrationManager getRegistrationManager() throws ServiceException {
        // TODO Auto-generated method stub
        return mockReg;
    }

    @Override
    public LookupManager getLookupManager() throws ServiceException {
        // TODO Auto-generated method stub
        return mockLookup;
    }


    @Override
    public DirectoryServiceClient getDirectoryServiceClient() {
        return dummyClient;
    }

    @Override
    public void start() {
        System.out.println("ServiceDirectoryManagerFactory start.");
    }

    @Override
    public void stop() {
        System.out.println("ServiceDirectoryManagerFactory stop.");
    }

    private static class MockRegistration implements RegistrationManager{

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
                String providerAddress, OperationalStatus status)
                throws ServiceException {
        }

        @Override
        public void updateServiceUri(String serviceName, String providerAddress,
                String uri) throws ServiceException {
        }
        
        @Override
        public void updateServiceMetadata(String serviceName,
                String providerAddress, Map<String, String> metadata)
                throws ServiceException {
        }

        @Override
        public void updateService(ProvidedServiceInstance serviceInstance)
                throws ServiceException {

        }

        @Override
        public void unregisterService(String serviceName, String providerAddress)
                throws ServiceException {

        }

        @Override
        public void close() throws ServiceException {

        }

    }

    private static class MockLookup implements LookupManager{

        @Override
        public ServiceInstance lookupInstance(String serviceName)
                throws ServiceException {
            throw new UnsupportedOperationException() ; // should not be called
        }

        @Override
        public List<ServiceInstance> lookupInstances(String serviceName)
                throws ServiceException {
            return Collections.emptyList();
        }

        @Override
        public ServiceInstance queryInstanceByName(String serviceName,
                ServiceInstanceQuery query) throws ServiceException {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<ServiceInstance> queryInstancesByName(
                String serviceName, ServiceInstanceQuery query)
                throws ServiceException {
            return Collections.emptyList();
        }

        @Override
        public ServiceInstance queryInstanceByMetadataKey(ServiceInstanceQuery query)
                throws ServiceException {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<ServiceInstance> queryInstancesByMetadataKey(
                ServiceInstanceQuery query) throws ServiceException {
            return Collections.emptyList();
        }

        @Override
        public ServiceInstance getInstance(String serviceName,
                String instanceId) throws ServiceException {
            throw new UnsupportedOperationException();
        }

        @Override
        public ServiceInstance getInstanceByAddress(String serviceName, String instanceAddress) throws ServiceException {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<ServiceInstance> getAllInstances(String serviceName)
                throws ServiceException {
            return Collections.emptyList();
        }

        @Override
        public List<ServiceInstance> getAllInstances(String serviceName,
                ServiceInstanceQuery query) throws ServiceException {
            return Collections.emptyList();
        }

        @Override
        public List<ServiceInstance> getAllInstances() throws ServiceException {
            // TODO Auto-generated method stub
            return Collections.emptyList();
        }

        @Override
        public List<ServiceInstance> getAllInstancesByMetadataKey(
                ServiceInstanceQuery query) throws ServiceException {
            return Collections.emptyList();
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

        @Override
        public void addInstanceChangeListener(String serviceName, ServiceInstanceChangeListener listener) throws ServiceException {

        }

        @Override
        public void removeInstanceChangeListener(String serviceName, ServiceInstanceChangeListener listener) throws ServiceException {

        }

        @Override
        public void close() throws ServiceException {
            // do nothing
        }

        @Override
        public boolean isStarted() throws ServiceException {
            return false;
        }
    }



}
