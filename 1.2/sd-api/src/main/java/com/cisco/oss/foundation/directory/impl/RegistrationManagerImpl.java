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

import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceInstanceHealth;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.exception.ServiceException;
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
     * Mark component started or not
     */
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * The DirectoryRegistrationService to do Service Registration.
     */
    private final DirectoryRegistrationService registrationService;

    /**
     * Constructor.
     *
     */
    public RegistrationManagerImpl(DirectoryRegistrationService service) {
        this.registrationService = service;
    }

    /**
     * Start the RegistrationManagerImpl.
     */
    @Override
    public void start(){
        isStarted.set(true);
    }

    /**
     * Stop the RegistrationManagerImpl
     *
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

        getRegistrationService().registerService(serviceInstance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerService(ProvidedServiceInstance serviceInstance,
            ServiceInstanceHealth registryHealth) throws ServiceException {

        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);
        getRegistrationService().registerService(serviceInstance, registryHealth);
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

        getRegistrationService().updateServiceUri(serviceName, providerId, uri);
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

        getRegistrationService().updateServiceOperationalStatus(serviceName, providerId, status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateService(ProvidedServiceInstance serviceInstance)
            throws ServiceException {
     
        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);
     
        getRegistrationService().updateService(serviceInstance);
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
        
        getRegistrationService().unregisterService(serviceName, providerId);
    }

    private DirectoryRegistrationService getRegistrationService(){

        return registrationService;
    }

}
