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

import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;

/**
 * LookupManager interface for the service consumer.
 *
 * The service consumer uses this interface to look up or query ServiceInstance stored on the directory server.
 *
 *
 */
public interface LookupManager {

    /**
     * Look up a service instance by the service name.
     *
     * It selects one instance from a set of instances for a given service based on round robin strategy.
     *
     * @param serviceName    The Service name.
     * @return               The ServiceInstance.
     * @throws ServiceException
     */
    public ServiceInstance lookupInstance(String serviceName) throws ServiceException;

    /**
     * Look up a list of service instances for a given service.
     *
     * It returns the complete list of the UP service instances.
     *
     * @param serviceName    The Service name.
     * @return                The ServiceInstance list.
     * @throws ServiceException
     */
    public List<ServiceInstance> lookupInstances(String serviceName) throws ServiceException;

    /**
     * Query for a service instance based on the service name and some filtering criteria on the service metadata.
     *
     * It returns a service instance from the service instance list based on round robin selection strategy.
     * The ServiceInstance list is of the specified Service.
     *
     * @param serviceName    The Service name.
     * @param query            The ServiceInstanceQuery for filtering the service instances.
     * @return                The ServiceInstance.
     * @throws ServiceException
     */
    public ServiceInstance queryInstanceByName(String serviceName, ServiceInstanceQuery query) throws ServiceException;

    /**
     * Query for all the ServiceInstances of the specified Service, which satisfy the query criteria on the service metadata.
     *
     * It returns all ServiceInstances of specified Service that satisfy the ServiceInstanceQuery.
     * The ServiceInstance list is of the specified Service.
     *
     * @param serviceName    The Service name.
     * @param query            The ServiceInstanceQuery for filtering the service instances.
     * @return                The ServiceInstance list.
     * @throws ServiceException
     */
    public List<ServiceInstance> queryInstancesByName(String serviceName, ServiceInstanceQuery query) throws ServiceException;

    /**
     * Query for a ServiceInstance which satisfies the query criteria on the service metadata.
     *
     * It returns a service instance from the service instance list based on round robin selection strategy.
     * The ServiceInstance list may have different Services.
     *
     * @param query         The ServiceInstanceQuery for filtering the service instances.
     * @return                The ServiceInstance list.
     * @throws ServiceException
     */
    public ServiceInstance queryInstanceByMetadataKey(ServiceInstanceQuery query) throws ServiceException;

    /**
     * Query for all the ServiceInstances which satisfy the query criteria on the service metadata.
     *
     * It returns all ServiceInstances of different Services which satisfy the query criteria.
     *
     * @param query            The ServiceInstanceQuery for filtering the service instances.
     * @return                The ServiceInstance list.
     * @throws ServiceException
     */
    public List<ServiceInstance> queryInstancesByMetadataKey(ServiceInstanceQuery query) throws ServiceException;

    /**
     * Get a ServiceInstance.
     *
     * It returns a ServiceInstances of the Service including the ServiceInstance of OperationalStatus DOWN.
     *
     * @param serviceName
     *         the service name.
     * @param instanceId
     *         the instanceId
     * @return
     *         the ServiceInstance.
     * @throws ServiceException
     */
    public ServiceInstance getInstance(String serviceName, String instanceId) throws ServiceException;

    /**
     * Get all ServiceInstances of the target Service, including the DOWN ServiceInstance.
     *
     * It will return all ServiceInstances of the Service including the ServiceInstance of OperationalStatus DOWN.
     *
     * @param serviceName
     *         the service name.
     * @return
     *         the ServiceInstance List.
     * @throws ServiceException
     */
    public List<ServiceInstance> getAllInstances(String serviceName) throws ServiceException;

    /**
     * Get all ServiceInstances of the specified Service, including the DOWN ServiceIntance,
     * which satisfy the query criteria on the service metadata.
     *
     * It filters all ServiceInstances of the specified Service including the ServiceInstance of OperationalStatus DOWN,
     * against the ServiceInstanceQuery.
     *
     * @param serviceName
     *         the Service name.
     * @param query
     *         the ServiceInstanceQuery.
     * @return
     *         the ServiceInstance List.
     * @throws ServiceException
     */
    public List<ServiceInstance> getAllInstances(String serviceName, ServiceInstanceQuery query) throws ServiceException;

    /**
     * Get all the ServiceInstances, including the DOWN ServiceInstance, which satisfy the query criteria on the service metadata.
     *
     * It filters all ServiceInstances of different Services, including the DOWN ServiceInstance,
     * which satisfy the query criteria.
     *
     * @param query
     *         the ServiceInstanceQuery criteria.
     * @return
     *         the ServiceInstance List.
     * @throws ServiceException
     */
    public List<ServiceInstance> getAllInstancesByMetadataKey(ServiceInstanceQuery query) throws ServiceException;

    /**
     * Get all ServiceInstances in the ServiceDirectory including the DOWN ServiceInstance.
     *
     * @return
     *         the ServiceInstance List.
     * @throws ServiceException
     */
    public List<ServiceInstance> getAllInstances() throws ServiceException;

    /**
     * Add a NotificationHandler to the Service.
     *
     * This method checks the duplicated NotificationHandler for the serviceName, if the NotificationHandler
     * already exists for the serviceName, do nothing.
     *
     *
     * @param serviceName
     *         the service name.
     * @param handler
     *         the NotificationHandler for the service.
     * @throws ServiceException
     */
    public void addNotificationHandler(String serviceName, NotificationHandler handler) throws ServiceException;

    /**
     * Remove a NotificationHandler from the Service.
     *
     *
     * @param serviceName
     *         the service name.
     * @param handler
     *         the NotificationHandler for the service.
     * @throws ServiceException
     */
    public void removeNotificationHandler(String serviceName, NotificationHandler handler) throws ServiceException ;
}
