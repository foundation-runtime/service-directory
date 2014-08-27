/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.Configurations;
import com.cisco.oss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.OperationResult;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.lifecycle.Closable;
import com.cisco.oss.foundation.directory.utils.JsonSerializer;

/**
 * It is the DirectoryLookupService with Cache.
 * 
 * It caches ServiceInstance for quick lookup and provides the cache sync function to
 * sync the latest change of the cached ServiceInstances.
 * 
 * @author zuxiang
 *
 */
public class CachedDirectoryLookupService extends DirectoryLookupService implements Closable {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CachedDirectoryLookupService.class);
	
	// Set this log to DEBUG to enable Service Cache dump in LookupManager.
	// It will dump the whole ServiceCache to log file when the Logger Changed first time,
	// and every time the Service Cache has new update.
	private static final Logger CacheDumpLogger = LoggerFactory.getLogger("com.cisco.oss.foundation.directory.cache.dump");
	
	/**
	 * The LookupManager cache sync executor kick off delay time property name in seconds.
	 */
	public static final String SD_API_CACHE_SYNC_DELAY_PROPERTY = "cache.sync.delay";
	
	/**
	 * The default delay time of LookupManager cache sync executor kick off.
	 */
	public static final int SD_API_CACHE_SYNC_DELAY_DEFAULT = 1;
	
	/**
	 * The LookupManager cache sync interval property name in seconds.
	 */
	public static final String SD_API_CACHE_SYNC_INTERVAL_PROPERTY = "cache.sync.interval";
	
	/**
	 * The default LookupManager cache sync interval value.
	 */
	public static final int SD_API_CACHE_SYNC_INTERVAL_DEFAULT = 10;
	
	/**
	 * The LookupManager cache enabled property.
	 */
	public static final String SD_API_CACHE_ENABLED_PROPERTY = "cache.enabled";
	
	/**
	 * The default cache enabled property value.
	 */
	public static final boolean SD_API_CACHE_ENABLED_DEFAULT = true;
	
	
	/**
	 * ScheduledExecutorService to sync cache.
	 */
	private ScheduledExecutorService syncService;
	
	/**
	 * Internal map cache for Service.
	 */
	private ServiceDirectoryCache<String, ModelService> cache;
	
	/**
	 * Internal map cache for the MetadataKey.
	 */
	private ServiceDirectoryCache<String, ModelMetadataKey> metaKeyCache;
	
	/**
	 * Mark whether component is started.
	 */
	private boolean isStarted = false;
	
	/**
	 * The JsonSerializer used in dump cache to serialize the ModelService.
	 */
	private JsonSerializer dumper = null;
	
	/**
	 * Constructor.
	 * 
	 * @param directoryServiceClientManager
	 * 		the DirectoryServiceClientManager.
	 */
	public CachedDirectoryLookupService(DirectoryServiceClientManager directoryServiceClientManager) {
		super(directoryServiceClientManager);
	}
	
	/**
	 * Start the CachedDirectoryLookupService.
	 * 
	 * It is thread safe.
	 * 
	 */
	@Override
	public void start(){
		if(this.isStarted == true) 
			return ;
		this.isStarted = true;
	}
	
	/**
	 * Stop the CachedDirectoryLookupService.
	 * 
	 * It is thread safe.
	 * 
	 */
	@Override
	public void stop(){
		if (this.isStarted == false)
			return;
		synchronized (this) {
			if (this.isStarted == true) {
				if(this.syncService != null){
					this.syncService.shutdown();
				}
				getCache().refresh();
				getMetadataKeyCache().refresh();
				this.isStarted = false;
			}
		}
	}
	
	/**
	 * Get the ModelService.
	 * 
	 * It will query the cache first, if the cache enabled.
	 * 
	 * @param serviceName
	 * 		the Service name.
	 * @return
	 * 		the ModelService.
	 */
	@Override
	protected ModelService getModelService(String serviceName){
		ModelService service = null;
		if (getCache().isCached(serviceName)) {
			service = getCache().getService(serviceName);
		} else {
			service = super.getModelService(serviceName);
			getCache().putService(serviceName, service);
		}
		return service;
	}
	
	/**
	 * Get the ModelMetadataKey
	 * 
	 * It will query the cache first, if the cache enabled.
	 * 
	 * @param keyName
	 * 		the metadata key name.
	 * @return
	 * 		the ModelMetadataKey.
	 */
	@Override
	protected ModelMetadataKey getModelMetadataKey(String keyName){
		ModelMetadataKey key = null;
		if(this.getMetadataKeyCache().isCached(keyName)){
			key = getMetadataKeyCache().getService(keyName);
		} else {
			key = super.getModelMetadataKey(keyName);
			getMetadataKeyCache().putService(keyName, key);
		}
		return key;
	}
	
	/**
	 * Initialize the CacheSyncTask lazily.
	 * 
	 * It is thread safe.
	 */
	private void initCacheSyncTask(){
		if(syncService == null){
			synchronized(this){
				if(syncService == null){
					int delay = Configurations.getInt(
							SD_API_CACHE_SYNC_DELAY_PROPERTY,
							SD_API_CACHE_SYNC_DELAY_DEFAULT);
					int interval = Configurations.getInt(
							SD_API_CACHE_SYNC_INTERVAL_PROPERTY,
							SD_API_CACHE_SYNC_INTERVAL_DEFAULT);

					syncService = Executors
							.newSingleThreadScheduledExecutor(new ThreadFactory() {

								@Override
								public Thread newThread(Runnable r) {
									Thread t = new Thread(r);
									t.setName("SD_Cache_Sync_Task");
									t.setDaemon(true);
									return t;
								}

							});

					syncService.scheduleWithFixedDelay(new CacheSyncTask(this),
							delay, interval, TimeUnit.SECONDS);
				}
			}
		}
	}
	
	/**
	 * Get the ServiceDirectoryCache that caches metadata key map, it is lazy initialized.
	 * 
	 * It is thread safe.
	 * 
	 * @return
	 * 		the ServiceDirectoryCache.
	 */
	private ServiceDirectoryCache<String, ModelMetadataKey> getMetadataKeyCache(){
		if(metaKeyCache == null){
			synchronized(this){
				if(metaKeyCache == null){
					metaKeyCache = new ServiceDirectoryCache<String, ModelMetadataKey>();
					initCacheSyncTask();
				}
			}
		}
		return metaKeyCache;
	}
	
	/**
	 * Get the ServiceDirectoryCache that caches Services, it is lazy initialized.
	 * 
	 * It is thread safe.
	 * 
	 * @return
	 * 		the ServiceDirectoryCache.
	 */
	private ServiceDirectoryCache<String, ModelService> getCache(){
		if(cache == null){
			synchronized (this) {
				if (this.cache == null) {
					this.cache = new ServiceDirectoryCache<String, ModelService>();
					initCacheSyncTask();
				}
			}
		}
		return this.cache;
	}
	
	/**
	 * Get the ModelService List for cache sync.
	 * 
	 * The ModelService doesn't have the referenced ModelServiceInstances.
	 * 
	 * @return
	 * 		the ModelService List.
	 */
	private List<ModelService> getAllServicesForSync(){
		List<ModelService> allServices = getCache().getAllServices();
		if(allServices.size() == 0){
			return Collections.emptyList();
		}
		
		List<ModelService> syncServices = new ArrayList<ModelService>();
		for(ModelService service : allServices){
			ModelService syncService = new ModelService(service.getName(), service.getId(), service.getModifiedTime(), service.getCreateTime());
			syncServices.add(syncService);
		}
		return syncServices;
	}
	
	/**
	 * Get the ModelMetadataKey List for sync.
	 * 
	 * The ModelMetadataKey doesn't have the referenced ModelServiceInstances.
	 * 
	 * @return
	 * 		the ModelMetadataKey List.
	 */
	private List<ModelMetadataKey> getAllMetadataKeysForSync(){
		List<ModelMetadataKey> allKeys = getMetadataKeyCache().getAllServices();
		if(allKeys.size() == 0){
			return Collections.emptyList();
		}
		List<ModelMetadataKey> syncKeys = new ArrayList<ModelMetadataKey>();
		for(ModelMetadataKey service : allKeys){
			ModelMetadataKey syncKey = new ModelMetadataKey(service.getName(), service.getId(), service.getModifiedTime(), service.getCreateTime());
			syncKeys.add(syncKey);
		}
		return syncKeys;
	}
	
	/**
	 * Get the MetadataKey changing delta.
	 * 
	 * @param keys
	 * 		the MetadataKey List.
	 * @return
	 * 		the ModelMetadataKey that has changed.
	 */
	private Map<String, OperationResult<ModelMetadataKey>> getMetadataKeyChanging(Map<String, ModelMetadataKey> keyMap){
		return this.getDirectoryServiceClient().getMetadataKeyChanging(keyMap);
	}
	
	/**
	 * Get the Service changing delta for Services list.
	 * 
	 * @param services
	 * 		the Service list.
	 * @return
	 * 		the Service list that has modification.
	 * @throws ServiceException
	 */
	private Map<String, OperationResult<ModelService>> getServiceChanging(Map<String, ModelService> serviceMap){
		return this.getDirectoryServiceClient().getServiceChanging(serviceMap);
	}
	
	/**
	 * Dump the ServiceCache to CacheDumpLogger Logger.
	 * 
	 * @return
	 * 		true if dump complete.
	 * @throws Exception
	 */
	private boolean dumpCache() throws Exception{
		
		if(CacheDumpLogger.isDebugEnabled()){
			
			List<ModelService> services = getCache().getAllServicesWithInstance();
			 if(dumper == null){
				 dumper = new JsonSerializer();
			 }
			 
			StringBuilder sb = new StringBuilder();
			sb.append("LookupManager dump Service Cache at: ").append(System.currentTimeMillis()).append("\n");
			for(ModelService service : services){
				sb.append(new String(dumper.serialize(service))).append("\n");
			}
			CacheDumpLogger.debug(sb.toString());
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * The Runnable for Cache Sync.
	 * 
	 * @author zuxiang
	 *
	 */
	private static class CacheSyncTask implements Runnable{

		private boolean lastCacheDump = false;
		private CachedDirectoryLookupService cachedLookupService;
		public CacheSyncTask(CachedDirectoryLookupService cachedLookupService){
			this.cachedLookupService = cachedLookupService;
		}
		@Override
		public void run() {
			try{
				
				
				List<ModelMetadataKey> keys = cachedLookupService.getAllMetadataKeysForSync();
				if(keys.size() > 0){
					Map<String, ModelMetadataKey> keyMap = new HashMap<String, ModelMetadataKey>();
					for(ModelMetadataKey key : keys){
						keyMap.put(key.getName(), key);
					}
					Map<String, OperationResult<ModelMetadataKey>> deltaKeys = cachedLookupService.getMetadataKeyChanging(keyMap);
					if(deltaKeys != null){
						for(Entry<String, OperationResult<ModelMetadataKey>> deltaKey : deltaKeys.entrySet()){
							String keyName = deltaKey.getKey();
							OperationResult<ModelMetadataKey> result = deltaKey.getValue();
							if(result.getResult()){
								ModelMetadataKey newKey = result.getobject();
								if(newKey != null){
									cachedLookupService.getMetadataKeyCache().putService(keyName, newKey);
									LOGGER.info("Update the ModelMetadataKey in cache, keyName=" + keyName);
								}
							} else {
								LOGGER.info("Cache sync ModelMetadataKey failed, keyName=" + keyName + " - " + result.getError().getErrorMessage());
							}
							
						}
					} else {
						LOGGER.error("Get no MetadataKey changed response.");
					}
				} else {
					LOGGER.info("No MetadataKey in the cache, skip cache sync.");
				}
			}catch(Exception e){
				LOGGER.error("Sync ModelMetadataKey cache from ServiceDirectory Server failed", e);
			}
			
			try{
				boolean cacheUpdated = false;
				List<ModelService> services = cachedLookupService.getAllServicesForSync();
				if(services.size() > 0){
					Map<String, ModelService> serviceMap = new HashMap<String, ModelService>();
					for(ModelService service : services){
						serviceMap.put(service.getName(), service);
					}
					Map<String, OperationResult<ModelService>> deltaSvcs = cachedLookupService.getServiceChanging(serviceMap);
					if(deltaSvcs != null){
						cacheUpdated = true;
						for(Entry<String, OperationResult<ModelService>> deltaService : deltaSvcs.entrySet()){
							String serviceName = deltaService.getKey();
							OperationResult<ModelService> result = deltaService.getValue();
							if(result.getResult()){
								ModelService newService = result.getobject();
								if(newService != null){
									cachedLookupService.getCache().putService(serviceName, newService);
									LOGGER.info("Update the ModeService in cache, serviceName=" + serviceName);
								}
							} else {
								LOGGER.info("Cache sync ModeService failed, serviceName=" + serviceName + " - " + result.getError().getErrorMessage());
							}
						}
					} else {
						LOGGER.error("Get no Service changed response.");
					}
				} else {
					LOGGER.info("No service in the cache, skip cache sync.");
				}
				
				try{
					if (cacheUpdated || lastCacheDump == false) {
						lastCacheDump = cachedLookupService.dumpCache();
					}
				} catch(Exception e){
					LOGGER.warn("Dump Service Cache failed. Set Logger " + CacheDumpLogger.getName() + " to INFO to close this message.");
					if(LOGGER.isTraceEnabled()){
						LOGGER.trace("Dump Service Cache failed.", e);
					}
				}
			}catch(Exception e){
				LOGGER.error("Sync ModelService cache from ServiceDirectory Server failed", e);
			}
			
		}
		
	}
}
