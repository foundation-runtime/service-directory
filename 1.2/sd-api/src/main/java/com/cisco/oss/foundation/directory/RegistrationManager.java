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




package com.cisco.oss.foundation.directory;

import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.exception.ServiceException;

/**
 * The service registration lifecycle management interface.
 *
 * This interface is intended for the service provider to register/update/unregister a ProvidedServiceInstance.
 *
 *
 */
public interface RegistrationManager extends AutoCloseable {

    /**
     * Register a new ProvidedServiceInstance.
     *
     * Register a new ProvidedServiceInstance to Service Directory.
     *
     * @param serviceInstance    The ProvidedServiceInstance.
     * @throws ServiceException
     */
    public void registerService(ProvidedServiceInstance serviceInstance) throws ServiceException;

    /**
     * Register a new ProviderServiceInstance with ServiceInstanceHealth.
     *
     * It registers a new ProviderServiceInstance and attaches a ServiceInstanceHealth callback.
     * Directory server will invoke ServiceInstanceHealth periodically to update the OperationalStatus of the ProviderServiceInstance on behalf of
     * the Service Provider.
     *
     * @param serviceInstance    The ProvidedServiceInstance.
     * @param registryHealth        The ServiceInstanceHealth.
     * @throws ServiceException
     */
    public void registerService(ProvidedServiceInstance serviceInstance, ServiceInstanceHealth registryHealth) throws ServiceException;

    /**
     * Update the OperationalStatus of the ProvidedServiceInstance.
     *
     * It is a convenient method to update the OperationalStatus of the ProvidedServiceInstance.
     *
     * @param serviceName   The name of the service.
     * @param providerId    The providerId of the ProvidedServiceInstance.
     * @param status        The OperationalStatus of the ProvidedServiceInstance.
     * @throws ServiceException
     */
    public void updateServiceOperationalStatus(String serviceName, String providerId, OperationalStatus status) throws ServiceException;

    /**
     * Update the uri attribute of the ProvidedServiceInstance.
     *
     * It is a convenient method to update the uri attribute of the ProvidedServiceInstance.
     *
     * @param serviceName   The name of the service.
     * @param providerId    The providerId of the ProvidedServiceInstance.
     * @param uri           The URI of the ProvidedServiceInstance.
     * @throws ServiceException
     */
    public void updateServiceUri(String serviceName, String providerId, String uri) throws ServiceException;


    /**
     * Update the ProvidedServiceInstance.
     *
     * Update the existing ProvidedServiceInstance.
     * The metadata Map in the ProvidedServiceInstance will not be updated when it is null.
     *
     * @param serviceInstance    The ProvidedServiceInstance.
     * @throws ServiceException
     */
    public void updateService(ProvidedServiceInstance serviceInstance) throws ServiceException;

    /**
     * Unregister the ProvidedServiceInstance.
     *
     * Unregister an existing ProvidedServiceInstance in the directory server.
     *
     * @param serviceName    The name of the Service.
     * @param providerId     The providerId of ProvidedServiceInstance.
     * @throws ServiceException
     */
    public void unregisterService(String serviceName, String providerId) throws ServiceException;

    /**
     * close the registration Manager, This method is invoked automatically if using JDK7 try-with-resource
     * @throws ServiceException when problem to close resource
     */
    @Override
    void close() throws ServiceException;
}
