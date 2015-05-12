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




package com.cisco.oss.foundation.directory.registration;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceInstanceHealth;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.impl.AbstractServiceDirectoryManager;
import com.cisco.oss.foundation.directory.impl.ServiceDirectoryService;
import com.cisco.oss.foundation.directory.utils.ServiceInstanceUtils;
/**
 * The RegistrationManager implementation.
 *
 *
 */
public class RegistrationManagerImpl extends AbstractServiceDirectoryManager implements RegistrationManager{

   public static final Logger LOGGER = LoggerFactory.getLogger(RegistrationManagerImpl.class);

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
        start();
    }

    /**
     * Start the RegistrationManagerImpl.
     */
    @Override
    public void start(){
        super.start();
        LOGGER.info("Registration Manager @{} is started", this);
    }

    /**
     * Stop the RegistrationManagerImpl
     *
     */
    @Override
    public void stop(){
        getRegistrationService().stop();
        LOGGER.info("Registration Manager @{} is stopped", this);
    }

    @Override
    public ServiceDirectoryService getService() {
        return getRegistrationService();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerService(ProvidedServiceInstance serviceInstance)
            throws ServiceException {

        ServiceInstanceUtils.validateManagerIsStarted(isStarted.get());
        ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);

        getRegistrationService().registerService(serviceInstance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerService(ProvidedServiceInstance serviceInstance,
            ServiceInstanceHealth registryHealth) throws ServiceException {

        ServiceInstanceUtils.validateManagerIsStarted(isStarted.get());
        ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);
        getRegistrationService().registerService(serviceInstance, registryHealth);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateServiceUri(String serviceName,
            String providerAddress, String uri) throws ServiceException {

        ServiceInstanceUtils.validateManagerIsStarted(isStarted.get());
        ServiceInstanceUtils.validateServiceName(serviceName);
        ServiceInstanceUtils.validateAddress(providerAddress);
        ServiceInstanceUtils.validateURI(uri);

        getRegistrationService().updateServiceUri(serviceName, providerAddress, uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateServiceOperationalStatus(String serviceName,
            String providerAddress, OperationalStatus status) throws ServiceException {

        ServiceInstanceUtils.validateManagerIsStarted(isStarted.get());
        ServiceInstanceUtils.validateServiceName(serviceName);
        ServiceInstanceUtils.validateAddress(providerAddress);

        getRegistrationService().updateServiceOperationalStatus(serviceName, providerAddress, status);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void updateServiceMetadata(String serviceName, String providerAddress, Map<String, String> metadata) throws ServiceException {

        ServiceInstanceUtils.validateManagerIsStarted(isStarted.get());
        ServiceInstanceUtils.validateServiceName(serviceName);
        ServiceInstanceUtils.validateAddress(providerAddress);

        getRegistrationService().updateServiceMetadata(serviceName, providerAddress, metadata);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public void updateService(ProvidedServiceInstance serviceInstance)
            throws ServiceException {
     
        ServiceInstanceUtils.validateManagerIsStarted(isStarted.get());
        ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);
     
        getRegistrationService().updateService(serviceInstance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterService(String serviceName, String providerAddress)
            throws ServiceException {
       
        ServiceInstanceUtils.validateManagerIsStarted(isStarted.get());
        ServiceInstanceUtils.validateServiceName(serviceName);
        ServiceInstanceUtils.validateAddress(providerAddress);
        
        getRegistrationService().unregisterService(serviceName, providerAddress);
    }

    private DirectoryRegistrationService getRegistrationService(){

        return registrationService;
    }

}
