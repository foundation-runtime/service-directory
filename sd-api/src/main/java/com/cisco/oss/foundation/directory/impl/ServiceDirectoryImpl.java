/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.Configurations;
import com.cisco.oss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceDirectoryManagerFactory;
import com.cisco.oss.foundation.directory.config.ServiceDirectoryConfig;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceDirectoryError;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.lifecycle.Closable;

/**
 * The ServiceDirectory context class.
 * 
 * This class initializes RegistrationManager, LookupManager, and ServiceDirectoryService with the getter methods.
 * 
 * @author zuxiang
 *
 */
public class ServiceDirectoryImpl implements DirectoryServiceClientManager{
	
	public static final Logger LOGGER = LoggerFactory.getLogger(ServiceDirectoryImpl.class);
	
	/**
	 * The customer ServiceDirectoryManagerFactory implementation class name property name.
	 */
	public static final String SD_API_SERVICE_DIRECTORY_MANAGER_FACTORY_PROVIDER_PROPERTY = "service.directory.manager.factory.provider";
	
	/**
	 * ServiceDirectory server node client, it is lazy initialized.
	 */
	private DirectoryServiceClient client ;
	
	/**
	 * ServiceDirectoryManagerFactory, it is lazy initialized.
	 */
	private ServiceDirectoryManagerFactory directoryManagerFactory;
	
	/**
	 * The singleton instance.
	 */
	private static ServiceDirectoryImpl instance = new ServiceDirectoryImpl(); 
	
	private boolean isShutdown = false;
	
	// Singleton, private the constructor.
	private ServiceDirectoryImpl() {
	}
	
	/**
	 * Get the ServiceDirectoryImpl singleton instance.
	 * 
	 * @return
	 * 		the ServiceDirectoryImpl instance.
	 */
	public static ServiceDirectoryImpl getInstance(){
		return instance;
	}
	
	/**
	 * Get RegistrationManager from ServiceDirectoryManagerFactory.
	 * 
	 * @return
	 * 		the RegistrationManager instance.
	 * @throws ServiceException 
	 */
	public RegistrationManager getRegistrationManager() throws ServiceException{
		return getServiceDirectoryManagerFactory().getRegistrationManager();
	}
	
	/**
	 * get LookupManager from ServiceDirectoryManagerFactory.
	 * 
	 * @return
	 * 		the LookupManager instance.
	 * @throws ServiceException 
	 */
	public LookupManager getLookupManager() throws ServiceException{
		return getServiceDirectoryManagerFactory().getLookupManager();
	}
	
	/**
	 * Get the ServiceDirectoryConfig in sd-api.
	 * 
	 * @return
	 * 		the ServiceDirectory configuration.
	 */
	public ServiceDirectoryConfig getServiceDirectoryConfig(){
		return new ServiceDirectoryConfig(Configurations.getConfiguration());
	}
	
	/**
	 * Set the ServiceDirectoryManagerFactory in the sd-api.
	 * 
	 * @param factory
	 * 		the ServiceDirectoryManagerFactory.
	 * @throws ServiceException 
	 */
	public void reinitServiceDirectoryManagerFactory(ServiceDirectoryManagerFactory factory) throws ServiceException{
		if (factory == null) {
			throw new IllegalArgumentException("The ServiceDirectoryManagerFactory cannot be NULL.");
		}
			
		synchronized(this){
			if(isShutdown){
				ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_IS_SHUTDOWN);
				throw new ServiceException(error);
			}
			
			if (this.directoryManagerFactory != null) {

				if (directoryManagerFactory instanceof Closable) {
					((Closable) directoryManagerFactory).stop();
				}

				LOGGER.info("Resetting ServiceDirectoryManagerFactory, old="
						+ this.directoryManagerFactory.getClass().getName()
						+ ", new=" + factory.getClass().getName() + ".");

				this.directoryManagerFactory = factory;
			} else {
				this.directoryManagerFactory = factory;
				LOGGER.info("Setting ServiceDirectoryManagerFactory,  factory="
						+ factory.getClass().getName() + ".");
			}
			this.directoryManagerFactory.initialize(this);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @throws ServiceException 
	 */
	@Override
	public DirectoryServiceClient getDirectoryServiceClient() throws ServiceException{
		if(client == null){
			synchronized(this){
				if(isShutdown){
					ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_IS_SHUTDOWN);
					throw new ServiceException(error);
				}
				if(client == null){
					client = new DirectoryServiceClient();
				}
			}
		}
		return client;
	}
	
	/**
	 * Shutdown the ServiceDirectory and the ServiceDirectoryManagerFactory.
	 */
	public void shutdown(){
		synchronized(this){
			if(! isShutdown){
				if (directoryManagerFactory != null) {
					if (directoryManagerFactory instanceof Closable) {
						((Closable) directoryManagerFactory).stop();
					}
					directoryManagerFactory = null;
				}
				if (client != null) {
					client.close();
					client = null;
				}
				this.isShutdown = true;
			}
		}
	}
	
	/**
	 * Get the ServiceDirectoryManagerFactory.
	 * 
	 * It is lazy initialized and thread safe.
	 * It looks up the configuration "service.directory.manager.factory.provider".
	 * If the configuration is null or the provider instantialization fails, it will instantialize the DefaultServiceDirectoryManagerFactory.
	 * 
	 * @return
	 * 		the ServiceDirectoryManagerFactory instance.
	 */
	private ServiceDirectoryManagerFactory getServiceDirectoryManagerFactory() throws ServiceException{
		if(directoryManagerFactory == null){
			synchronized(this){
				if(isShutdown){
					ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_IS_SHUTDOWN);
					throw new ServiceException(error);
				}
				if(directoryManagerFactory == null){
					String custProvider = Configurations
							.getString(SD_API_SERVICE_DIRECTORY_MANAGER_FACTORY_PROVIDER_PROPERTY);
					
					if (custProvider != null && !custProvider.isEmpty()) {
						try {
							Class<?> provider = Class.forName(custProvider);
							if (ServiceDirectoryManagerFactory.class
									.isAssignableFrom(provider)) {
								directoryManagerFactory = (ServiceDirectoryManagerFactory) provider
										.newInstance();
								directoryManagerFactory.initialize(this);
								LOGGER.info("Instantialized the ServiceDirectoryManager with customer implementation " + custProvider + ".");
							}
						} catch (Exception e) {
							LOGGER.error(
									"Instantialize ServiceDirectoryManagerFactory from "
											+ custProvider + " failed.", e);
						}
					}

					if (directoryManagerFactory == null) {
						LOGGER.info("Instantialized the ServiceDirectoryManager with default implementation StandServiceDirectoryManager.");
						directoryManagerFactory = new DefaultServiceDirectoryManagerFactory();
						directoryManagerFactory.initialize(this);
					}
				}
			}
		}
		return directoryManagerFactory;
	}
	
	/**
	 * Keep it default for Unit test.
	 * Revert the ServiceDirectory from shutdown.
	 */
	void revertForUnitTest(){
		isShutdown= false;
	}
	
}
