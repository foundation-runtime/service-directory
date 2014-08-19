/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.vss.foundation.directory.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cisco.vss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.vss.foundation.directory.NotificationHandler;
import com.cisco.vss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.vss.foundation.directory.entity.ModelService;
import com.cisco.vss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.vss.foundation.directory.entity.OperationalStatus;

/**
 * It is the Directory LookupService to perform the lookup functionality.
 * 
 * It looks up ServiceInstance from DirectoryServer. 
 * 
 * @author zuxiang
 *
 */
public class DirectoryLookupService {
	
	/**
	 * The DirectoryServiceClientManager to get the DirectoryServiceClient.
	 */
	private DirectoryServiceClientManager directoryServiceClientManager = null;
	
	/**
	 * Constructor.
	 * 
	 * @param directoryServiceClientManager
	 * 		the DirectoryServiceClientManager.
	 */
	public DirectoryLookupService(DirectoryServiceClientManager directoryServiceClientManager){
		this.directoryServiceClientManager = directoryServiceClientManager;
	}

	/**
	 * Get the ModelService.
	 * 
	 * @param serviceName
	 * 		the Service name.
	 * @return
	 * 		the ModelService.
	 */
	protected ModelService getModelService(String serviceName){
		ModelService service = getDirectoryServiceClient().lookupService(serviceName);
		return service;
	}
	
	/**
	 * Get the ModelMetadataKey
	 * 
	 * @param keyName
	 * 		the metadata key name.
	 * @return
	 * 		the ModelMetadataKey.
	 */
	protected ModelMetadataKey getModelMetadataKey(String keyName){
		ModelMetadataKey key = getDirectoryServiceClient().getMetadataKey(keyName);
		return key;
	}
	
	/**
	 * Get the ModelServiceInstance by serviceName and instanceId.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param instanceId
	 * 		the instanceId.
	 * @return
	 * 		the ModelServiceInstance.
	 */
	public ModelServiceInstance getModelServiceInstance(String serviceName, String instanceId){
		ModelService service = getModelService(serviceName);
		if(service != null && service.getServiceInstances() != null ){
			for(ModelServiceInstance instance : new ArrayList<ModelServiceInstance>(service.getServiceInstances())){
				if(instance.getInstanceId().equals(instanceId)){
					return instance;
				}
			}
			
		}
		return null;
	}
	
	/**
	 * Get the ModelServiceInstance list by the Metadata Key.
	 * 
	 * @param keyName
	 * 		the metadata key name.
	 * @return
	 * 		the UP ModelServiceInstances that has the metadata key.
	 */
	public List<ModelServiceInstance> getModelInstancesByKey(String keyName){
		ModelMetadataKey key = getModelMetadataKey(keyName);
		if(key == null || key.getServiceInstances().size() == 0){
			return Collections.emptyList();
		}else{
			return new ArrayList<ModelServiceInstance>(key.getServiceInstances());
		}
	}
	
	/**
	 * Get the UP ModelServiceInstance list by the Metadata Key.
	 * 
	 * It only return the UP ServiceInstance.
	 * 
	 * @param keyName
	 * 		the metadata key name.
	 * @return
	 * 		the ModelServiceInstances that has the metadata key.
	 */
	public List<ModelServiceInstance> getUPModelInstancesByKey(String keyName){
		ModelMetadataKey key = getModelMetadataKey(keyName);
		
		List<ModelServiceInstance> list = null;
		if(key != null && key.getServiceInstances().size() > 0){
			
			for(ModelServiceInstance instance : new ArrayList<ModelServiceInstance>(key.getServiceInstances())){
				if(instance.getStatus().equals(OperationalStatus.UP)){
					if(list == null){
						list = new ArrayList<ModelServiceInstance>();
					}
					list.add(instance);
				}
			}
			
		}
		
		if(list == null){
			return Collections.emptyList();
		}else{
			return list;
		}
	}
	
	/**
	 * Get the ModelServiceInstance list of the Service.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @return
	 * 		the ModelServiceInstance list of the Service.
	 */
	public List<ModelServiceInstance> getModelInstances(String serviceName){
		ModelService service = getModelService(serviceName);
		if(service == null || service.getServiceInstances().size() == 0){
			return Collections.emptyList();
		}else{
			return new ArrayList<ModelServiceInstance>(service.getServiceInstances());
		}
	}
	
	/**
	 * Get the UP ModelServiceInstance list of the Service.
	 * 
	 * It only return the UP ServiceInstance of the Service.
	 * @param serviceName
	 * 		the service name.
	 * @return
	 * 		the ModelServiceInstance list of the Service.
	 */
	public List<ModelServiceInstance> getUPModelInstances(String serviceName){
		ModelService service = getModelService(serviceName);
		
		List<ModelServiceInstance> list = null;
		if(service != null && service.getServiceInstances().size() > 0){
			
			for(ModelServiceInstance instance : new ArrayList<ModelServiceInstance>(service.getServiceInstances())){
				if(instance.getStatus().equals(OperationalStatus.UP)){
					if(list == null){
						list = new ArrayList<ModelServiceInstance>();
					}
					list.add(instance);
				}
			}
			
		}
		
		if(list == null){
			return Collections.emptyList();
		}else{
			return list;
		}
	}
	
	/**
	 * Add a NotificationHandler to the Service.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param handler
	 * 		the NotificationHandler for the service.
	 */
	public void addNotificationHandler(String serviceName, NotificationHandler handler){
		
	}
	
	/**
	 * Remove the NotificationHandler from the Service.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param handler
	 * 		the NotificationHandler for the service.
	 */
	public void removeNotificationHandler(String serviceName, NotificationHandler handler){
		
	}
	
	/**
	 * Get the DirectoryServiceClient.
	 * 
	 * @return
	 * 		the DirectoryServiceClient
	 */
	protected DirectoryServiceClient getDirectoryServiceClient(){
		return directoryServiceClientManager.getDirectoryServiceClient();
	}
}
