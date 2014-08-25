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
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceDirectoryError;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.exception.ServiceRuntimeException;
import com.cisco.oss.foundation.directory.lb.LoadBalancerManager;
import com.cisco.oss.foundation.directory.lb.MetadataQueryRRLoadBalancer;
import com.cisco.oss.foundation.directory.lb.ServiceQueryRRLoadBalancer;
import com.cisco.oss.foundation.directory.lb.ServiceRRLoadBalancer;
import com.cisco.oss.foundation.directory.lifecycle.Closable;
import com.cisco.oss.foundation.directory.query.QueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.ContainQueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.NotContainQueryCriterion;
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
		
		if(serviceName == null || serviceName.isEmpty()){
			throw new IllegalArgumentException("The serviceName argument is null or empty.");
		}
		
		try{
			ServiceRRLoadBalancer lb = lbManager.getServiceRRLoadBalancer(serviceName);
			return lb.vote();
		} catch(ServiceRuntimeException e){
			throw new ServiceException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ServiceInstance> lookupInstances(String serviceName)
			throws ServiceException {
		
		if(serviceName == null || serviceName.isEmpty()){
			throw new IllegalArgumentException("The serviceName argument is null or empty.");
		}
		
		try{
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
		} catch(ServiceRuntimeException e){
			throw new ServiceException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServiceInstance queryInstanceByName(String serviceName, ServiceInstanceQuery query)
			throws ServiceException {
		if(serviceName == null || serviceName.isEmpty()){
			throw new IllegalArgumentException("The serviceName argument is null or empty.");
		}
		
		if(query == null){
			throw new IllegalArgumentException("The ServiceInstanceQuery is null.");
		}
		
		try{
			ServiceQueryRRLoadBalancer lb = lbManager.getServiceQueryRRLoadBalancer(serviceName, query);
			return lb.vote();
		} catch(ServiceRuntimeException e){
			throw new ServiceException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ServiceInstance> queryInstancesByName(String serviceName, ServiceInstanceQuery query)
			throws ServiceException {
		
		if(serviceName == null || serviceName.isEmpty()){
			throw new IllegalArgumentException("The serviceName argument is null or empty.");
		}
		
		if(query == null){
			throw new IllegalArgumentException("The ServiceInstanceQuery is null.");
		}
		
		try{
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
		} catch(ServiceRuntimeException e){
			throw new ServiceException(e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServiceInstance queryInstanceByKey(ServiceInstanceQuery query)
			throws ServiceException {
		
		if(query == null){
			throw new IllegalArgumentException("The ServiceInstanceQuery is null.");
		}
		
		validateServiceInstanceQuery(query);
		
		try{
			MetadataQueryRRLoadBalancer lb = lbManager.getMetadataQueryRRLoadBalancer(query);
			return lb.vote();
		} catch(ServiceRuntimeException e){
			throw new ServiceException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ServiceInstance> queryInstancesByKey(ServiceInstanceQuery query)
			throws ServiceException {
		
		if(query == null){
			throw new IllegalArgumentException("The ServiceInstanceQuery is null.");
		}
		validateServiceInstanceQuery(query);
		try{
			List<ServiceInstance> instances = null;
			String keyName = null;
			if (query.getCriteria().size() > 0) {
				keyName = query.getCriteria().get(0).getMetadataKey();
			}
			if (keyName != null && !keyName.isEmpty()) {
				List<ModelServiceInstance> modelInstances = getLookupService()
						.getUPModelInstancesByKey(keyName);
				List<ModelServiceInstance> filteredInstances = ServiceInstanceQueryHelper
						.filter(query, modelInstances);
				if (filteredInstances.size() > 0) {
					if (instances == null) {
						instances = new ArrayList<ServiceInstance>();
					}
					for (ModelServiceInstance model : filteredInstances) {
						instances.add(ServiceInstanceUtils
								.transferFromModelServiceInstance(model));
					}
				}
			}

			if (instances != null) {
				return instances;
			} else {
				return Collections.emptyList();
			}
		} catch(ServiceRuntimeException e){
			throw new ServiceException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServiceInstance getInstance(String serviceName, String instanceId)
			throws ServiceException {
		if(instanceId == null || instanceId.isEmpty()){
			throw new IllegalArgumentException("The instanceId argument is null or empty.");
		}
		
		if(serviceName == null || serviceName.isEmpty()){
			throw new IllegalArgumentException("The serviceName argument is null or empty.");
		}
		
		try{
			ModelServiceInstance instance = getLookupService().getModelServiceInstance(serviceName, instanceId);
			if(instance != null){
				return ServiceInstanceUtils.transferFromModelServiceInstance(instance);
			}
		} catch(ServiceRuntimeException e){
			throw new ServiceException(e);
		}
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ServiceInstance> getAllInstances(String serviceName)
			throws ServiceException {
		
		
		if(serviceName == null || serviceName.isEmpty()){
			throw new IllegalArgumentException("The serviceName argument is null or empty.");
		}
		
		try{
			List<ModelServiceInstance> modelSvc = getLookupService().getModelInstances(serviceName);
			if(modelSvc == null || modelSvc.isEmpty()){
				return Collections.emptyList();
			}else{
				List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
				for(ModelServiceInstance modelInstance : modelSvc){
					instances.add(ServiceInstanceUtils.transferFromModelServiceInstance(modelInstance));
				}
				return instances;
			}
		} catch(ServiceRuntimeException e){
			throw new ServiceException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ServiceInstance> getAllInstances(String serviceName,
			ServiceInstanceQuery query) throws ServiceException {
		
		if(query == null){
			throw new IllegalArgumentException("The ServiceInstanceQuery argument is null.");
		}
		
		if(serviceName == null || serviceName.isEmpty()){
			throw new IllegalArgumentException("The serviceName argument is null or empty.");
		}
		
		try{
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
		} catch(ServiceRuntimeException e){
			throw new ServiceException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ServiceInstance> getAllInstancesByKey(ServiceInstanceQuery query)
			throws ServiceException {
		if(query == null){
			throw new IllegalArgumentException("The ServiceInstanceQuery argument is null.");
		}
		validateServiceInstanceQuery(query);
		try{
			List<ServiceInstance> instances = null;
			String keyName = null;
			if (query.getCriteria().size() > 0) {
				keyName = query.getCriteria().get(0).getMetadataKey();
			}
			if (keyName != null && !keyName.isEmpty()) {
				List<ModelServiceInstance> modelInstances = getLookupService()
						.getModelInstancesByKey(keyName);
				List<ModelServiceInstance> filteredInstances = ServiceInstanceQueryHelper
						.filter(query, modelInstances);
				if (filteredInstances.size() > 0) {
					if (instances == null) {
						instances = new ArrayList<ServiceInstance>();
					}
					for (ModelServiceInstance model : filteredInstances) {
						instances.add(ServiceInstanceUtils
								.transferFromModelServiceInstance(model));
					}
				}
			}

			if (instances != null) {
				return instances;
			} else {
				return Collections.emptyList();
			}
		} catch(ServiceRuntimeException e){
			throw new ServiceException(e);
		}
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
	
	/**
	 * Validate the ServiceInstanceQuery for the queryInstanceByKey, queryInstancesByKey 
	 * and getAllInstancesByKey method.
	 * 
	 * For those methods, the ContainQueryCriterion and NotContainQueryCriterion are not supported.
	 * 
	 * @param query
	 * 		the ServiceInstanceQuery to validate.
	 * @throws ServiceException
	 * 		when the ServiceInstanceQuery has ContainQueryCriterion or NotContainQueryCriterion.
	 */
	private void validateServiceInstanceQuery(ServiceInstanceQuery query) throws ServiceException{
		for(QueryCriterion criterion : query.getCriteria()){
			if(criterion instanceof ContainQueryCriterion || criterion instanceof NotContainQueryCriterion){
				ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.QUERY_CRITERION_ILLEGAL_IN_QUERY);
				throw new ServiceException(error);
			}
		}
	}
}
