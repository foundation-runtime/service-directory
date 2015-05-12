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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceDirectory;
import com.cisco.oss.foundation.directory.client.DirectoryServiceRestfulClient;
import com.cisco.oss.foundation.directory.client.DirectoryServiceRestfulClient.DirectoryHttpInvoker;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceDirectoryError;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.utils.HttpResponse;
import com.cisco.oss.foundation.directory.utils.HttpUtils;
/**
 * Test Suite to test the Exception Handling in the Directory API.
 *
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
        final DirectoryServiceRestfulClient client = (DirectoryServiceRestfulClient)ServiceDirectoryImpl.getInstance().getDirectoryServiceClient();
        String serviceName = "mock-test01";
        final ProvidedServiceInstance instance = createInstance(serviceName);

        ServiceDirectoryError sde1 = new ServiceDirectoryError(ErrorCode.SERVICE_INSTANCE_NOT_EXIST);
        final AtomicReference<ServiceDirectoryError> error = new AtomicReference<>();
        error.set(sde1);

        final DirectoryHttpInvoker mockInvoker = new DirectoryHttpInvoker() {
            @Override
            public HttpResponse invoke(String uri, String payload, HttpUtils.HttpMethod method, Map<String, String>headers) {

                Assert.assertEquals("http://vcsdirsvc:2013/service/mock-test01/" + instance.getAddress(), directoryAddresses+uri);
                throw new ServiceException(error.get().getExceptionCode(),error.get().getErrorMessage());
            }
        };
        client.setInvoker(mockInvoker);


        RegistrationManager registration = ServiceDirectory.getRegistrationManager();

        LOGGER.info("Do the negative test.....");

        try {
            registration.registerService(instance);
        } catch (ServiceException e) {
            Assert.assertEquals(
                    e.getServiceDirectoryError().getExceptionCode(),
                    ErrorCode.SERVICE_INSTANCE_NOT_EXIST);
        }

        ServiceDirectoryError sde2 = new ServiceDirectoryError(ErrorCode.SERVICE_INSTANCE_NOT_EXIST);
        error.set(sde2);
        try {
            registration.updateService(instance);
        } catch (ServiceException e) {
            Assert.assertEquals(
                    e.getServiceDirectoryError().getExceptionCode(),
                    ErrorCode.ILLEGAL_SERVICE_INSTANCE_OWNER_ERROR);
        }


        ServiceDirectoryError sde3 = new ServiceDirectoryError(ErrorCode.SERVICE_INSTANCE_ALREADY_EXIST);
        error.set(sde3);
        try {
            registration.registerService(instance);
            registration.registerService(instance);
        } catch (ServiceException e) {
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
        RegistrationManager registration = ServiceDirectory.getRegistrationManager();

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
                address);
        si.setUri("http://www.sina.com.cn");
        Map<String, String> pair = new HashMap<>();
        pair.put("meta1", "value1");
        pair.put("meta2", "value2");
        si.setMetadata(pair);
        si.setStatus(OperationalStatus.UP);
        return si;
    }
}
