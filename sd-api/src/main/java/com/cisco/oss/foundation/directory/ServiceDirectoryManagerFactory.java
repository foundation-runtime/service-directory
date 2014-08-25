package com.cisco.oss.foundation.directory;

import com.cisco.oss.foundation.directory.config.ServiceDirectoryConfig;
import com.cisco.oss.foundation.directory.exception.ServiceException;

/**
 * The ServiceDirectoryManagerFactory to instantialize LookupManager and RegistrationManager.
 * 
 * Two default ServiceDirectoryManagerFactory implementations are supplied:
 * DefaultServiceDirectoryManagerFactory for the production use.
 * TestServiceDirectoryManagerFactory for the integration test purpose.
 * 
 * @author zuxiang
 *
 */
public interface ServiceDirectoryManagerFactory{
	
	/**
	 * Get the RegistrationManager.
	 * 
	 * @return
	 * 		the RegistrationManager implementation instance.
	 * @throws ServiceException
	 */
	public RegistrationManager getRegistrationManager() throws ServiceException;
	
	/**
	 * Get the LookupManager.
	 * 
	 * @return
	 * 		the LookupManager implementation instance.
	 * @throws ServiceException
	 */
	public LookupManager getLookupManager() throws ServiceException;
	
	/**
	 * Initialize the factory with the DirectoryServiceClientManager.
	 * 
	 * @param manager
	 * 		The DirectoryServiceClientManager.
	 */
	public void initialize(DirectoryServiceClientManager manager);
	
	/**
	 * Set the ServiceDirectoryConfig for factory.
	 * 
	 * @param config
	 * 		the ServiceDirectory Configuration.
	 */
	public void setServiceDirectoryConfig(ServiceDirectoryConfig config);
}
