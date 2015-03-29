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




package com.cisco.oss.foundation.directory;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.configuration.ConfigurationFactory;
import com.cisco.oss.foundation.directory.client.DirectoryServiceRestfulClient;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.impl.ServiceDirectoryImpl;
import com.cisco.oss.foundation.directory.lookup.CachedDirectoryLookupService;
import com.cisco.oss.foundation.directory.lookup.DirectoryLookupService;

import static com.cisco.oss.foundation.directory.impl.DefaultServiceDirectoryManagerFactory.SD_API_CACHE_ENABLED_PROPERTY;

/**
 * ServiceDirectory client class.
 *
 * Applications obtain the instance of LookupManager/RegistrationManager/ServiceDirectoryConfig from this class.
 *
 *
 */
public class ServiceDirectory {

    public static final Logger LOGGER = LoggerFactory.getLogger(ServiceDirectory.class);

    /**
     * The ServiceDirectory enabled property name, indicating whether ServiceDirectory is enabled for directory service.
     */
    public static final String SD_API_SERVICE_DIRECTORY_ENABLED_PROPERTY = "com.cisco.oss.foundation.directory.enabled";

    /**
     * Default is to enable ServiceDirectory for directory service.
     */
    public static final boolean SD_API_SERVICE_DIRECTORY_ENABLED_DEFAULT = true;

    /**
     * The default config load by foundation runtime from config.properties or configSchema.xml
     */
    private static final Configuration defaultConfigLoadByFoundationRuntime = ConfigurationFactory.getConfiguration();

    /**
     * Get the LookupManager.
     *
     * @return
     *         the implementation instance of LookupManager.
     * @throws ServiceException
     */
    @Deprecated
    public static LookupManager getLookupManager() throws ServiceException {
        return getImpl().getLookupManager();
    }

    /**
     * Get the RegistrationManager.
     *
     * @return
     *         the implementation instance of RegistrationManager.
     * @throws ServiceException
     */
    @Deprecated
    public static RegistrationManager getRegistrationManager() throws ServiceException {
        return getImpl().getRegistrationManager();
    }

    /**
     * Get the ServiceDirectoryConfig.
     *
     * @return
     *         the ServiceDirectoryConfig
     * @deprecated as release 1.2. it's globe configuration
     */
    @Deprecated
    public static Configuration getServiceDirectoryConfig() {
        return defaultConfigLoadByFoundationRuntime;
    }

    /**
     * Re-initialize the ServiceDirectoryManagerFactory.
     *
     * It allows applications to change the ServiceDirectoryManagerFactory in the runtime.
     *
     * @param factory
     *         the ServiceDirectoryManagerFactory which generates LookupManager and RegistrationManager.
     */
    @Deprecated
    public static void reinitServiceDirectoryManagerFactory(ServiceDirectoryManagerFactory factory) throws ServiceException{
        getImpl().reinitServiceDirectoryManagerFactory(factory);
    }

    /**
     * Check whether ServiceDirectory is enabled for the directory service.
     *
     * The value comes from the configuration property "com.cisco.oss.foundation.directory.enabled".
     *
     * By default the ServiceDirectory is enabled.
     *
     * @return
     *         true if the ServiceDirectory is enabled.
     */
    @Deprecated
    public static boolean isEnabled(){
        return getServiceDirectoryConfig().getBoolean(SD_API_SERVICE_DIRECTORY_ENABLED_PROPERTY,
                SD_API_SERVICE_DIRECTORY_ENABLED_DEFAULT);
    }

    /**
     * Get the Service Directory API version
     * 
     * @return
     *        the Service Directory API version 
     */
    public static String getAPIVersion() {
        return getImpl().getVersion();
    }
    
    /**
     * Shut down the ServiceDirectory.
     *
     * Be careful to invoke this method. When shutdown() is called, ServiceDirectory cannot be used 
     * unless jvm is restarted to reload the ServiceDirectory class. 
     */
    public static void shutdown(){
        getImpl().shutdown();
    }

    /**
     * Get the ServiceDirectory implementation.
     *
     * @return
     *         the ServiceDirectoryImpl instance.
     */
    @Deprecated
    private static ServiceDirectoryImpl getImpl() {
        return ServiceDirectoryImpl.getInstance();
    }

    // ------------
    // 1.2 API
    // ------------
    public static ServiceDirectoryConfig config(){ return new ServiceDirectoryConfig(); }
    public static ServiceDirectoryConfig globeConfig(){ return ServiceDirectoryConfig.GLOBE; }


    /**
     * SD is build by SDConfig
     */
    public final static class ServiceDirectoryConfig {
        public static final ServiceDirectoryConfig GLOBE = new ServiceDirectoryConfig(defaultConfigLoadByFoundationRuntime);
        private final Configuration _apacheConfig;

        //client user should not know it
        private ServiceDirectoryConfig(Configuration root) {
            _apacheConfig=root;
        }
        private ServiceDirectoryConfig(){
            _apacheConfig = new BaseConfiguration();
        }

        public ServiceDirectoryConfig setCacheEnabled(boolean cacheEnable){
            _set(SD_API_CACHE_ENABLED_PROPERTY, cacheEnable);
            return this;
        }
        private void _set(String key, Object value){
            if (this == GLOBE){
                LOGGER.warn("GLOBE ServiceDirectoryConfig changed! '{}' => '{}'",key,value);
            }
            _apacheConfig.setProperty(key,value);
        }
        public boolean isCacheEnabled(){
            if (_apacheConfig.containsKey(SD_API_CACHE_ENABLED_PROPERTY)){
                return _apacheConfig.getBoolean(SD_API_CACHE_ENABLED_PROPERTY);
            }else{
                return GLOBE.isCacheEnabled();
            }
        }


        // the builder of SD
        public ServiceDirectory build(){
            return new ServiceDirectory(this);
        }
    };
    /*
     * SD Constructor by using SD Config
     * The constructor is protected by private, so that only
     * builder can call it
     */
    private ServiceDirectory(ServiceDirectoryConfig config){
            this._config = config;
    }

    private final ServiceDirectoryConfig _config;

    public DirectoryLookupService getLookupService() throws ServiceException {
        if (_config.isCacheEnabled()){
            return new CachedDirectoryLookupService(new DirectoryServiceRestfulClient());
        }else {
            return new DirectoryLookupService(new DirectoryServiceRestfulClient());
        }
    }


}
