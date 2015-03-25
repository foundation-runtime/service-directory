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

import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceDirectoryManagerFactory;
import com.cisco.oss.foundation.directory.config.ServiceDirectoryConfig;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.lifecycle.Stoppable;

/**
 * It is the default ServiceDirectoryManagerFactory to access remote ServiceDirectory server node.
 *
 * When there is no other ServiceDirectoryManagerFactory provider assigned, Service Directory API will 
 * initialize this class to provide ServiceDirectory services.
 *
 *
 */
public class DefaultServiceDirectoryManagerFactory implements
        ServiceDirectoryManagerFactory {

    /**
     * The RegistrationManager.
     */
    private final RegistrationManagerImpl registrationManager;

    /**
     * The LookupManager.
     */
    private final LookupManagerImpl lookupManager;


    /**
     * The DirectoryServiceClient.
     * TODO: should be final
     * the field is not declared final because the reinit() method try to change the reference.
     */
    private DirectoryServiceClient dirSvcClient;

    /**
     * Default constructor.
     */
    public DefaultServiceDirectoryManagerFactory(){
        dirSvcClient = new DirectoryServiceClient();
        registrationManager = new RegistrationManagerImpl(dirSvcClient);
        lookupManager = new LookupManagerImpl(dirSvcClient);
    }

    /**
     * Get RegistrationManager.
     *
     * It is thread safe.
     *
     * @return
     *         the RegistrationManager implementation instance.
     */
    @Override
    public RegistrationManager getRegistrationManager(){
       return registrationManager;
    }

    /**
     * Get LookupManager
     *
     * It is thread safe.
     *
     * @return
     *         the LookupManager implementation instance.
     */
    @Override
    public LookupManager getLookupManager() {
        return lookupManager;
    }

    /**
     * {@inheritDoc}
     * The method will be removed, don't use it
     */
    @Override
    @Deprecated
    public void initialize(DirectoryServiceClient client) {
        this.dirSvcClient = client;
    }

    /**
     * get the DirectoryServiceClient to access remote directory server.
     *
     * @return
     *         the DirectoryServiceClient.
     */
    @Override
    public DirectoryServiceClient getDirectoryServiceClient(){
        return dirSvcClient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServiceDirectoryConfig(ServiceDirectoryConfig config) {
        // TODO Auto-generated method stub

    }

    @Override
    public void start() {
        registrationManager.start();
        lookupManager.start();
    }

    @Override
    public void stop() {
        registrationManager.stop();
        lookupManager.stop();
    }

}
