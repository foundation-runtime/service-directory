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

import java.util.List;

import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.Permission;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.User;

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
     * @param serviceInstance    The ProvidedServiceInstance.
     * @throws ServiceException
     */
    public void registerService(ProvidedServiceInstance serviceInstance);

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
    public void registerService(ProvidedServiceInstance serviceInstance, ServiceInstanceHealth registryHealth);

    /**
     * Update the OperationalStatus of the ProvidedServiceInstance.
     *
     * It is a convenient method to update the OperationalStatus of the ProvidedServiceInstance.
     *
     * @param serviceName    The name of the service.
     * @param providerId    The providerId of the ProvidedServiceInstance.
     * @param status        The OperationalStatus of the ProvidedServiceInstance.
     * @throws ServiceException
     */
    public void updateServiceOperationalStatus(String serviceName, String providerId, OperationalStatus status);

    /**
     * Update the URI of the ProvidedServiceInstance.
     *
     * It is a convenient method to update the URI of the ProvidedServiceInstance.
     *
     * @param serviceName    The name of the service.
     * @param providerId    The providerId of the ProvidedServiceInstance.
     * @param uri        The URI of the ProvidedServiceInstance.
     * @throws ServiceException
     */
    public void updateServiceUri(String serviceName, String providerId, String uri);


    /**
     * Update the ProvidedServiceInstance.
     *
     * Update the existing ProvidedServiceInstance.
     * For the referenced metadata Map in the ProvidedServiceInstance, it will not update it when it is null.
     *
     * @param serviceInstance    The ProvidedServiceInstance.
     * @throws ServiceException
     */
    public void updateService(ProvidedServiceInstance serviceInstance);

    /**
     * Unregister the ProvidedServiceInstance.
     *
     * Unregister the existing ProvidedServiceInstance in the directory server.
     *
     * @param serviceName    The name of the Service.
     * @param providerId    The providerId of ProvidedServiceInstance.
     * @throws ServiceException
     */
    public void unregisterService(String serviceName, String providerId);

    /**
     * Create a new User.
     *
     * @param user
     *         the User.
     * @throws ServiceException
     */
    public void createUser(User user, String password);

    /**
     * Get a User by the name.
     *
     * @param name
     *         the name of the User.
     * @return
     *         the User.
     * @throws ServiceException
     */
    public User getUser(String name);

    /**
     * Get all Users.
     *
     * @return
     *         all users.
     * @throws ServiceException
     */
    public List<User> getAllUsers();

    /**
     * Update the User.
     *
     * @param user
     *         the User.
     * @throws ServiceException
     */
    public void updateUser(User user);

    /**
     * Delete a User by name.
     *
     * @param name
     *         the name of the User.
     * @throws ServiceException
     */
    public void deleteUser(String name);

    /**
     * Set the permission list for the user.
     *
     * If the permissions is null or empty, it equals to set the User Permission to NONE.
     * The Permission should have a corresponding User, and delete the User will delete the corresponding Permission.
     *
     * @param userName
     *         the user name.
     * @param permissions
     *         the permission list of the user.
     * @throws ServiceException
     */
    public void setUserPermission(String userName, List<Permission> permissions);

    /**
     * Set the password for the User.
     *
     * @param userName
     *         the name of the User.
     * @param password
     *         the password String.
     * @throws ServiceException
     */
    public void setUserPassword(String userName, String password);
}
