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
import com.cisco.oss.foundation.directory.lifecycle.Closable;

/**
 * It is the default ServiceDirectoryManagerFactory to access remote ServiceDirectory server node.
 *
 * When there is no other ServiceDirectoryManagerFactory provider assigned, SD API will instantialize
 * this class to provide ServiceDirectory services.
 *
 * @author zuxiang
 *
 */
public class DefaultServiceDirectoryManagerFactory implements
        ServiceDirectoryManagerFactory, Closable {

    /**
     * RegistrationManager, it is lazy initialized.
     */
    private RegistrationManagerImpl registrationManager;

    /**
     * The LookupManager, it is lazy initialized.
     */
    private LookupManagerImpl lookupManager;

    /**
     * The DirectoryServiceClientManager.
     */
    private DirectoryServiceClientManager dirSvcClientMgr;

    /**
     * Default constructor.
     */
    public DefaultServiceDirectoryManagerFactory(){
    }

    /**
     * Get RegistrationManager.
     *
     * It is thread safe in lazy initializing.
     *
     * @return
     *         the RegistrationManager implementation instance.
     */
    @Override
    public RegistrationManager getRegistrationManager(){
        if(registrationManager == null){
            synchronized(this){
                if(registrationManager == null){
                    RegistrationManagerImpl registration = new RegistrationManagerImpl(getDirectoryServiceClientManager());
                    registration.start();
                    registrationManager = registration;
                }
            }
        }
        return registrationManager;
    }

    /**
     * get LookupManager
     *
     * It is thread safe in lazy initializing.
     *
     * @return
     *         the LookupManager implementation instance.
     */
    @Override
    public LookupManager getLookupManager(){
        if(lookupManager == null){
            synchronized(this){
                if(lookupManager == null){
                    LookupManagerImpl lookup = new LookupManagerImpl(getDirectoryServiceClientManager());
                    lookup.start();
                    lookupManager = lookup;
                }
            }
        }
        return lookupManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(DirectoryServiceClientManager manager) {
        this.dirSvcClientMgr = manager;
    }

    /**
     * get the DirectoryServiceClientManager to access remote directory server.
     *
     * @return
     *         the DirectoryServiceClientManager.
     */
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
        if(registrationManager != null){
            ((RegistrationManagerImpl) registrationManager).stop();
        }

        if(lookupManager != null){
            ((LookupManagerImpl) lookupManager).stop();
        }
    }

}
