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




package com.cisco.oss.foundation.directory.test;

import com.cisco.oss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceDirectoryManagerFactory;
import com.cisco.oss.foundation.directory.config.ServiceDirectoryConfig;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.lifecycle.Closable;

/**
 * The default ServiceDirectoryManagerFactory for integration test on top of SD API.
 *
 * It is an in-memory service directory provider which stores and looks up services in local.
 * Set configuration "service.directory.manager.factory.provider" to
 * "test.TestServiceDirectoryManagerFactory" before invoking the ServiceDirectory.
 *
 *
 */
public class TestServiceDirectoryManagerFactory implements
        ServiceDirectoryManagerFactory, Closable {

    /**
     * The DefaultTestServiceDirectoryManager.
     */
    private volatile DefaultTestServiceDirectoryManager testManager;

    /**
     * Constructor.
     */
    public TestServiceDirectoryManagerFactory(){
    }

    public TestServiceDirectoryManagerFactory(DefaultTestServiceDirectoryManager testManager){
        this.testManager = testManager;
        testManager.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegistrationManager getRegistrationManager() throws ServiceException {
        return getDefaultTestServiceDirectoryManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LookupManager getLookupManager() throws ServiceException {
        return getDefaultTestServiceDirectoryManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(DirectoryServiceClientManager manager) {
        // do nothing.

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServiceDirectoryConfig(ServiceDirectoryConfig config) {
        // Do nothing.
    }

    /**
     * Get the DefaultTestServiceDirectoryManager, it is lazy initialized.
     *
     * @return
     *         the DefaultTestServiceDirectoryManager.
     */
    public DefaultTestServiceDirectoryManager getDefaultTestServiceDirectoryManager(){
        if(testManager == null){
            synchronized(this){
                if(testManager == null){
                    testManager = new DefaultTestServiceDirectoryManager();
                    testManager.start();
                }
            }
        }
        return testManager;
    }

    @Override
    public void start() {
        // Do nothing here.
    }

    @Override
    public void stop() {
        if(testManager != null){
            if(testManager instanceof Closable){
                ((Closable) testManager).stop();
            }
        }
    }




}
