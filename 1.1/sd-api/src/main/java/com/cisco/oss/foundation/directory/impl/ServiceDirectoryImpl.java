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
import com.cisco.oss.foundation.directory.exception.ServiceDirectoryError;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.lifecycle.Stoppable;

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
     * ServiceDirectory client.
    private DirectoryServiceClient client ;
     */

    /**
     * ServiceDirectoryManagerFactory.
     */
    private ServiceDirectoryManagerFactory directoryManagerFactory;

    /**
     * The singleton instance.
     */
    private final static ServiceDirectoryImpl instance = new ServiceDirectoryImpl();

    private boolean isShutdown = false;

    // Singleton, private the constructor.
    private ServiceDirectoryImpl() {
        String custProvider = getServiceDirectoryConfig()
                .getString(SD_API_SERVICE_DIRECTORY_MANAGER_FACTORY_PROVIDER_PROPERTY);

        if (custProvider != null && !custProvider.isEmpty()) {
            try {
                Class<?> provider = Class.forName(custProvider);
                if (ServiceDirectoryManagerFactory.class
                        .isAssignableFrom(provider)) {
                    directoryManagerFactory = (ServiceDirectoryManagerFactory) provider
                            .newInstance();
                    LOGGER.info("Initialize the ServiceDirectoryManager with customer implementation " + custProvider + ".");
                }
            } catch (Exception e) {
                LOGGER.error(
                        "Initialize ServiceDirectoryManagerFactory from "
                                + custProvider + " failed.", e);
            }
        }

        if (directoryManagerFactory == null) {
            LOGGER.info("Initialize the ServiceDirectoryManager with default implementation - StandServiceDirectoryManager.");
            directoryManagerFactory = new DefaultServiceDirectoryManagerFactory();
        }

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
     * Get the SD API version
     * 
     * @return
     *        the SD API version 
     */
    public String getVersion() {
        
        try {
            InputStream input = ServiceDirectoryImpl.class.getClassLoader()
                    .getResourceAsStream("version.txt");
            if (input == null) {
                return "Unknown";
            }
            Properties prop = new Properties();
            prop.load(input);
            input.close();
            return prop.getProperty("version", "Unknown");
        } catch (IOException e) {
            return "Unknown";
        }
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
            throw new IllegalArgumentException("The ServiceDirectoryManagerFactory cannot be NULL.");
        }

        synchronized(this){
            if(isShutdown){
                throw new ServiceException(new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_IS_SHUTDOWN));
            }

            if (this.directoryManagerFactory != null) {

                ((Stoppable) directoryManagerFactory).stop();


                LOGGER.info("Resetting ServiceDirectoryManagerFactory, old="
                        + this.directoryManagerFactory.getClass().getName()
                        + ", new=" + factory.getClass().getName() + ".");

                this.directoryManagerFactory = factory;
            } else {
                this.directoryManagerFactory = factory;
                LOGGER.info("Setting ServiceDirectoryManagerFactory,  factory="
                        + factory.getClass().getName() + ".");
            }
        }
    }

    /**
     * Get DirectoryServiceClient
     * @throws ServiceException
     *
     */
    public DirectoryServiceClient getDirectoryServiceClient() throws ServiceException{
        if (isShutdown) {
            ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_IS_SHUTDOWN);
            throw new ServiceException(error);
        }
        return getServiceDirectoryManagerFactory().getDirectoryServiceClientManager().getDirectoryServiceClient();
    }

    /**
     * Shut down the ServiceDirectory client and the ServiceDirectoryManagerFactory.
     */
    public synchronized void shutdown(){
        if (!isShutdown) {
            if (directoryManagerFactory != null) {
                ((Stoppable) directoryManagerFactory).stop();
                directoryManagerFactory = null;
            }
            this.isShutdown = true;
        }
    }

    /**
     * not properly implemented, just works as an opposite part with shutdown() method
     * since the shutdown() is not implemented properly also
     */
    public synchronized void restart(){
        if (directoryManagerFactory != null) {
            ((Stoppable) directoryManagerFactory).start();
        }
        isShutdown = false;
    }

    /**
     * Get the ServiceDirectoryManagerFactory.
     *
     * It is lazy initialized and thread safe.
     * It looks up the configuration "com.cisco.oss.foundation.directory.manager.factory.provider".
     * If the configuration is null or the provider instantialization fails, it will instantialize the DefaultServiceDirectoryManagerFactory.
     *
     * @return
     *         the ServiceDirectoryManagerFactory instance.
     */
    protected ServiceDirectoryManagerFactory getServiceDirectoryManagerFactory() throws ServiceException{

        if(directoryManagerFactory == null){
            if(isShutdown){
                ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_IS_SHUTDOWN);
                throw new ServiceException(error);
            }else{
                // should not allow to return a null
                // TODO, make directoryManagerFactory is immutable.
                // TODO. remove the initialize and reinit method in ServiceDirectoryManagerFactory and ServiceDirectory
                throw new IllegalStateException("ServiceDirectoryManagerFactory is null");
            }
        }
        return directoryManagerFactory;
    }

}
