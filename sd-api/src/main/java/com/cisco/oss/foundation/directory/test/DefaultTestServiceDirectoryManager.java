/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceInstanceHealth;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.exception.DirectoryServerClientException;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceDirectoryError;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.exception.ServiceRuntimeException;
import com.cisco.oss.foundation.directory.impl.ServiceDirectoryCache;
import com.cisco.oss.foundation.directory.impl.ServiceInstanceQueryHelper;
import com.cisco.oss.foundation.directory.lifecycle.Closable;
import com.cisco.oss.foundation.directory.query.QueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;
import com.cisco.oss.foundation.directory.utils.ServiceInstanceUtils;

/**
 * The default Directory API RegistrationManager, LookupManager implementation for Test.
 * 
 * It implements the RegistrationManager and LookupManager interfaces to operate the ServiceInstance
 * at client side and does not invoke a remote ServiceDirectory server node, all ServiceInstances 
 * are stored in the local memory.
 * 
 *  
 * @author zuxiang
 *
 */
public class DefaultTestServiceDirectoryManager implements
		RegistrationManager, LookupManager, Closable {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DefaultTestServiceDirectoryManager.class);
	
	/**
	 * The position index for query instance by service name.
	 */
	private Map<String, AtomicInteger> queryInstanceIdx;
	
	/**
	 * The internal Cache for ProvidedService.
	 */
	private ServiceDirectoryCache<String, ProvidedService> cache;
	
	/**
	 * The internal Cache for MetadataKey.
	 */
	private ServiceDirectoryCache<String, List<ProvidedServiceInstance>> metadataKeyCache;
	
	/**
	 * Mark whether component is started or not.
	 */
	protected boolean isStarted = false;
	
	/**
	 * Constructor
	 */
	public DefaultTestServiceDirectoryManager(){
		cache = new ServiceDirectoryCache<String, ProvidedService>();
		metadataKeyCache = new ServiceDirectoryCache<String, List<ProvidedServiceInstance>>();
		this.queryInstanceIdx = new HashMap<String, AtomicInteger>();
	}

	/**
	 * Start the DefaultTestServiceDirectoryManager.
	 */
	@Override
	public void start() {
		if(! isStarted){
			isStarted = true;
		}
	}

	/**
	 * Stop the DefaultTestServiceDirectoryManager.
	 */
	@Override
	public void stop() {
		if(isStarted){
			cache.refresh();
			isStarted = false;
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @return
	 * 		the ServiceInstance.
	 * @throws ServiceException
	 */
	@Override
	public ServiceInstance lookupInstance(String serviceName)
			throws ServiceException {
		List<ServiceInstance> list = this.lookupInstances(serviceName);
		if(list == null || list.size() == 0){
			return null;
		}
		int index = getNextQueryInstanceIndex(serviceName, list.size());
		return list.get(index);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @return
	 * 		the ServiceInstance list.
	 * @throws ServiceException
	 */
	@Override
	public List<ServiceInstance> lookupInstances(String serviceName)
			throws ServiceException {
		
		ProvidedService service = null;
		if(cache.isCached(serviceName)){
			service = cache.getService(serviceName);
		}
		
		if(service == null || service.getServiceInstances().size() == 0){
			return Collections.emptyList();
		}else{
			List<ServiceInstance> list = new ArrayList<ServiceInstance>();
			for(ProvidedServiceInstance model : service.getServiceInstances()){
				if(model.getStatus() == OperationalStatus.UP){
					list.add(new ServiceInstance(serviceName, model.getProviderId(), model.getUri(), 
							model.isMonitorEnabled(), model.getStatus(), model.getMetadata()));
				}
			}
			return list;
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param query
	 * 		the ServiceInstanceQuery.
	 * @return
	 * 		the ServiceInstance.
	 * @throws ServiceException
	 */
	@Override
	public ServiceInstance queryInstanceByName(String serviceName, ServiceInstanceQuery query)
			throws ServiceException {
		List<ServiceInstance> list = this.queryInstancesByName(serviceName, query);
		if(list == null || list.size() == 0){
			return null;
		}
		int index = getNextQueryInstanceIndex(serviceName, list.size());
		return list.get(index);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param query
	 * 		the ServiceInstanceQuery.
	 * @return
	 * 		the ServiceInstance list match the ServiceInstanceQuery.
	 * @throws ServiceException
	 */
	@Override
	public List<ServiceInstance> queryInstancesByName(String serviceName, ServiceInstanceQuery query)
			throws ServiceException {
		List<ServiceInstance> instances = this.lookupInstances(serviceName);
		return ServiceInstanceQueryHelper.filterServiceInstance(query, instances);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param serviceInstance
	 * 		the ProvidedServiceInstance.
	 * @throws ServiceException
	 */
	@Override
	public void registerService(ProvidedServiceInstance serviceInstance)
			throws ServiceException {
		if(serviceInstance == null){
			throw new IllegalArgumentException("The ServiceInstance can not be null.");
		}
		
		ErrorCode code = ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceInstance.getServiceName());
			throw new ServiceException(error);
		}
		
		ProvidedServiceInstance cachedInstance = this.getProvidedServiceInstance(serviceInstance.getServiceName(), serviceInstance.getProviderId());
		if(cachedInstance != null){
			ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_INSTANCE_ALREADY_EXIST, serviceInstance.getServiceName());
			throw new ServiceException(error);
		}
		String serviceName = serviceInstance.getServiceName();
		
		ProvidedService service = this.createServiceIfNotExists(serviceName);
		
		ProvidedServiceInstance cacheServiceInstance = deepCloneProvidedServiceInstance(serviceInstance);
		service.getServiceInstances().add(cacheServiceInstance);
		addMetadataKeyMap(serviceInstance);
		LOGGER.info("Registered Service, name=" + serviceInstance.getServiceName());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param serviceInstance
	 * 		the ProvidedServiceInstance.
	 * @param status
	 * 		the OperationalStatus of the ProvidedServiceInstance.
	 * @throws ServiceException
	 */
	@Override
	public void registerService(ProvidedServiceInstance serviceInstance,
			OperationalStatus status) throws ServiceException {
		serviceInstance.setStatus(status);
		registerService(serviceInstance);
		
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param serviceInstance
	 * 		the ProvidedServiceInstance.
	 * @param status
	 * 		the OperationalStatus of ProvidedServiceInstance.
	 * @param registryHealth
	 * 		the ServiceInstanceHealth callback of the ServiceInstance.
	 * @throws ServiceException
	 */
	@Override
	public void registerService(ProvidedServiceInstance serviceInstance,
			OperationalStatus status, ServiceInstanceHealth registryHealth)
			throws ServiceException {
		serviceInstance.setStatus(status);
		registerService(serviceInstance);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param providerId
	 * 		the providerId.
	 * @param status
	 * 		the OperatonalStatus of the ProvidedServiceInstance.
	 * @throws ServiceException
	 */
	@Override
	public void updateServiceOperationalStatus(String serviceName,
			String providerId, OperationalStatus status)
			throws ServiceException {
		
		ErrorCode code = ServiceInstanceUtils.isNameValid(serviceName);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceName);
			throw new ServiceException(error);
		}
		
		code = ServiceInstanceUtils.isIdValid(providerId);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceName);
			throw new ServiceException(error);
		}
		
				
		ProvidedServiceInstance model = getProvidedServiceInstance(serviceName, providerId);
		if(model == null){
			LOGGER.error("Update Service Instance OperationalStatus failed - can not find the ServiceInstance.");
			ServiceDirectoryError sde = new ServiceDirectoryError(
					ErrorCode.SERVICE_INSTANCE_NOT_EXIST,
					serviceName);
			throw new DirectoryServerClientException(sde);
		}
		model.setStatus(status);
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateServiceUri(String serviceName,
			String providerId, String uri) throws ServiceException {
		ErrorCode code = ServiceInstanceUtils.isNameValid(serviceName);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceName);
			throw new ServiceException(error);
		}
		
		code = ServiceInstanceUtils.isIdValid(providerId);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceName);
			throw new ServiceException(error);
		}
		
		code = ServiceInstanceUtils.isUriValid(uri);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceName);
			throw new ServiceException(error);
		}
		
		
		ProvidedServiceInstance model = getProvidedServiceInstance(serviceName, providerId);
		if(model == null){
			LOGGER.error("Update Service Instance URI failed - can not find the ServiceInstance.");
			ServiceDirectoryError sde = new ServiceDirectoryError(
					ErrorCode.SERVICE_INSTANCE_NOT_EXIST,
					serviceName);
			throw new DirectoryServerClientException(sde);
		}
		model.setUri(uri);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param serviceInstance
	 * 		the ProvidedServiceInstance.
	 * @throws ServiceException
	 */
	@Override
	public void updateService(ProvidedServiceInstance serviceInstance)
			throws ServiceException {
		if(serviceInstance == null){
			throw new IllegalArgumentException("The ServiceInstance can not be null.");
		}
		
		ErrorCode code = ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceInstance.getServiceName());
			throw new ServiceException(error);
		}
		
		updateMetadataKeyMap(serviceInstance);
		try{
			updateInstance(serviceInstance);
		} catch(ServiceRuntimeException e){
			throw new ServiceException(e);
		}
		
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param providerId
	 * 		the providerId
	 * @throws ServiceException
	 */
	@Override
	public void unregisterService(String serviceName, String providerId)
			throws ServiceException {
		ErrorCode code = ServiceInstanceUtils.isNameValid(serviceName);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceName);
			throw new ServiceException(error);
		}
		
		code = ServiceInstanceUtils.isIdValid(providerId);
		if(! code.equals(ErrorCode.OK)){
			ServiceDirectoryError error = new ServiceDirectoryError(code, serviceName);
			throw new ServiceException(error);
		}
		
		deleteMetadataKeyMap(serviceName, providerId);
		ProvidedServiceInstance model = getProvidedServiceInstance(serviceName, providerId);
		if(model == null){
			LOGGER.error("Unregister Service failed - can not find the ServiceInstance.");
			ServiceDirectoryError sde = new ServiceDirectoryError(
					ErrorCode.SERVICE_INSTANCE_NOT_EXIST,
					serviceName);
			throw new DirectoryServerClientException(sde);
		}
		
		getService(serviceName).getServiceInstances().remove(model);
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServiceInstance queryInstanceByKey(ServiceInstanceQuery query)
			throws ServiceException {
		List<QueryCriterion> criteria = query.getCriteria();
		if (criteria != null && criteria.size() > 0) {

			String key = criteria.get(0).getMetadataKey();
			List<ServiceInstance> list = this.queryInstancesByKey(query);
			if(list == null || list.size() == 0){
				return null;
			}
			int index = getNextQueryInstanceIndex(key, list.size());
			return list.get(index);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ServiceInstance> queryInstancesByKey(
			ServiceInstanceQuery query) throws ServiceException {
		List<QueryCriterion> criteria = query.getCriteria();
		if (criteria != null && criteria.size() > 0) {

			String key = criteria.get(0).getMetadataKey();
			if (metadataKeyCache.isCached(key)) {
				List<ProvidedServiceInstance> instances = metadataKeyCache
						.getService(key);
				List<ServiceInstance> list = new ArrayList<ServiceInstance>();
				for (ProvidedServiceInstance model : instances) {
					if (model.getStatus() == OperationalStatus.UP) {
						list.add(new ServiceInstance(model.getServiceName(),
								model.getProviderId(), model.getUri(), model.isMonitorEnabled(),
								model.getStatus(), model.getMetadata()));
					}
				}

				return ServiceInstanceQueryHelper.filterServiceInstance(query,
						list);
			}
		}
		
		return Collections.emptyList();
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServiceInstance getInstance(String serviceName, String instanceId)
			throws ServiceException {
		ProvidedServiceInstance instance = getProvidedServiceInstance(serviceName, instanceId);
		return new ServiceInstance(serviceName, instance.getProviderId(), instance.getUri(), 
				instance.isMonitorEnabled(), instance.getStatus(), instance.getMetadata());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ServiceInstance> getAllInstances(String serviceName)
			throws ServiceException {
		ProvidedService service = null;
		if(cache.isCached(serviceName)){
			service = cache.getService(serviceName);
		}
		
		if(service == null || service.getServiceInstances().size() == 0){
			return Collections.emptyList();
		}else{
			List<ServiceInstance> list = new ArrayList<ServiceInstance>();
			for(ProvidedServiceInstance model : service.getServiceInstances()){
				list.add(new ServiceInstance(serviceName, model.getProviderId(), model.getUri(), 
						model.isMonitorEnabled(), model.getStatus(), model.getMetadata()));
			}
			return list;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ServiceInstance> getAllInstances(String serviceName,
			ServiceInstanceQuery query) throws ServiceException {
		List<ServiceInstance> instances = this.getAllInstances(serviceName);
		return ServiceInstanceQueryHelper.filterServiceInstance(query, instances);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ServiceInstance> getAllInstancesByKey(ServiceInstanceQuery query)
			throws ServiceException {
		List<QueryCriterion> criteria = query.getCriteria();
		if (criteria != null && criteria.size() > 0) {

			String key = criteria.get(0).getMetadataKey();
			if (metadataKeyCache.isCached(key)) {
				List<ProvidedServiceInstance> instances = metadataKeyCache
						.getService(key);
				List<ServiceInstance> list = new ArrayList<ServiceInstance>();
				for (ProvidedServiceInstance model : instances) {
					list.add(new ServiceInstance(model.getServiceName(),
							model.getProviderId(), model.getUri(),
							model.isMonitorEnabled(), model.getStatus(), model.getMetadata()));
				}

				return ServiceInstanceQueryHelper.filterServiceInstance(query,
						list);
			}
		}
		
		return Collections.emptyList();
	}
	
	/**
	 * Get the ServiceDirectoryCache.
	 * 
	 * @return
	 * 		the ServiceDirectoryCache.
	 */
	protected ServiceDirectoryCache<String, ProvidedService> getServiceDirectoryCache(){
		return this.cache;
	}
	
	/**
	 * Get the ProvidedServiceInstance from cache.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param instanceId
	 * 		the instanceId.
	 * @return
	 * 		the ProvidedServiceInstance.
	 */
	private ProvidedServiceInstance getProvidedServiceInstance(String serviceName, String instanceId){
		if(this.cache.isCached(serviceName)){
			List<ProvidedServiceInstance> instances = cache.getService(serviceName).getServiceInstances();
			for(ProvidedServiceInstance ist : instances){
				if(ist.getProviderId().equals(instanceId)){
					return ist;
				}
			}
		}
		return null;
	}
	
	/**
	 * Validate the ProvidedServiceInstance.
	 * 
	 * @param instance
	 * 		the ProvidedServiceInstance.
	 */
	private void validateProvidedServiceInstance(ProvidedServiceInstance instance){
		if(instance.getUri() == null || instance.getUri().isEmpty()){
			ServiceDirectoryError sde = new ServiceDirectoryError(
					ErrorCode.SERVICE_INSTANCE_URI_FORMAT_ERROR,
					instance.getServiceName());
			throw new DirectoryServerClientException(sde);
		}
	}
	
	/**
	 * Obtain a ProvidedService, and create a new one if not existed.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @return
	 * 		the ProvidedService.
	 */
	private ProvidedService createServiceIfNotExists(String serviceName){
		if(cache.isCached(serviceName)){
			return cache.getService(serviceName);
		}else{
			ProvidedService service = new ProvidedService(serviceName);
			List<ProvidedServiceInstance> ins = new ArrayList<ProvidedServiceInstance>();
			service.setServiceInstances(ins);
			cache.putService(serviceName, service);
			return service;
		}
	}
	
	/**
	 * Get the ProvidedService with its instances.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @return
	 * 		the ProvidedService.
	 */
	private ProvidedService getService(String serviceName){
		if(cache.isCached(serviceName)){
			return cache.getService(serviceName);
		}
		return null;
	}
	
	/**
	 * Update the ServiceInstance.
	 * 
	 * @param instance
	 * 		the ProvidedServiceInstance.
	 * @throws ServiceException
	 */
	private void updateInstance(ProvidedServiceInstance instance)
			throws ServiceException {
		
		
		validateProvidedServiceInstance(instance);
		
		String serviceName = instance.getServiceName();
		String instanceId = instance.getProviderId();
		
		ProvidedServiceInstance model = getProvidedServiceInstance(serviceName, instanceId);
		if(model == null){
			LOGGER.error("Update Service failed - cannot find the ServiceInstance");
			ServiceDirectoryError sde = new ServiceDirectoryError(
					ErrorCode.SERVICE_INSTANCE_NOT_EXIST,
					instance.getServiceName());
			throw new DirectoryServerClientException(sde);
		}
		
		model.setMonitorEnabled(instance.isMonitorEnabled());
		model.setStatus(instance.getStatus());
		model.setUri(instance.getUri());
		
		// Change the metadata when metadata is not null.
		if(instance.getMetadata() != null){
			Map<String, String> modelMetadata = new HashMap<String, String>();
			model.setMetadata(modelMetadata);
			
			for(Entry<String, String> entry : instance.getMetadata().entrySet()){
				modelMetadata.put(entry.getKey(), entry.getValue());
			}
		}
		
	}

	/**
	 * Update the ProvidedServiceInstance OperationalStatus.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param instanceId
	 * 		the instanceId
	 * @param status
	 * 		the OperationalStatus of the ServiceInstance.
	 * @throws ServiceException
	 */
	protected void updateInstanceStatus(String serviceName, String instanceId,
			OperationalStatus status) throws ServiceException {
		
		ProvidedServiceInstance model = getProvidedServiceInstance(serviceName, instanceId);
		if(model == null){
			LOGGER.error("Update Service OperationalStatus failed - cannot find the ServiceInstance");
			ServiceDirectoryError sde = new ServiceDirectoryError(
					ErrorCode.SERVICE_INSTANCE_NOT_EXIST,
					serviceName);
			throw new DirectoryServerClientException(sde);
		}
		
		model.setStatus(status);
	}
	
	/**
	 * Add the ProvidedServiceInstance to metadata key map.
	 * 
	 * @param serviceInstance
	 * 		the ProvidedServiceInstance.
	 */
	private void addMetadataKeyMap(ProvidedServiceInstance serviceInstance){
		String serviceName = serviceInstance.getServiceName();
		String instanceId = serviceInstance.getProviderId();
		
		ProvidedServiceInstance cachedInstance = this.getProvidedServiceInstance(serviceName, instanceId);
		if(cachedInstance == null){
			return;
		}
		
		for(Entry<String, String> en : serviceInstance.getMetadata().entrySet()){
			String key = en.getKey();
			if(! this.metadataKeyCache.isCached(key)){
				List<ProvidedServiceInstance> instances = new ArrayList<ProvidedServiceInstance>();
				this.metadataKeyCache.putService(key, instances);
			}
			this.metadataKeyCache.getService(key).add(cachedInstance);
		}
	}
	
	/**
	 * Update the metadata key for the new ProvidedServiceInstance.
	 * 
	 * It removes the metadata key map from the old ProvidedServiceInstance, and adds metadata key 
	 * to the new ProvidedServiceInstance.
	 * 
	 * @param serviceInstance
	 * 		the ProvidedServiceInstance.
	 */
	private void updateMetadataKeyMap(ProvidedServiceInstance serviceInstance){
		// Remove old mapping.
		deleteMetadataKeyMap(serviceInstance.getServiceName(), serviceInstance.getProviderId());
		addMetadataKeyMap(serviceInstance);
	}
	
	/**
	 * Delete the metadatakey for the ProvidedServiceInstance.
	 * 
	 * @param serviceName
	 * 		the serviceName of ProvidedServiceInstance.
	 * @param providerId
	 * 		the providerId of ProvidedServiceInstance.
	 */
	private void deleteMetadataKeyMap(String serviceName, String providerId){
		
		ProvidedServiceInstance cachedInstance = this.getProvidedServiceInstance(serviceName, providerId);
		if(cachedInstance == null){
			return ;
		}
		for(Entry<String, String> en : cachedInstance.getMetadata().entrySet()){
			String key = en.getKey();
			if(! this.metadataKeyCache.isCached(key)){
				List<ProvidedServiceInstance> instances = new ArrayList<ProvidedServiceInstance>();
				this.metadataKeyCache.putService(key, instances);
			}
			this.metadataKeyCache.getService(key).remove(cachedInstance);
		}
	}
	
	/**
	 * Get the next index of ServiceInstance List in the queryList.
	 * 
	 * @param serviceName	the name of the service.
	 * @param max			The max index in the ServiceInstance List, it is size()-1.
	 * @return
	 * 		the next ServiceInstance index in query.
	 */
	private int getNextQueryInstanceIndex(String serviceName, int max){
		if(max == 0){
			return 0;
		}
		
		if(this.queryInstanceIdx.containsKey(serviceName)){
			AtomicInteger pre = this.queryInstanceIdx.get(serviceName);
			int theIndex = pre.incrementAndGet();
			return theIndex % max;
		}else{
			this.queryInstanceIdx.put(serviceName, new AtomicInteger(0));
			return 0;
		}
	}
	
	/**
	 * Deep clone a ProvidedServiceInstance.
	 * 
	 * It can clone all object reference and the Collections.
	 * 
	 * @param instance
	 * 		the ProvidedServiceInstance.
	 * @return
	 * 		the new ProvidedServiceInstance cloned.
	 */
	private ProvidedServiceInstance deepCloneProvidedServiceInstance(ProvidedServiceInstance instance){
		ProvidedServiceInstance newInstance = new ProvidedServiceInstance(
				instance.getServiceName(), instance.getAddress(), instance.getPort());
		newInstance.setStatus(instance.getStatus());
		newInstance.setMonitorEnabled(instance.isMonitorEnabled());
		newInstance.setUri(instance.getUri());
		
		Map<String, String> meta = new HashMap<String, String>();
		for(Entry<String, String> entry : instance.getMetadata().entrySet()){
			meta.put(entry.getKey(), entry.getValue());
		}
		newInstance.setMetadata(meta);
		return newInstance;
		
	}
	
	/**
	 * The Service Object in Service Directory test framework.
	 * 
	 * It is the logic Service Object in Service Directory. The ProvidedServiceInstances with the same service 
	 * name belong to one ProvidedService.
	 * It is only used in the test framework.
	 * 
	 * @author zuxiang
	 *
	 */
	public static class ProvidedService {

		/**
		 * The name.
		 */
		private String name;
		
		/**
		 * The ServiceInstances of the Service.
		 */
		private List<ProvidedServiceInstance> serviceInstances;
		
		/**
		 * Constructor.
		 */
		public ProvidedService(){
			
		}
		
		/**
		 * Constructor.
		 * 
		 * @param name
		 * 		the name.
		 */
		public ProvidedService(String name){
			this.name = name;
		}
		
		/**
		 * Get service name.
		 * 
		 * @return
		 * 		the service name.
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Set service name.
		 * 
		 * @param name
		 * 		the service name.
		 */
		public void setName(String name) {
			this.name = name;
		}
		
		/**
		 * Get the ProvidedServiceInstance list.
		 * 
		 * @return
		 * 		the ProviidedServiceInstance list.
		 */
		public List<ProvidedServiceInstance> getServiceInstances() {
			return serviceInstances;
		}
		
		/**
		 * Set the ProviidedServiceInstance list.
		 * @param serviceInstances
		 * 		the ProviidedServiceInstance list.
		 */
		public void setServiceInstances(List<ProvidedServiceInstance> serviceInstances) {
			this.serviceInstances = serviceInstances;
		}
	}
}
