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

import org.apache.commons.configuration.Configuration;

import com.cisco.oss.foundation.configuration.ConfigurationFactory;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.impl.ServiceDirectoryImpl;

/**
 * ServiceDirectory client class.
 *
 * Applications obtain the instance of LookupManager/RegistrationManager/ServiceDirectoryConfig from this class.
 *
 *
 */
public class ServiceDirectory {

    /**
     * The ServiceDirectory enabled property name, indicating whether ServiceDirectory is enabled for directory service.
     */
    public static final String SD_API_SERVICE_DIRECTORY_ENABLED_PROPERTY = "com.cisco.oss.foundation.directory.enabled";

    /**
     * Default is to enable ServiceDirectory for directory service.
     */
    public static final boolean SD_API_SERVICE_DIRECTORY_ENABLED_DEFAULT = true;


    private static final Configuration serviceDirectoryConfig = ConfigurationFactory.getConfiguration();

    /**
     * Singleton, private constructor.
     */
    private ServiceDirectory(){
    }

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
     * Get the ServiceDirectoryConfig.
     *
     * @return
     *         the ServiceDirectoryConfig
     */
    public static Configuration getServiceDirectoryConfig() {
        return serviceDirectoryConfig;
    }

    /**
     * Re-initialize the ServiceDirectoryManagerFactory.
     *
     * It allows applications to change the ServiceDirectoryManagerFactory in the runtime.
     *
     * @param factory
     *         the ServiceDirectoryManagerFactory which generates LookupManager and RegistrationManager.
     */
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
        return getImpl().getVersion();
    }
    
    /**
     * Shut down the ServiceDirectory.
     *
     * Be careful to invoke this method. When shutdown() is called, ServiceDirectory cannot be used 
     * unless jvm is restarted to reload the ServiceDirectory class. 
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
        return ServiceDirectoryImpl.getInstance();
    }

}
