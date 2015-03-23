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

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceInstanceHealth;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.exception.ServiceRuntimeException;
import com.cisco.oss.foundation.directory.lifecycle.Stoppable;
import com.cisco.oss.foundation.directory.utils.ServiceInstanceUtils;
import static com.cisco.oss.foundation.directory.ServiceDirectory.getServiceDirectoryConfig;
/**
 * The RegistrationManager implementation.
 *
 *
 */
public class RegistrationManagerImpl implements RegistrationManager, Stoppable{

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RegistrationManagerImpl.class);

    /**
     * The Registration heartbeat and health check enabled property name.
     */
    public static final String SD_API_HEARTBEAT_ENABLED_PROPERTY = "com.cisco.oss.foundation.directory.heartbeat.enabled";

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
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

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
        isStarted.set(true);
    }

    /**
     * Stop the RegistrationManagerImpl
     *
     * it is idempotent, it can be invoked in multiple times while in same state. But not thread safe.
     */
    @Override
    public void stop(){
        if (isStarted.compareAndSet(true,false)) {
            ((Stoppable) getRegistrationService()).stop();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerService(ProvidedServiceInstance serviceInstance)
            throws ServiceException {

        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);

        try {
           getRegistrationService().registerService(serviceInstance);
        } catch (ServiceRuntimeException e) {
            throw new ServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerService(ProvidedServiceInstance serviceInstance,
            ServiceInstanceHealth registryHealth) throws ServiceException {

        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);

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

        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateServiceName(serviceName);
        ServiceInstanceUtils.validateServiceInstanceID(providerId);
        ServiceInstanceUtils.validateURI(uri);

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

        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateServiceName(serviceName);
        ServiceInstanceUtils.validateServiceInstanceID(providerId);

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
     
        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);
     
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
       
        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateServiceName(serviceName);
        ServiceInstanceUtils.validateServiceInstanceID(providerId);
        
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
                    boolean heartbeatEnabled = getServiceDirectoryConfig().getBoolean(SD_API_HEARTBEAT_ENABLED_PROPERTY,
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
