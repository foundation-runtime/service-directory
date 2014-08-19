/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.vss.foundation.directory.impl;

import com.cisco.vss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.vss.foundation.directory.ServiceInstanceHealth;
import com.cisco.vss.foundation.directory.entity.OperationalStatus;
import com.cisco.vss.foundation.directory.entity.ProvidedServiceInstance;

/**
 * It is the Directory Registration Service to perform the ServiceInstance registration.
 * 
 * It registers ServiceInstance to DirectoryServer. 
 * 
 * @author zuxiang
 *
 */
public class DirectoryRegistrationService {

	/**
	 * The remote ServiceDirectory node client.
	 */
	private final DirectoryServiceClientManager directoryServiceClientManager;

	/**
	 * Constructor.
	 * 
	 * @param directoryServiceClientManager
	 * 		DirectoryServiceClientManager to get DirectoryServiceClient.
	 */
	public DirectoryRegistrationService(
			DirectoryServiceClientManager directoryServiceClientManager) {
		this.directoryServiceClientManager = directoryServiceClientManager;
	}

	/**
	 * Register a ProvidedServiceInstance.
	 * 
	 * @param serviceInstance
	 * 		the ProvidedServiceInstance.
	 */
	public void registerService(ProvidedServiceInstance serviceInstance) {
		getServiceDirectoryClient().registerInstance(serviceInstance);
	}

	/**
	 * Register a ProvidedServiceInstance with the OperationalStatus.
	 * 
	 * @param serviceInstance
	 * 		the ProvidedServiceInstance.
	 * @param status
	 * 		the OperationalStatus of the ProvidedServiceInstance.
	 */
	public void registerService(ProvidedServiceInstance serviceInstance,
			OperationalStatus status) {

		serviceInstance.setStatus(status);
		registerService(serviceInstance);
	}

	/**
	 * Register a ProvidedServiceInstance with the OperationalStatus and the ServiceInstanceHealth callback.
	 * 
	 * @param serviceInstance
	 * 		the ProvidedServiceInstance.
	 * @param status
	 * 		the OperationalStatus of the ProvidedServiceInstance.
	 * @param registryHealth
	 * 		the ServiceInstanceHealth callback.
	 */
	public void registerService(ProvidedServiceInstance serviceInstance,
			OperationalStatus status, ServiceInstanceHealth registryHealth) {
		registerService(serviceInstance, status);
	}

	/**
	 * Update the uri of the ProvidedServiceInstance by serviceName and providerId.
	 * 
	 * @param serviceName
	 * 		the serviceName of the ProvidedServiceInstance.
	 * @param providerId
	 * 		the providerId of the ProvidedServiceInstance.
	 * @param uri
	 * 		the new uri.
	 */
	public void updateServiceUri(String serviceName, String providerId,
			String uri) {
		getServiceDirectoryClient().updateInstanceUri(serviceName, providerId,
				uri);
	}

	/**
	 * Update the OperationalStatus of the ProvidedServiceInstance by serviceName and providerId.
	 * 
	 * @param serviceName
	 * 		the serviceName of the ProvidedServiceInstance.
	 * @param providerId
	 * 		the providerId of the ProvidedServiceInstance.
	 * @param status
	 * 		the new OperationalStatus of the ProvidedServiceInstance.
	 */
	public void updateServiceOperationalStatus(String serviceName,
			String providerId, OperationalStatus status) {
		getServiceDirectoryClient().updateInstanceStatus(serviceName,
				providerId, status);

	}

	/**
	 * Update the ProvidedServiceInstance.
	 * 
	 * @param serviceInstance
	 * 		the ProvidedServiceInstance.
	 */
	public void updateService(ProvidedServiceInstance serviceInstance) {
		getServiceDirectoryClient().updateInstance(serviceInstance);

	}

	/**
	 * Unregister a ProvidedServiceInstance by serviceName and providerId.
	 * 
	 * @param serviceName
	 * 		the serviceName of ProvidedServiceInstance.
	 * @param providerId
	 * 		the provierId of ProvidedServiceInstance.
	 */
	public void unregisterService(String serviceName, String providerId) {
		getServiceDirectoryClient().unregisterInstance(serviceName, providerId);
	}

	/**
	 * Get the DirectoryServiceClient.
	 * 
	 * @return the DirectoryServiceClient to access remote directory server.
	 */
	protected DirectoryServiceClient getServiceDirectoryClient() {
		return directoryServiceClientManager.getDirectoryServiceClient();
	}
}
