/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.Configurations;
import com.cisco.oss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.NotificationHandler;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceDirectoryError;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.lb.LoadBalancerManager;
import com.cisco.oss.foundation.directory.lb.MetadataQueryRRLoadBalancer;
import com.cisco.oss.foundation.directory.lb.ServiceQueryRRLoadBalancer;
import com.cisco.oss.foundation.directory.lb.ServiceRRLoadBalancer;
import com.cisco.oss.foundation.directory.lifecycle.Closable;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;
import com.cisco.oss.foundation.directory.utils.ServiceInstanceUtils;

/**
 * The default LookupManager implementation to access the remote ServiceDirectory
 * node.
 * 
 * @author zuxiang
 *
 */
public class LookupManagerImpl implements LookupManager, Closable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LookupManagerImpl.class);

	/**
	 * The LookupManager cache enabled property.
	 */
	public static final String SD_API_CACHE_ENABLED_PROPERTY = "cache.enabled";
	
	/**
	 * The default cache enabled property value.
	 */
	public static final boolean SD_API_CACHE_ENABLED_DEFAULT = true;
	
	/**
	 * The remote ServiceDirectory node client.
	 */
	private final DirectoryServiceClientManager directoryServiceClientManager ;
	
	/**
	 * The loadbalancer map for Services.
	 */
	private final LoadBalancerManager lbManager;
	
	/**
	 * The LookupService.
	 */
	private DirectoryLookupService lookupService;
	
	/**
	 * Mark component started or not
	 */
	private boolean isStarted=false;
	
	/**
	 * Constructor.
	 */
	public LookupManagerImpl(DirectoryServiceClientManager directoryServiceClientManager){
		this.directoryServiceClientManager = directoryServiceClientManager;
		this.lbManager = new LoadBalancerManager(getLookupService());
	}
	
	/**
	 * Start the LookupManagerImpl.
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
	 * Stop the LookupManagerImpl
	 * 
	 * it is idempotent, it can be invoked multiple times while in same state and is not thread safe.
	 */
	@Override
	public void stop(){
		if(isStarted){
			synchronized (this) {
				if (isStarted) {
					if (getLookupService() instanceof Closable) {
						((Closable) getLookupService()).stop();
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
	public ServiceInstance lookupInstance(String serviceName)
			throws ServiceException {
		if(! isStarted){
			ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
			throw new ServiceException(error);
		}
		
		ServiceRRLoadBalancer lb = lbManager.getServiceRRLoadBalancer(serviceName);
		return lb.vote();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ServiceInstance> lookupInstances(String serviceName)
			throws ServiceException {
		if(! isStarted){
			ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
			throw new ServiceException(error);
		}
		
		List<ModelServiceInstance> modelSvc = getLookupService().getUPModelInstances(serviceName);
		if(modelSvc == null || modelSvc.isEmpty()){
			return Collections.emptyList();
		}else{
			List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
			for(ModelServiceInstance modelInstance : modelSvc){
				instances.add(ServiceInstanceUtils.transferFromModelServiceInstance(modelInstance));
			}
			return instances;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServiceInstance queryInstanceByName(String serviceName, ServiceInstanceQuery query)
			throws ServiceException {
		if(! isStarted){
			ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
			throw new ServiceException(error);
		}
		
		ServiceQueryRRLoadBalancer lb = lbManager.getServiceQueryRRLoadBalancer(serviceName, query);
		return lb.vote();
			
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ServiceInstance> queryInstancesByName(String serviceName, ServiceInstanceQuery query)
			throws ServiceException {
		if(! isStarted){
			ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
			throw new ServiceException(error);
		}
		
		List<ModelServiceInstance> modelSvc = getLookupService().getUPModelInstances(serviceName);
		if(modelSvc != null && ! modelSvc.isEmpty()){
			List<ModelServiceInstance> filteredInstances = ServiceInstanceQueryHelper.filter(query, modelSvc);
			if(filteredInstances.size() > 0){
				List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
				for(ModelServiceInstance model : filteredInstances){
					instances.add(ServiceInstanceUtils.transferFromModelServiceInstance(model));
				}
				return instances;
			}
		}
		return Collections.emptyList();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServiceInstance queryInstanceByKey(ServiceInstanceQuery query)
			throws ServiceException {
		if(! isStarted){
			ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
			throw new ServiceException(error);
		}
//		validateServiceInstanceQuery(query);
		
		if(query == null ){
			throw new IllegalArgumentException("ServiceInstanceQuery cannot be null.");
		}
		
		if(query.getCriteria() == null || query.getCriteria().size() == 0){
			return null;
		}
		
		MetadataQueryRRLoadBalancer lb = lbManager.getMetadataQueryRRLoadBalancer(query);
		return lb.vote();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ServiceInstance> queryInstancesByKey(ServiceInstanceQuery query)
			throws ServiceException {
		if(! isStarted){
			ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
			throw new ServiceException(error);
		}
//		validateServiceInstanceQuery(query);
		if(query == null ){
			throw new IllegalArgumentException("ServiceInstanceQuery cannot be null.");
		}
		
		if(query.getCriteria() == null || query.getCriteria().size() == 0){
			return Collections.emptyList();
		}
			
		List<ServiceInstance> instances = null;
		
		List<ModelServiceInstance> modelInstances = getLookupService().queryUPModelInstances(query);
		if(modelInstances != null && modelInstances.size()> 0){
			instances = new ArrayList<ServiceInstance>();
			for(ModelServiceInstance model : modelInstances){
				instances.add(ServiceInstanceUtils
						.transferFromModelServiceInstance(model));
			}
		}

		if (instances != null) {
			return instances;
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServiceInstance getInstance(String serviceName, String instanceId)
			throws ServiceException {
		if(! isStarted){
			ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
			throw new ServiceException(error);
		}
		if(instanceId == null || instanceId.isEmpty()){
			return null;
		}
		
		ModelService service = getLookupService().getModelService(serviceName);
		if(service != null && service.getServiceInstances() != null 
				&& service.getServiceInstances().size() > 0){
			for(ModelServiceInstance instance : service.getServiceInstances()){
				if(instance.getInstanceId().equals(instanceId)){
					return ServiceInstanceUtils.transferFromModelServiceInstance(instance);
				}
			}
				
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ServiceInstance> getAllInstances(String serviceName)
			throws ServiceException {
		if(! isStarted){
			ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
			throw new ServiceException(error);
		}
		List<ModelServiceInstance> modelSvc = getLookupService()
				.getModelInstances(serviceName);
		if (modelSvc == null || modelSvc.isEmpty()) {
			return Collections.emptyList();
		} else {
			List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
			for (ModelServiceInstance modelInstance : modelSvc) {
				instances.add(ServiceInstanceUtils
						.transferFromModelServiceInstance(modelInstance));
			}
			return instances;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ServiceInstance> getAllInstances(String serviceName,
			ServiceInstanceQuery query) throws ServiceException {
		if(! isStarted){
			ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
			throw new ServiceException(error);
		}
		
		List<ModelServiceInstance> modelSvc = getLookupService().getModelInstances(serviceName);
		if(modelSvc != null && ! modelSvc.isEmpty()){
			List<ModelServiceInstance> filteredInstances = ServiceInstanceQueryHelper.filter(query, modelSvc);
			if(filteredInstances.size() > 0){
				List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
				for(ModelServiceInstance model : filteredInstances){
					instances.add(ServiceInstanceUtils.transferFromModelServiceInstance(model));
				}
				return instances;
			}
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ServiceInstance> getAllInstancesByKey(ServiceInstanceQuery query)
			throws ServiceException {
		if(! isStarted){
			ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
			throw new ServiceException(error);
		}
//		validateServiceInstanceQuery(query);
		if(query == null ){
			throw new IllegalArgumentException("ServiceInstanceQuery cannot be null.");
		}
		
		if(query.getCriteria() == null || query.getCriteria().size() == 0){
			return Collections.emptyList();
		}
				
		List<ServiceInstance> instances = null;
		
		List<ModelServiceInstance> modelInstances = getLookupService().queryModelInstances(query);
		if(modelInstances != null && modelInstances.size()> 0){
			instances = new ArrayList<ServiceInstance>();
			for(ModelServiceInstance model : modelInstances){
				instances.add(ServiceInstanceUtils
						.transferFromModelServiceInstance(model));
			}
		}

		if (instances != null) {
			return instances;
		} else {
			return Collections.emptyList();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ServiceInstance> getAllInstances() throws ServiceException {
		if(! isStarted){
			ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
			throw new ServiceException(error);
		}
		
		List<ServiceInstance> instances = null;
		
		List<ModelServiceInstance> allInstances = getLookupService().getAllInstances();
		for(ModelServiceInstance serviceInstance : allInstances){
			if(instances == null){
				instances = new ArrayList<ServiceInstance>();
			}
			instances.add(ServiceInstanceUtils.transferFromModelServiceInstance(serviceInstance));
		}
		
		if(instances == null){
			return Collections.emptyList();
		}
		
		return instances;
	}
	
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
	 */
	@Override
	public void addNotificationHandler(String serviceName, NotificationHandler handler) throws ServiceException {
		
		if(! isStarted){
			ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
			throw new ServiceException(error);
		}
		
		if(handler == null || serviceName == null || serviceName.isEmpty()){
			throw new IllegalArgumentException();
		}
		
		ModelService service = getLookupService().getModelService(serviceName);
		if(service == null){
			ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_NOT_EXIST);
			throw new ServiceException(error); 
		}
		getLookupService().addNotificationHandler(serviceName, handler);
	}
	
	/**
	 * Remove the NotificationHandler from the Service.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param handler
	 * 		the NotificationHandler for the service.
	 */
	@Override
	public void removeNotificationHandler(String serviceName, NotificationHandler handler) throws ServiceException {
		
		if(! isStarted){
			ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
			throw new ServiceException(error);
		}
		
		if(handler == null || serviceName == null || serviceName.isEmpty()){
			throw new IllegalArgumentException();
		}
		
		getLookupService().removeNotificationHandler(serviceName, handler);
	}
	
	/**
	 * Get the DirectoryLookupService to do the lookup.
	 * 
	 * It is thread safe and lazy initialized.
	 * 
	 * @return
	 * 		the LookupService.
	 */
	private DirectoryLookupService getLookupService(){
		if(lookupService == null){
			synchronized(this){
				if(lookupService == null){
					boolean cacheEnabled = Configurations.getBoolean(SD_API_CACHE_ENABLED_PROPERTY,
							SD_API_CACHE_ENABLED_DEFAULT);
					if(cacheEnabled){
						CachedDirectoryLookupService service = new CachedDirectoryLookupService(directoryServiceClientManager);
						service.start();
						lookupService = service;
						LOGGER.info("Created the CachedDirectoryLookupService in LookupManager");
					} else {
						lookupService = new DirectoryLookupService(directoryServiceClientManager);
						LOGGER.info("Created the DirectoryLookupService in LookupManager");
					}
				}
			}
		}
		return lookupService;
	}
}
