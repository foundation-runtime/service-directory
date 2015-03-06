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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.Configurations;
import com.cisco.oss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceInstanceHealth;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceDirectoryError;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.exception.ServiceRuntimeException;
import com.cisco.oss.foundation.directory.lifecycle.Closable;
import com.cisco.oss.foundation.directory.utils.ServiceInstanceUtils;

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
    private volatile boolean isStarted = false;

    /**
     * The DirectoryRegistrationService to do Service Registration.
     */
    private volatile DirectoryRegistrationService registrationService;

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
        if(! isStarted){
            ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
            throw new ServiceException(error);
        }

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
            ServiceInstanceHealth registryHealth) throws ServiceException {
        if(! isStarted){
            ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
            throw new ServiceException(error);
        }
        if(serviceInstance == null){
            throw new IllegalArgumentException("The ServiceInstance argument is null.");
        }

        ErrorCode code = ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);
        if(! code.equals(ErrorCode.OK)){
            ServiceDirectoryError error = new ServiceDirectoryError(code, serviceInstance.getServiceName());
            throw new ServiceException(error);
        }

        try{
            getRegistrationService().registerService(serviceInstance, registryHealth);
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
        if(! isStarted){
            ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
            throw new ServiceException(error);
        }

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
        if(! isStarted){
            ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
            throw new ServiceException(error);
        }
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
        if(! isStarted){
            ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
            throw new ServiceException(error);
        }
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
        if(! isStarted){
            ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
            throw new ServiceException(error);
        }
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
