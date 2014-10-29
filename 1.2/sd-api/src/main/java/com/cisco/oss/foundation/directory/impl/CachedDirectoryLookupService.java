/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.impl;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.oss.foundation.directory.async.Watcher;
import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.WatcherType;
import com.cisco.oss.foundation.directory.lifecycle.Closable;
import com.cisco.oss.foundation.directory.proto.ServiceInstanceOperate;
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
	
	private Watcher watcher;
	
	/**
	 * The JsonSerializer used in dump cache to serialize the ModelService.
	 */
	private JsonSerializer dumper = null;
	
	private boolean lastCacheDump = false;
	
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
		watcher = new CacheSyncWatcher();
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
				getCache().refresh();
				getMetadataCache().refresh();
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
	public ModelService getModelService(String serviceName){
		ModelService service = null;
		if (getCache().isCached(serviceName)) {
			service = getCache().getService(serviceName);
		} else {
			service= this.getDirectoryServiceClient().getService(serviceName, watcher);
//			service = super.getModelService(serviceName);
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
	public ModelMetadataKey getModelMetadataKey(String keyName){
		ModelMetadataKey key = null;
		if(this.getMetadataCache().isCached(keyName)){
			key = getMetadataCache().getService(keyName);
		} else {
//			key = super.getModelMetadataKey(keyName);
			key = this.getDirectoryServiceClient().getMetadata(keyName, watcher);
			getMetadataCache().putService(keyName, key);
		}
		return key;
	}
	
	/**
	 * Get the ServiceDirectoryCache that caches metadata key map, it is lazy initialized.
	 * 
	 * It is thread safe.
	 * 
	 * @return
	 * 		the ServiceDirectoryCache.
	 */
	private ServiceDirectoryCache<String, ModelMetadataKey> getMetadataCache(){
		if(metaKeyCache == null){
			synchronized(this){
				if(metaKeyCache == null){
					metaKeyCache = new ServiceDirectoryCache<String, ModelMetadataKey>();
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
				}
			}
		}
		return this.cache;
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
	
	class CacheSyncWatcher implements Watcher{

		@Override
		public void process(String name, WatcherType type, ServiceInstanceOperate operate) {
			boolean cacheUpdated = false;
			if(WatcherType.SERVICE.equals(type)){
				cacheUpdated = true;
				if(getCache().isCached(name)){
					processServiceInstanceOperate(getCache().getService(name).getServiceInstances(), operate);
					LOGGER.warn("Update the ModelService in the cache, serviceName=" + name + ", instanceId=" + operate.getInstanceId());
				} else {
					LOGGER.warn("Drop the ServiceInstanceEvent, the service doesn't in the cache, serviceName=" + operate.getServiceName() + ", instanceId=" + operate.getInstanceId());
				}
			} else if(WatcherType.METADATA.equals(type)){
				if(getMetadataCache().isCached(name)){
					processServiceInstanceOperate(getMetadataCache().getService(name).getServiceInstances(), operate);
					LOGGER.warn("Update the ModelMetadata in the cache, serviceName=" + operate.getServiceName() + ", instanceId=" + operate.getInstanceId() + ", metaName=" + name);
				} else {
					LOGGER.warn("Drop the MetadataEvent, the service doesn't in the cache, serviceName=" + operate.getServiceName() + ", instanceId=" + operate.getInstanceId() + ", metaName=" + name);
				}
			} else {
				LOGGER.warn("Unknown WatcherEvent, type=" + type + ", object=" + name + ", service=" + operate.getServiceName() + ", instanceId=" + operate.getInstanceId() + ", class=" + operate.getClass().getName());
			}
			
			try{
				if (cacheUpdated || lastCacheDump == false) {
					lastCacheDump = dumpCache();
				}
			} catch(Exception e){
				LOGGER.warn("Dump Service Cache failed. Set Logger " + CacheDumpLogger.getName() + " to INFO to close this message.");
				if(LOGGER.isTraceEnabled()){
					LOGGER.trace("Dump Service Cache failed.", e);
				}
			}
		}
		
		private void processServiceInstanceOperate(List<ModelServiceInstance> instances, ServiceInstanceOperate operate){
			switch(operate.getType()){
			case Add:
				instances.add(operate.getServiceInstance());
				break;
			case Update:
				
				for(ModelServiceInstance instance : instances){
					
					if(operate.getServiceName().equals(instance.getServiceName())
							&& operate.getInstanceId().equals(instance.getInstanceId())){
						ModelServiceInstance updated = operate.getServiceInstance();
						instance.setStatus(updated.getStatus());
						instance.setUri(updated.getUri());
						instance.setMetadata(updated.getMetadata());
						break;
					}
				}
				break;
			case Delete:
				Iterator<ModelServiceInstance> it = instances.iterator();
				while(it.hasNext()){
					ModelServiceInstance instance = it.next();
					
					if(operate.getServiceName().equals(instance.getServiceName())
							&& operate.getInstanceId().equals(instance.getInstanceId())){
						it.remove();
						break;
					}
				}
				break;
			default:
				break;
			}
		}
		
	}
}
