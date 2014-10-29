/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory;

import com.cisco.oss.foundation.directory.config.ServiceDirectoryConfig;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.impl.ServiceDirectoryImpl;

/**
 * ServiceDirectory client class.
 * 
 * Applications obtain the instance of LookupManager/RegistrationManager/ServiceDirectoryConfig from this class.
 * 
 * @author zuxiang
 *
 */
public class ServiceDirectory {
	
	/**
	 * The ServiceDirectory enable property name, indicating whether ServiceDirectory enabled for directory service.
	 */
	public static final String SD_API_SERVICE_DIRECTORY_ENABLED_PROPERTY = "service.directory.enabled";
	
	/**
	 * Default value to enable ServiceDirectory for directory service.
	 */
	public static final boolean SD_API_SERVICE_DIRECTORY_ENABLED_DEFAULT = true;
	
	/**
	 * Singleton, private constructor.
	 */
	private ServiceDirectory(){
	}
	
	/**
	 * Get the LookupManager.
	 * 
	 * @return
	 * 		the implementation instance of LookupManager.
	 * @throws ServiceException 
	 */
	public static LookupManager getLookupManager() throws ServiceException {
		return getImpl().getLookupManager();
	}
	
	/**
	 * Get the RegistrationManager.
	 * 
	 * @return
	 * 		the implementation instance of RegistrationManager.
	 * @throws ServiceException 
	 */
	public static RegistrationManager getRegistrationManager() throws ServiceException {
		return getImpl().getRegistrationManager();
	}
	
	/**
	 * Get the ServiceDirectoryConfig.
	 * 
	 * @return
	 * 		the ServiceDirectoryConfig of the SD API.
	 */
	public static ServiceDirectoryConfig getServiceDirectoryConfig() {
		return getImpl().getServiceDirectoryConfig();
	}
	
	/**
	 * Re-initialize the ServiceDirectoryManagerFactory.
	 * 
	 * It allows applications to change the ServiceDirectoryManagerFactory in the runtime.
	 * 
	 * @param factory
	 * 		the ServiceDirectoryManagerFactory which generates LookupManager and RegistrationManager.
	 */
	public static void reinitServiceDirectoryManagerFactory(ServiceDirectoryManagerFactory factory) throws ServiceException{
		getImpl().reinitServiceDirectoryManagerFactory(factory);
	}
	
	/**
	 * Check whether ServiceDirectory is enabled for the directory service.
	 * 
	 * The value comes from the SD API configuration property "service.directory.enabled".
	 * 
	 * By default the ServiceDirectory is enabled.
	 * 
	 * @return
	 * 		true if the ServiceDirectory is enabled.
	 */
	public static boolean isEnabled(){
		return getServiceDirectoryConfig().getBoolean(SD_API_SERVICE_DIRECTORY_ENABLED_PROPERTY, 
				SD_API_SERVICE_DIRECTORY_ENABLED_DEFAULT);
	}

	/**
	 * Get the ServiceDirectory implementation.
	 * 
	 * @return
	 * 		the ServiceDirectoryImpl instance.
	 */
	private static ServiceDirectoryImpl getImpl() {
		return ServiceDirectoryImpl.getInstance();
	}
	
	/**
	 * Set the Directory User.
	 * 
	 * @param userName
	 * 		the user name.
	 * @param password
	 * 		the password of the user.
	 * @throws ServiceException
	 */
	public static void setUser(String userName, String password) throws ServiceException{
		getImpl().getDirectoryServiceClient().setUser(userName, password);
	}
	
	/**
	 * Shutdown the ServiceDirectory.
	 * 
	 * Be careful to invoke this method, it used to help gc
	 * in shutdown the jvm.
	 * The whole ServiceDirectory shutdown, it cannot be used unless restart the jvm 
	 * and reload the ServiceDirectory class. The methods throw SERVICE_DIRECTORY_IS_SHUTDOWN error.
	 * 
	 * 
	 */
	public static void shutdown(){
		getImpl().shutdown();
	}
	
}
