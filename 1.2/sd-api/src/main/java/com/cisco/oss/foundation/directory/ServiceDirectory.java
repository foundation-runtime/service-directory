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

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.configuration.ConfigurationFactory;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.impl.ServiceDirectoryConfig;
import com.cisco.oss.foundation.directory.impl.ServiceDirectoryImpl;
import com.cisco.oss.foundation.directory.impl.ServiceDirectoryVersion;

/**
 * ServiceDirectory client class.
 *
 * Applications obtain the instance of LookupManager/RegistrationManager/Configuration from this class.
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
     * The default configuration loaded by foundation runtime from configSchema.xml
     */
    private static final Configuration defaultServiceDirectoryConfig = ConfigurationFactory.getConfiguration();

    /**
     * The static reference to ServiceDirectoryImpl
     */
    private static final AtomicReference<ServiceDirectoryImpl> sdImplRef = new AtomicReference<>();



    /**
     * Get the LookupManager.
     *
     * @return
     *         the implementation instance of LookupManager.
     * @throws ServiceException
     */
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
    public static RegistrationManager getRegistrationManager() throws ServiceException {
        return getImpl().getRegistrationManager();
    }

    /**
     * Get the default configuration of ServiceDirectory.
     *
     * @return
     *         the ServiceDirectoryConfig
     */
    public static Configuration getServiceDirectoryConfig() {
        return defaultServiceDirectoryConfig;
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
        return ServiceDirectoryVersion.getVersion();
    }
    
    /**
     * Shut down the ServiceDirectory.
     *
     * Be careful to invoke this method. When shutdown() is called, ServiceDirectory cannot be used 
     * unless reset() method is called.
     *
     * @see #reset()
     *
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
    private static ServiceDirectoryImpl getImpl() {
        sdImplRef.compareAndSet(null,ServiceDirectoryImpl.getInstance());
        return sdImplRef.get();
    }

    // ------------
    // 1.2 API
    // ------------

    /**
     * Reset the ServiceDirectory
     *
     * @since 1.2
     */
    public static void reset(){
        getImpl().restart();
    }


}
