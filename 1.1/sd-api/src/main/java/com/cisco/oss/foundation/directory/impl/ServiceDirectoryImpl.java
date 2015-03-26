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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceDirectoryManagerFactory;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceException;

import static com.cisco.oss.foundation.directory.ServiceDirectory.getServiceDirectoryConfig;

/**
 * The ServiceDirectory context class.
 *
 * This class initializes RegistrationManager, LookupManager, and ServiceDirectoryService with the getter methods.
 *
 *
 */
public class ServiceDirectoryImpl {

    public static final Logger LOGGER = LoggerFactory.getLogger(ServiceDirectoryImpl.class);

    /**
     * The customer ServiceDirectoryManagerFactory implementation class name property name.
     */
    public static final String SD_API_SERVICE_DIRECTORY_MANAGER_FACTORY_PROVIDER_PROPERTY = "com.cisco.oss.foundation.directory.manager.factory.provider";

    /**
     * ServiceDirectoryManagerFactory.
     */
    private ServiceDirectoryManagerFactory directoryManagerFactory;

    /**
     * The singleton instance.
     */
    private final static ServiceDirectoryImpl instance = new ServiceDirectoryImpl();

    private boolean isShutdown = false;

    private final String _version;

    // Singleton, private the constructor.
    private ServiceDirectoryImpl() {
        String version = "Unknown";
        try {
            InputStream input = ServiceDirectoryImpl.class.getClassLoader()
                    .getResourceAsStream("version.txt");
            if (input == null) {
            }
            Properties prop = new Properties();
            prop.load(input);
            input.close();
            if (prop.containsKey("version")) {
                version = prop.getProperty("version", "Unknown");
            }
        } catch (IOException e) {
        }
        _version = version;

        String custProvider = getServiceDirectoryConfig()
                .getString(SD_API_SERVICE_DIRECTORY_MANAGER_FACTORY_PROVIDER_PROPERTY);

        if (custProvider != null && !custProvider.isEmpty()) {
            try {
                Class<?> provider = Class.forName(custProvider);
                if (ServiceDirectoryManagerFactory.class
                        .isAssignableFrom(provider)) {
                    directoryManagerFactory = (ServiceDirectoryManagerFactory) provider
                            .newInstance();
                    LOGGER.info(
                            "Initialize the ServiceDirectoryManager with customer implementation {}.",
                            custProvider);
                }
            } catch (Exception e) {
                LOGGER.error(
                        "Initialize ServiceDirectoryManagerFactory from {} failed.",
                        custProvider, e);
            }
        }

        if (directoryManagerFactory == null) {
            LOGGER.info("Initialize the ServiceDirectoryManager with default implementation.");
            directoryManagerFactory = new DefaultServiceDirectoryManagerFactory();
        }
        directoryManagerFactory.start();

    }

    /**
     * Get the ServiceDirectoryImpl singleton instance.
     *
     * @return
     *         the ServiceDirectoryImpl instance.
     */
    public static ServiceDirectoryImpl getInstance(){
        return instance;
    }

    /**
     * Get RegistrationManager from ServiceDirectoryManagerFactory.
     *
     * @return
     *         the RegistrationManager instance.
     * @throws ServiceException
     */
    public RegistrationManager getRegistrationManager() throws ServiceException{
        return getServiceDirectoryManagerFactory().getRegistrationManager();
    }

    /**
     * get LookupManager from ServiceDirectoryManagerFactory.
     *
     * @return
     *         the LookupManager instance.
     * @throws ServiceException
     */
    public LookupManager getLookupManager() throws ServiceException{
        return getServiceDirectoryManagerFactory().getLookupManager();
    }


    /**
     * Get the Service Directory API version
     * 
     * @return
     *        the Service Directory API version 
     */
    public String getVersion() {
        return this._version;
    }

    /**
     * Set the ServiceDirectoryManagerFactory.
     *
     * @param factory
     *         the ServiceDirectoryManagerFactory.
     * @throws ServiceException
     */
    public void reinitServiceDirectoryManagerFactory(ServiceDirectoryManagerFactory factory) throws ServiceException{

        if (factory == null) {
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                     ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "ServiceDirectoryManagerFactory");
        }

        synchronized(this){
            if(isShutdown){
                throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_IS_SHUTDOWN);
            }

            if (this.directoryManagerFactory != null) {

                directoryManagerFactory.stop();
                LOGGER.info(
                        "Resetting ServiceDirectoryManagerFactory, old={}, new={}.",
                        this.directoryManagerFactory.getClass().getName(),
                        factory.getClass().getName());

                this.directoryManagerFactory = factory;
            } else {
                this.directoryManagerFactory = factory;
                LOGGER.info("Setting ServiceDirectoryManagerFactory, factory={}.",
                        factory.getClass().getName());
            }
            directoryManagerFactory.start();
        }

    }

    /**
     * Get DirectoryServiceClient
     * @throws ServiceException
     *
     */
    public DirectoryServiceClient getDirectoryServiceClient() throws ServiceException{
        if (isShutdown) {
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_IS_SHUTDOWN);
        }
        return getServiceDirectoryManagerFactory().getDirectoryServiceClient();
    }

    /**
     * Shut down the ServiceDirectory client and the ServiceDirectoryManagerFactory.
     */
    public synchronized void shutdown(){
        if (directoryManagerFactory != null) {
            directoryManagerFactory.stop();
        }
        this.isShutdown = true;
    }

    /**
     * not properly implemented, just works as an opposite part with shutdown() method
     * since the shutdown() is not implemented properly
     */
    public synchronized void restart(){
        if (directoryManagerFactory != null) {
            directoryManagerFactory.start();
        }
        isShutdown = false;
    }

    /**
     * Get the ServiceDirectoryManagerFactory.
     *
     * It looks up the configuration "com.cisco.oss.foundation.directory.manager.factory.provider".
     * If the configuration is null or the provider instantiation fails, it will instantiate the DefaultServiceDirectoryManagerFactory.
     *
     * @return
     *         the ServiceDirectoryManagerFactory instance.
     */
    protected ServiceDirectoryManagerFactory getServiceDirectoryManagerFactory() throws ServiceException{
        if(isShutdown){
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_IS_SHUTDOWN);
        }
        if(directoryManagerFactory == null){
            // should not allow to return a null
            // TODO, make directoryManagerFactory is immutable.
            // TODO. remove the initialize and reinit method in ServiceDirectoryManagerFactory and ServiceDirectory
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "ServiceDirectoryManagerFactory");
        }
        return directoryManagerFactory;
    }

}
