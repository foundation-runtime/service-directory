/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.vss.foundation.directory;

import com.cisco.vss.foundation.directory.entity.OperationalStatus;
import com.cisco.vss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.vss.foundation.directory.exception.ServiceException;

/**
 * The service registration lifecycle management interface.
 * 
 * This interface is intended for the service provider to register/update/unregister a ProvidedServiceInstance.
 * 
 * @author zuxiang
 *
 */
public interface RegistrationManager {

	/**
	 * Register a new ProvidedServiceInstance.
	 * 
	 * Register a new ProvidedServiceInstance to Service Directory.
	 * 
	 * @param serviceInstance	The ProvidedServiceInstance.
	 * @throws ServiceException
	 */
	public void registerService(ProvidedServiceInstance serviceInstance) throws ServiceException;
	
	/**
	 * Register a new ProvidedServiceInstance with OperationalStatus.
	 * 
	 * It is a convenient method to register a new ProvidedServiceInstance to Service Directory and set OperationalStatus together.
	 * 
	 * @param serviceInstance	The ProvidedServiceInstance.
	 * @param status			The OperationalStatus of the ProvidedServiceInstance.
	 * @throws ServiceException
	 */
	public void registerService(ProvidedServiceInstance serviceInstance, OperationalStatus status) throws ServiceException;
	
	/**
	 * Register a new ProviderServiceInstance with OperationalStatus and ServiceInstanceHealth.
	 * 
	 * It registers a new ProviderServiceInstance with OperationalStatus and attaches a ServiceInstanceHealth callback. 
	 * Directory server will invoke ServiceInstanceHealth periodically to update the OperationalStatus of the ProviderServiceInstance on behalf of 
	 * the Service Provider.
	 * 
	 * @param serviceInstance	The ProvidedServiceInstance.
	 * @param status			The OperationalStatus of the ProvidedServiceInstance.
	 * @param registryHealth		The ServiceInstanceHealth.
	 * @throws ServiceException
	 */
	public void registerService(ProvidedServiceInstance serviceInstance, OperationalStatus status, ServiceInstanceHealth registryHealth) throws ServiceException;
	
	/**
	 * Update the OperationalStatus of the ProvidedServiceInstance.
	 * 
	 * It is a convenient method to update the OperationalStatus of the ProvidedServiceInstance.
	 * 
	 * @param serviceName	The name of the service.
	 * @param providerId	The providerId of the ProvidedServiceInstance.
	 * @param status		The OperationalStatus of the ProvidedServiceInstance.
	 * @throws ServiceException
	 */
	public void updateServiceOperationalStatus(String serviceName, String providerId, OperationalStatus status) throws ServiceException;
	
	/**
	 * Update the URI of the ProvidedServiceInstance.
	 * 
	 * It is a convenient method to update the URI of the ProvidedServiceInstance.
	 * 
	 * @param serviceName	The name of the service.
	 * @param providerId	The providerId of the ProvidedServiceInstance.
	 * @param uri		The URI of the ProvidedServiceInstance.
	 * @throws ServiceException
	 */
	public void updateServiceUri(String serviceName, String providerId, String uri) throws ServiceException;
	
	
	/**
	 * Update the ProvidedServiceInstance.
	 * 
	 * Update the existing ProvidedServiceInstance.
	 * For the referenced metadata Map in the ProvidedServiceInstance, it will not update it when it is null. 
	 * 
	 * @param serviceInstance	The ProvidedServiceInstance.
	 * @throws ServiceException
	 */
	public void updateService(ProvidedServiceInstance serviceInstance) throws ServiceException;
	
	/**
	 * Unregister the ProvidedServiceInstance.
	 * 
	 * Unregister the existing ProvidedServiceInstance in the directory server.
	 * 
	 * @param serviceName	The name of the Service.
	 * @param providerId	The providerId of ProvidedServiceInstance.
	 * @throws ServiceException
	 */
	public void unregisterService(String serviceName, String providerId) throws ServiceException;
}
