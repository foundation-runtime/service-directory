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

import com.cisco.oss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceDirectoryManagerFactory;
import com.cisco.oss.foundation.directory.config.ServiceDirectoryConfig;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.lifecycle.Closable;

/**
 * It is the default ServiceDirectoryManagerFactory to access remote ServiceDirectory server node.
 *
 * When there is no other ServiceDirectoryManagerFactory provider assigned, SD API will initialize
 * this class to provide ServiceDirectory services.
 *
 *
 */
public class DefaultServiceDirectoryManagerFactory implements
        ServiceDirectoryManagerFactory, Closable {

    /**
     * RegistrationManager, it is lazy initialized.
     */
    private final RegistrationManagerImpl registrationManager;

    /**
     * The LookupManager, it is lazy initialized.
     */
    private final LookupManagerImpl lookupManager;

    /**
     * The DirectoryServiceClientManager.
     * TODO: should be final
     * the field is not declared final because the reinit() method try to change the reference.
     */
    private DirectoryServiceClientManager dirSvcClientMgr;

    private static class DefaultDirectoryServiceClientManager implements DirectoryServiceClientManager{
        private final static DirectoryServiceClient _client = new DirectoryServiceClient();
        @Override
        public DirectoryServiceClient getDirectoryServiceClient() throws ServiceException {
            return _client;
        }
    }

    /**
     * Default constructor.
     */
    public DefaultServiceDirectoryManagerFactory(){
        dirSvcClientMgr = new DefaultDirectoryServiceClientManager();
        lookupManager= new LookupManagerImpl(dirSvcClientMgr);
        lookupManager.start();
        registrationManager = new RegistrationManagerImpl(dirSvcClientMgr);
        registrationManager.start();
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
    public LookupManager getLookupManager(){
        return lookupManager;
    }

    /**
     * {@inheritDoc}
     * The method will be removed, don't use it
     */
    @Override
    @Deprecated
    public void initialize(DirectoryServiceClientManager manager) {
        this.dirSvcClientMgr = manager;
    }

    /**
     * get the DirectoryServiceClientManager to access remote directory server.
     *
     * @return
     *         the DirectoryServiceClientManager.
     */
    @Override
    public DirectoryServiceClientManager getDirectoryServiceClientManager(){
        return dirSvcClientMgr;
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
        // do nothing now.
    }

    @Override
    public void stop() {
       registrationManager.stop();
       lookupManager.stop();
    }

}
