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
import com.cisco.oss.foundation.directory.client.DirectoryServiceClient;
import com.cisco.oss.foundation.directory.client.DirectoryServiceRestfulClient;
import com.cisco.oss.foundation.directory.lookup.CachedDirectoryLookupService;
import com.cisco.oss.foundation.directory.lookup.CachedLookupManagerImpl;
import com.cisco.oss.foundation.directory.lookup.DirectoryLookupService;
import com.cisco.oss.foundation.directory.lookup.LookupManagerImpl;
import com.cisco.oss.foundation.directory.registration.DirectoryRegistrationService;
import com.cisco.oss.foundation.directory.registration.HeartbeatDirectoryRegistrationService;
import com.cisco.oss.foundation.directory.registration.HeartbeatRegistrationManagerImpl;
import com.cisco.oss.foundation.directory.registration.RegistrationManagerImpl;

import static com.cisco.oss.foundation.directory.ServiceDirectory.getServiceDirectoryConfig;

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
     * The LookupManager cache enabled property.
     */
    public static final String SD_API_CACHE_ENABLED_PROPERTY = "com.cisco.oss.foundation.directory.cache.enabled";

    /**
     * The default cache enabled property value.
     */
    public static final boolean SD_API_CACHE_ENABLED_DEFAULT = true;

    /**
     * The Registration heartbeat and health check enabled property name.
     */
    public static final String SD_API_HEARTBEAT_ENABLED_PROPERTY = "com.cisco.oss.foundation.directory.heartbeat.enabled";

    /**
     * the default value of hearbeat enabled property value.
     */
    public static final boolean SD_API_HEARTBEAT_ENABLED_DEFAULT = true;


    private static final DirectoryServiceRestfulClient dirSvcRestfulClient = new DirectoryServiceRestfulClient();

    /**
     * The XXXImpl holders for wired lazy-init requirement
     * Sigh! where is spring!
     */
    private static class LookupManagerImplHolder {
        public static final LookupManagerImpl INSTANCE =
                new LookupManagerImpl(new DirectoryLookupService(dirSvcRestfulClient));
    }
    private static class CachedLookupManagerImplHolder {
        public static final CachedLookupManagerImpl INSTANCE =
                new CachedLookupManagerImpl(new CachedDirectoryLookupService(dirSvcRestfulClient));
    }
    private static class HeartbeatRegistrationManagerImplHolder {
        public static final HeartbeatRegistrationManagerImpl INSTANCE
                = new HeartbeatRegistrationManagerImpl(new HeartbeatDirectoryRegistrationService(dirSvcRestfulClient));
    }
    private static class RegistrationManagerImplHolder {
        public static final RegistrationManagerImpl INSTANCE
                = new RegistrationManagerImpl(new DirectoryRegistrationService(dirSvcRestfulClient));
    }

    /**
     * Default constructor.
     */
    public DefaultServiceDirectoryManagerFactory(){
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
       return _getRegistrationMgrImpl();
    }
    private static RegistrationManagerImpl _getRegistrationMgrImpl(){
        boolean heartbeatEnabled = getServiceDirectoryConfig().getBoolean(SD_API_HEARTBEAT_ENABLED_PROPERTY,
                SD_API_HEARTBEAT_ENABLED_DEFAULT);
        if(heartbeatEnabled){
            return HeartbeatRegistrationManagerImplHolder.INSTANCE;
        } else {
            return RegistrationManagerImplHolder.INSTANCE;
        }
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
        return _getLookupMgrImpl();
    }

    private static LookupManagerImpl _getLookupMgrImpl(){
        boolean cacheEnabled = getServiceDirectoryConfig().getBoolean(SD_API_CACHE_ENABLED_PROPERTY,
                SD_API_CACHE_ENABLED_DEFAULT);
        if(cacheEnabled){
            return CachedLookupManagerImplHolder.INSTANCE;
        } else {
            return LookupManagerImplHolder.INSTANCE;
        }
    }

    /**
     * get the DirectoryServiceClient to access remote directory server.
     *
     * @return
     *         the DirectoryServiceClient.
     */
    @Override
    public DirectoryServiceClient getDirectoryServiceClient(){
        return dirSvcRestfulClient;
    }


    @Override
    public void start() {
        _getRegistrationMgrImpl().start();
        _getLookupMgrImpl().start();
    }

    @Override
    public void stop() {
        _getRegistrationMgrImpl().stop();
        _getLookupMgrImpl().stop();
    }

}
