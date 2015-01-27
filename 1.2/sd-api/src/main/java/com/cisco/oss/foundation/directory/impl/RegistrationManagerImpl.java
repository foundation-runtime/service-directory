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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceInstanceHealth;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.Permission;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.User;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceDirectoryError;
import com.cisco.oss.foundation.directory.exception.ServiceException;
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
        if(! isStarted){
            ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
            throw new ServiceException(error);
        }

        if(serviceInstance == null){
            throw new IllegalArgumentException("The ServiceInstance can not be null.");
        }

        ErrorCode code = ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);
        if(! code.equals(ErrorCode.OK)){
            ServiceDirectoryError error = new ServiceDirectoryError(code);
            throw new ServiceException(error);
        }

        getRegistrationService().registerService(serviceInstance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerService(ProvidedServiceInstance serviceInstance, ServiceInstanceHealth registryHealth) throws ServiceException {
        if(! isStarted){
            ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
            throw new ServiceException(error);
        }
        if(serviceInstance == null){
            throw new IllegalArgumentException("The ServiceInstance can not be null.");
        }

        if(registryHealth == null){
            throw new IllegalArgumentException("The ServiceInstanceHealth can not be null.");
        }

        ErrorCode code = ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);
        if(! code.equals(ErrorCode.OK)){
            ServiceDirectoryError error = new ServiceDirectoryError(code);
            throw new ServiceException(error);
        }

        getRegistrationService().registerService(serviceInstance, registryHealth);

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
            ServiceDirectoryError error = new ServiceDirectoryError(code);
            throw new ServiceException(error);
        }

        code = ServiceInstanceUtils.isIdValid(providerId);
        if(! code.equals(ErrorCode.OK)){
            ServiceDirectoryError error = new ServiceDirectoryError(code);
            throw new ServiceException(error);
        }

        code = ServiceInstanceUtils.isUriValid(uri);
        if(! code.equals(ErrorCode.OK)){
            ServiceDirectoryError error = new ServiceDirectoryError(code);
            throw new ServiceException(error);
        }

        getRegistrationService().updateServiceUri(serviceName, providerId, uri);
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
            ServiceDirectoryError error = new ServiceDirectoryError(code);
            throw new ServiceException(error);
        }

        code = ServiceInstanceUtils.isIdValid(providerId);
        if(! code.equals(ErrorCode.OK)){
            ServiceDirectoryError error = new ServiceDirectoryError(code);
            throw new ServiceException(error);
        }

        getRegistrationService().updateServiceOperationalStatus(serviceName, providerId, status);
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
            throw new IllegalArgumentException("The ServiceInstance can not be null.");
        }

        ErrorCode code = ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);
        if(! code.equals(ErrorCode.OK)){
            ServiceDirectoryError error = new ServiceDirectoryError(code);
            throw new ServiceException(error);
        }

        getRegistrationService().updateService(serviceInstance);

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
            ServiceDirectoryError error = new ServiceDirectoryError(code);
            throw new ServiceException(error);
        }

        code = ServiceInstanceUtils.isIdValid(providerId);
        if(! code.equals(ErrorCode.OK)){
            ServiceDirectoryError error = new ServiceDirectoryError(code);
            throw new ServiceException(error);
        }

        getRegistrationService().unregisterService(serviceName, providerId);
    }

    private DirectoryRegistrationService getRegistrationService(){
        if(registrationService == null){
            synchronized(this){
                if(registrationService == null){
                    registrationService = new DirectoryRegistrationService(directoryServiceClientManager);
                    LOGGER.info("Created the DirectoryRegistrationService in RegistrationManager");
                }
            }
        }
        return registrationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createUser(User user, String password) throws ServiceException {
        if(! isStarted){
            ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
            throw new ServiceException(error);
        }
        if(user == null){
            throw new IllegalArgumentException("The User can not be null.");
        }

        getRegistrationService().createUser(user, password);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUser(String name) throws ServiceException {
        if(! isStarted){
            ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
            throw new ServiceException(error);
        }
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("The name can not be empty.");
        }

        return getRegistrationService().getUser(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUser(User user) throws ServiceException {
        if(! isStarted){
            ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
            throw new ServiceException(error);
        }
        if(user == null){
            throw new IllegalArgumentException("The User can not be null.");
        }

        getRegistrationService().updateUser(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteUser(String name) throws ServiceException {
        if(! isStarted){
            ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
            throw new ServiceException(error);
        }
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("The name can not be empty.");
        }

        getRegistrationService().deleteUser(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUserPermission(String userName, List<Permission> permissions)
            throws ServiceException {
        if(! isStarted){
            ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
            throw new ServiceException(error);
        }
        if(userName == null || userName.isEmpty()){
            throw new IllegalArgumentException("The user name can not be empty.");
        }

        getRegistrationService().setUserPermission(userName, permissions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> getAllUsers() throws ServiceException {
        if(! isStarted){
            ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
            throw new ServiceException(error);
        }

        return getRegistrationService().getAllUser();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUserPassword(String userName, String password)
            throws ServiceException {
        if(! isStarted){
            ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
            throw new ServiceException(error);
        }

        getRegistrationService().setUserPassword(userName, password);
    }

}
