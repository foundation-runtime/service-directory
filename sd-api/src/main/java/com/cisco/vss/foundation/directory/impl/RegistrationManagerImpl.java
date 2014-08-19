/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.vss.foundation.directory.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.vss.foundation.directory.Configurations;
import com.cisco.vss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.vss.foundation.directory.RegistrationManager;
import com.cisco.vss.foundation.directory.ServiceInstanceHealth;
import com.cisco.vss.foundation.directory.entity.OperationalStatus;
import com.cisco.vss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.vss.foundation.directory.exception.ErrorCode;
import com.cisco.vss.foundation.directory.exception.ServiceDirectoryError;
import com.cisco.vss.foundation.directory.exception.ServiceException;
import com.cisco.vss.foundation.directory.exception.ServiceRuntimeException;
import com.cisco.vss.foundation.directory.lifecycle.Closable;
import com.cisco.vss.foundation.directory.utils.ServiceInstanceUtils;

/**
 * The RegistrationManager implementation.
 * 
 * @author zuxiang
 *
 */
public class RegistrationManagerImpl implements RegistrationManager, Closable{
	
	private static final Logger LOGGER = LoggerFactory
			.getLogger(RegistrationManagerImpl.class);
	
	/**
	 * The Registration heartbeat and health check enabled property name.
	 */
	public static final String SD_API_HEARTBEAT_ENABLED_PROPERTY = "heartbeat.enabled";
	
	/**
	 * the default value of hearbeat enabled property value.
	 */
	public static final boolean SD_API_HEARTBEAT_ENABLED_DEFAULT = true;
	
	/**
	 * The DirectoryServiceClientManager.
	 */
	private DirectoryServiceClientManager directoryServiceClientManager ;
	
	/**
	 * Mark component started or not
	 */
	private boolean isStarted = false;
	
	/**
	 * The DirectoryRegistrationService to do Service Registration.
	 */
	private DirectoryRegistrationService registrationService;
	
	/**
	 * Constructor.
	 * 
	 */
	public RegistrationManagerImpl(DirectoryServiceClientManager directoryServiceClientManager){
		this.directoryServiceClientManager = directoryServiceClientManager;
	}
	
	/**
	 * Start the RegistrationManagerImpl.
	 * 
	 * it is idempotent, it can be invoked multiple times while in same state and is not thread safe.
	 */
	@Override
	public void start(){
		if(!isStarted){
			synchronized (this) {
				if (!isStarted) {
					isStarted = true;
				}
			}
		}
		
	}
	
	/**
	 * Stop the RegistrationManagerImpl
	 * 
	 * it is idempotent, it can be invoked in multiple times while in same state. But not thread safe.
	 */
	@Override
	public void stop(){
		if (isStarted) {
			synchronized (this) {
				if (isStarted) {
					if (getRegistrationService() instanceof Closable) {
						((Closable) getRegistrationService()).stop();
					}
					isStarted = false;
				}
			}
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerService(ProvidedServiceInstance serviceInstance)
			throws ServiceException {
		if(serviceInstance == null){
			throw new IllegalArgumentException("The ServiceInstance argument is null.");
		}
		
		ErrorCode code = ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceInstance.getServiceName());
			throw new ServiceException(error);
		}
		try{
			getRegistrationService().registerService(serviceInstance);
		} catch(ServiceRuntimeException e){
			throw new ServiceException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerService(ProvidedServiceInstance serviceInstance,
			OperationalStatus status) throws ServiceException {
		
		if(serviceInstance == null){
			throw new IllegalArgumentException("The ServiceInstance argument is null.");
		}
		
		if(status == null){
			throw new IllegalArgumentException("The OperationalStatus argument is null.");
		}
		
		ErrorCode code = ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceInstance.getServiceName());
			throw new ServiceException(error);
		}
		
		try{
			getRegistrationService().registerService(serviceInstance, status);
		} catch(ServiceRuntimeException e){
			throw new ServiceException(e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerService(ProvidedServiceInstance serviceInstance, OperationalStatus status,
			ServiceInstanceHealth registryHealth) throws ServiceException {
		
		if(serviceInstance == null){
			throw new IllegalArgumentException("The ServiceInstance argument is null.");
		}
		
		if(status == null){
			throw new IllegalArgumentException("The OperationalStatus argument is null.");
		}
		
		if(registryHealth == null){
			throw new IllegalArgumentException("The ServiceInstanceHealth argument is null.");
		}
		
		ErrorCode code = ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceInstance.getServiceName());
			throw new ServiceException(error);
		}
		try{
			getRegistrationService().registerService(serviceInstance, status, registryHealth);
		} catch(ServiceRuntimeException e){
			throw new ServiceException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateServiceUri(String serviceName,
			String providerId, String uri) throws ServiceException {
		ErrorCode code = ServiceInstanceUtils.isNameValid(serviceName);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceName);
			throw new ServiceException(error);
		}
		
		code = ServiceInstanceUtils.isIdValid(providerId);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceName);
			throw new ServiceException(error);
		}
		
		code = ServiceInstanceUtils.isUriValid(uri);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceName);
			throw new ServiceException(error);
		}
		
		try{
			getRegistrationService().updateServiceUri(serviceName, providerId, uri);
		} catch(ServiceRuntimeException e){
			throw new ServiceException(e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateServiceOperationalStatus(String serviceName,
			String providerId, OperationalStatus status) throws ServiceException {
		
		ErrorCode code = ServiceInstanceUtils.isNameValid(serviceName);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceName);
			throw new ServiceException(error);
		}
		
		code = ServiceInstanceUtils.isIdValid(providerId);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceName);
			throw new ServiceException(error);
		}
		
        try{
			getRegistrationService().updateServiceOperationalStatus(serviceName, providerId, status);
		} catch(ServiceRuntimeException e){
			throw new ServiceException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateService(ProvidedServiceInstance serviceInstance)
			throws ServiceException {
		
		if(serviceInstance == null){
			throw new IllegalArgumentException("The ServiceInstance argument is null.");
		}
		
		ErrorCode code = ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceInstance.getServiceName());
			throw new ServiceException(error);
		}
		
		try{
			getRegistrationService().updateService(serviceInstance);
			
		} catch(ServiceRuntimeException e){
			throw new ServiceException(e);
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregisterService(String serviceName, String providerId)
			throws ServiceException {
		
		ErrorCode code = ServiceInstanceUtils.isNameValid(serviceName);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceName);
			throw new ServiceException(error);
		}
		
		code = ServiceInstanceUtils.isIdValid(providerId);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceName);
			throw new ServiceException(error);
		}
		
		try{
			getRegistrationService().unregisterService(serviceName, providerId);
		} catch(ServiceRuntimeException e){
			throw new ServiceException(e);
		}
	}
	
	private DirectoryRegistrationService getRegistrationService(){
		if(registrationService == null){
			synchronized(this){
				if(registrationService == null){
					boolean heartbeatEnabled = Configurations.getBoolean(SD_API_HEARTBEAT_ENABLED_PROPERTY,
							SD_API_HEARTBEAT_ENABLED_DEFAULT);
					if(heartbeatEnabled){
						HeartbeatDirectoryRegistrationService service = new HeartbeatDirectoryRegistrationService(directoryServiceClientManager);
						service.start();
						registrationService = service;
						LOGGER.info("Created the HeartbeatDirectoryRegistrationService in RegistrationManager");
					} else {
						registrationService = new DirectoryRegistrationService(directoryServiceClientManager);
						LOGGER.info("Created the DirectoryRegistrationService in RegistrationManager");
					}
				}
			}
		}
		return registrationService;
	}
	
}
