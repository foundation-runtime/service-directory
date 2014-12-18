/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory;

import java.util.List;

import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;

/**
 * LookupManager interface for the service consumer.
 * 
 * The service consumer uses this interface to look up or query ServiceInstance stored on the directory server.
 * 
 * @author zuxiang
 *
 */
public interface LookupManager {
	
	/**
	 * Look up a service instance by the service name.
	 * 
	 * It selects one instance from a set of instances for a given service based on round robin strategy.
	 * 
	 * @param serviceName	The Service name.
	 * @return				The ServiceInstance.
	 * @throws ServiceException
	 */
	public ServiceInstance lookupInstance(String serviceName);
	
	/**
	 * Look up a list of service instances for a given service.
	 * 
	 * It returns the complete list of the service instances.
	 * 
	 * @param serviceName	The Service name.
	 * @return				The ServiceInstance list.
	 * @throws ServiceException
	 */
	public List<ServiceInstance> lookupInstances(String serviceName);
	
	/**
	 * Query for a service instance based on the service name and some filtering criteria on the service metadata.
	 * 
	 * It returns a service instance from the service instance list based on round robin selection strategy.
	 * The ServiceInstance list is of the specified Service. 
	 * 
	 * @param serviceName	The Service name.
	 * @param query			The ServiceInstanceQuery for filtering the service instances.
	 * @return				The ServiceInstance.
	 * @throws ServiceException
	 */
	public ServiceInstance queryInstanceByName(String serviceName, ServiceInstanceQuery query);
	
	/**
	 * Query for all the ServiceInstances of the specified Service, which satisfy the query criteria on the service metadata.
	 * 
	 * It returns all ServiceInstances of specified Service that satisfy the ServiceInstanceQuery.
	 * The ServiceInstance list is of the specified Service.
	 * 
	 * @param serviceName	The Service name.
	 * @param query			The ServiceInstanceQuery for filtering the service instances.
	 * @return				The ServiceInstance list.
	 * @throws ServiceException
	 */
	public List<ServiceInstance> queryInstancesByName(String serviceName, ServiceInstanceQuery query);
	
	/**
	 * Query for one the ServiceInstances which satisfy the query criteria on the service metadata.
	 * 
	 * It returns a service instance from the service instance list based on round robin selection strategy.
	 * The ServiceInstance list have different Services.
	 * 
	 * @param query 		The ServiceInstanceQuery for filtering the service instances.
	 * @return				The ServiceInstance list.
	 * @throws ServiceException
	 */
	public ServiceInstance queryInstanceByKey(ServiceInstanceQuery query);
	
	/**
	 * Query for all the ServiceInstances which satisfy the query criteria on the service metadata.
	 * 
	 * It returns all ServiceInstances of different Services which satisfy the query criteria.
	 * 
	 * @param query			The ServiceInstanceQuery for filtering the service instances.
	 * @return				The ServiceInstance list.
	 * @throws ServiceException
	 */
	public List<ServiceInstance> queryInstancesByKey(ServiceInstanceQuery query);
	
	/**
	 * Get a ServiceInstance.
	 * 
	 * It returns a ServiceInstances of the Service including the ServiceInstance of OperationalStatus DOWN.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param instanceId
	 * 		the istanceId
	 * @return
	 * 		the ServiceInstance.
	 * @throws ServiceException
	 */
	public ServiceInstance getInstance(String serviceName, String instanceId);
	
	/**
	 * Get all ServiceInstance List of the target Service, including the DOWN ServiceInstance.
	 * 
	 * It will return all ServiceInstances of the Service including the ServiceInstance of OperationalStatus DOWN.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @return
	 * 		the ServiceInstance List.
	 * @throws ServiceException
	 */
	public List<ServiceInstance> getAllInstances(String serviceName);
	
	/**
	 * Get all ServiceInstances of the specified Service, including the DOWN ServiceIntance, 
	 * which satisfy the query criteria on the service metadata.
	 * 
	 * It filter all ServiceInstances of the specified Service including the ServiceInstance of OperationalStatus DOWN, 
	 * against the ServiceInstanceQuery.
	 * 
	 * @param serviceName
	 * 		the Service name.
	 * @param query
	 * 		the ServiceInstanceQuery.
	 * @return
	 * 		the ServiceInstance List.
	 * @throws ServiceException
	 */
	public List<ServiceInstance> getAllInstances(String serviceName, ServiceInstanceQuery query);
	
	/**
	 * Get all the ServiceInstances, including the DOWN ServiceInstance, which satisfy the query criteria on the service metadata.
	 * 
	 * It filters all ServiceInstances of different Services, including the DOWN ServiceInstance,
	 * which satisfy the query criteria.
	 * 
	 * @param query
	 * 		the ServiceInstanceQuery criteria.
	 * @return
	 * 		the ServiceInstance List.
	 * @throws ServiceException
	 */
	public List<ServiceInstance> getAllInstancesByKey(ServiceInstanceQuery query);
	
	/**
	 * Get the all ServiceInstances in the ServiceDirectory including the DOWN ServiceInstance.
	 * 
	 * @return
	 * 		the ServiceInstance List.
	 * @throws ServiceException
	 */
	public List<ServiceInstance> getAllInstances();
	
	/**
	 * Add a NotificationHandler to the Service.
	 * 
	 * This method can check the duplicate NotificationHandler for the serviceName, if the NotificationHandler
	 * already exists in the serviceName, do nothing.
	 * 
	 * Throw IllegalArgumentException if serviceName or handler is null.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param handler
	 * 		the NotificationHandler for the service.
	 * @throws ServiceException
	 */
	public void addNotificationHandler(String serviceName, NotificationHandler handler);
	
	/**
	 * Remove the NotificationHandler from the Service.
	 * 
	 * Throw IllegalArgumentException if serviceName or handler is null.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param handler
	 * 		the NotificationHandler for the service.
	 * @throws ServiceException
	 */
	public void removeNotificationHandler(String serviceName, NotificationHandler handler) ;
}
