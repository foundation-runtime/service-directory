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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.Configurations;
import com.cisco.oss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationResult;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.lifecycle.Closable;
import static com.cisco.oss.foundation.directory.utils.JsonSerializer.*;
import com.cisco.oss.foundation.directory.utils.ServiceInstanceUtils;

/**
 * It is the DirectoryLookupService with client-side Cache.
 *
 * It caches ServiceInstance for quick lookup and provides the cache sync function to
 * sync the latest changes of the cached ServiceInstances.
 *
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
    public static final String SD_API_CACHE_SYNC_DELAY_PROPERTY = "com.cisco.oss.foundation.directory.cache.sync.delay";

    /**
     * The default delay time of LookupManager cache sync executor kick off.
     */
    public static final int SD_API_CACHE_SYNC_DELAY_DEFAULT = 1;

    /**
     * The LookupManager cache sync interval property name in seconds.
     */
    public static final String SD_API_CACHE_SYNC_INTERVAL_PROPERTY = "com.cisco.oss.foundation.directory.cache.sync.interval";

    /**
     * The default LookupManager cache sync interval value.
     */
    public static final int SD_API_CACHE_SYNC_INTERVAL_DEFAULT = 10;


    /**
     * ScheduledExecutorService to sync cache.
     */
    private final ScheduledExecutorService syncService;

    /**
     * Internal map cache for Service.
     */
    private final ConcurrentHashMap<String, ModelService> cache = new ConcurrentHashMap<String,ModelService>();

    /**
     * Internal map cache for the MetadataKey.
     */
    private final ConcurrentHashMap<String, ModelMetadataKey> metaKeyCache
            = new ConcurrentHashMap<String, ModelMetadataKey>();

    /**
     * Mark whether component is started.
     */
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * Constructor.
     *
     * @param directoryServiceClientManager
     *         the DirectoryServiceClientManager.
     */
    public CachedDirectoryLookupService(DirectoryServiceClientManager directoryServiceClientManager) {
        super(directoryServiceClientManager);
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

    }

    /**
     * Start the CachedDirectoryLookupService.
     *
     * It is thread safe.
     *
     */
    @Override
    public void start(){
        if (isStarted.compareAndSet(false,true)){
            initCacheSyncTask();
        }
    }

    /**
     * Stop the CachedDirectoryLookupService.
     *
     * It is thread safe.
     *
     */
    @Override
    public void stop(){
        if (isStarted.compareAndSet(true,false)) {
            this.syncService.shutdown();
            getCache().clear();
            getMetadataKeyCache().clear();
        }
    }

    /**
     * Get the ModelService.
     *
     * It will query the cache first. If not found in the cache, the service will be added to the cache.
     * 
     *
     * @param serviceName
     *         the Service name.
     * @return
     *         the ModelService.
     */
    @Override
    protected ModelService getModelService(String serviceName){
        ModelService service = null;
        if (getCache().containsKey(serviceName)) {
            service = getCache().get(serviceName);
        } else {
            service = super.getModelService(serviceName);
            getCache().putIfAbsent(serviceName, service);
        }
        return service;
    }

    /**
     * Get the ModelMetadataKey value
     *
     * It will query the cache first. If not found in the cache, the metadata key will be added to the cache.
     *
     * @param keyName
     *         the metadata key name.
     * @return
     *         the ModelMetadataKey.
     */
    @Override
    protected ModelMetadataKey getModelMetadataKeyValue(String keyName){
        ModelMetadataKey key = null;
        if(this.getMetadataKeyCache().containsKey(keyName)){
            key = getMetadataKeyCache().get(keyName);
        } else {
            key = super.getModelMetadataKeyValue(keyName);
            getMetadataKeyCache().putIfAbsent(keyName, key);
        }
        return key;
    }

    /**
     * initialization of the CacheSyncTask
     */
    private void initCacheSyncTask(){
        int delay = Configurations.getInt(
                SD_API_CACHE_SYNC_DELAY_PROPERTY,
                SD_API_CACHE_SYNC_DELAY_DEFAULT);
        int interval = Configurations.getInt(
                SD_API_CACHE_SYNC_INTERVAL_PROPERTY,
                SD_API_CACHE_SYNC_INTERVAL_DEFAULT);

        syncService.scheduleWithFixedDelay(new CacheSyncTask(),
                delay, interval, TimeUnit.SECONDS);
    }

    /**
     * Get the ServiceDirectoryCache that caches metadata key map, it is lazy initialized.
     *
     * It is thread safe.
     *
     * @return
     *         the ServiceDirectoryCache.
     */
    private ConcurrentHashMap<String, ModelMetadataKey> getMetadataKeyCache(){
        return metaKeyCache;
    }

    /**
     * Get the ServiceDirectoryCache that caches Services, it is lazy initialized.
     *
     * It is thread safe.
     *
     * @return
     *         the ServiceDirectoryCache.
     */
    private ConcurrentHashMap<String, ModelService> getCache(){
        return this.cache;
    }

    /**
     * Get the ModelService List for cache sync.
     *
     * The ModelService doesn't have the referenced ModelServiceInstances.
     *
     * @return
     *         the ModelService List.
     */
    private List<ModelService> getAllServicesForSync(){
        List<ModelService> allServices = new ArrayList<ModelService>();
        allServices.addAll(this.cache.values());

        List<ModelService> syncServices = new ArrayList<ModelService>();
        for(ModelService service : allServices){
            ModelService syncService = new ModelService(service.getName(), service.getId(), service.getCreateTime());
            syncServices.add(syncService);
        }
        return syncServices;
    }

    /**
     * Get the ModelMetadataKey List for cache sync.
     *
     * The ModelMetadataKey doesn't have the referenced ModelServiceInstances.
     *
     * @return
     *         the ModelMetadataKey List.
     */
    private List<ModelMetadataKey> getAllMetadataKeysForSync(){
        List<ModelMetadataKey> allKeys = new ArrayList<ModelMetadataKey>();
        allKeys.addAll(this.metaKeyCache.values());
        List<ModelMetadataKey> syncKeys = new ArrayList<ModelMetadataKey>();
        for(ModelMetadataKey service : allKeys){
            ModelMetadataKey syncKey = new ModelMetadataKey(service.getName(), service.getId(), service.getModifiedTime(), service.getCreateTime());
            syncKeys.add(syncKey);
        }
        return syncKeys;
    }

    /**
     * Get the changed list for the MetadataKey from the server.
     *
     * @param keyMap
     *         the MetadataKey List.
     * @return
     *         the ModelMetadataKey that has changed.
     */
    private Map<String, OperationResult<ModelMetadataKey>> getMetadataKeyChanging(Map<String, ModelMetadataKey> keyMap){
        return this.getDirectoryServiceClient().getChangedMetadataKeys(keyMap);
    }

    /**
     * Get the changed Services list from the server.
     *
     * @param serviceMap
     *         the Service list.
     * @return
     *         the Service list that has modification.
     * @throws ServiceException
     */
    private Map<String, OperationResult<ModelService>> getServiceChanging(Map<String, ModelService> serviceMap){
        return this.getDirectoryServiceClient().getChangedServices(serviceMap);
    }

    /**
     * Dump the ServiceCache to CacheDumpLogger Logger.
     *
     * @return
     *         true if dump complete.
     */
    private boolean dumpCache(){
        if (CacheDumpLogger.isDebugEnabled()) {
            try {
                List<ModelService> services = new ArrayList<ModelService>();
                services.addAll(getCache().values());
                StringBuilder sb = new StringBuilder();
                sb.append("LookupManager dump Service Cache at: ").append(System.currentTimeMillis()).append("\n");
                for (ModelService service : services) {
                    sb.append(new String(serialize(service))).append("\n");
                }
                CacheDumpLogger.debug(sb.toString());
            } catch (Exception e) {
                LOGGER.warn("Dump Service Cache failed. Set Logger " + CacheDumpLogger.getName() + " to INFO to close this message.");
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Dump Service Cache failed.", e);
                }
                return false;
            }
            return true;
        }
        return false;

    }

    /**
     * The Runnable for Cache Sync.
     *
     *
     */
    private class CacheSyncTask implements Runnable{

        private boolean lastCacheDump = false;
        private final CachedDirectoryLookupService cachedLookupService = CachedDirectoryLookupService.this;
        public CacheSyncTask(){
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
                                    cachedLookupService.getMetadataKeyCache().put(keyName, newKey);
                                    LOGGER.info("Update the ModelMetadataKey in cache, keyName=" + keyName);
                                }
                            } else {
                                LOGGER.error("Cache sync ModelMetadataKey failed, keyName=" + keyName + " - " + result.getError().getErrorMessage());
                            }

                        }
                    } else {
                        LOGGER.info("No MetadataKey is changed from server response.");
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
                                ModelService oldService = cachedLookupService.getCache().get(serviceName);
                                if(newService != null){
                                    cachedLookupService.getCache().put(serviceName, newService);
                                    LOGGER.info("Update the ModelService in cache, serviceName=" + serviceName );
                                }
                                onServiceChanged(newService, oldService);
                            } else {
                                LOGGER.error("Cache sync ModelService failed, serviceName=" + serviceName + " - " + result.getError().getErrorMessage());
                            }
                        }
                    } else {
                        LOGGER.info("No Service is changed from server response.");
                    }
                } else {
                    LOGGER.info("No service in the cache, skip cache sync.");
                }

                if (cacheUpdated || lastCacheDump == false) {
                    lastCacheDump = cachedLookupService.dumpCache();
                }

            }catch(Exception e){
                LOGGER.error("Sync ModelService cache from ServiceDirectory Server failed", e);
            }
        }
        

        private void onServiceChanged(ModelService newService, ModelService oldService){
            if(newService == null || oldService == null || newService == oldService){
                return;
            }

            List<ModelServiceInstance> oldInstances = oldService.getServiceInstances();
            List<ModelServiceInstance> newInstances = newService.getServiceInstances();

            if(newInstances == null || newInstances.size() == 0){
                if(oldInstances != null){
                    for(ModelServiceInstance model : oldInstances){
                        if (model.getStatus().equals(OperationalStatus.UP)) {
                            //Change the status to DOWN before the notification when unregistering a running instance
                            model.setStatus(OperationalStatus.DOWN);
                            cachedLookupService.onServiceInstanceUnavailable(ServiceInstanceUtils.toServiceInstance(model));
                        }
                    }
                }
            } else {
                if(oldInstances == null || oldInstances.size() == 0){
                    for(ModelServiceInstance model : newInstances){
                        if (model.getStatus().equals(OperationalStatus.UP)) {
                            cachedLookupService.onServiceInstanceAvailable(ServiceInstanceUtils.toServiceInstance(model));
                        }
                    }
                } else {

                    // Loop through all instances (added, deleted, changed) and send the proper notifications
                    // Can not operate directly on newInstances or oldIntances since it will remove the item from cache
                    List<ModelServiceInstance> newTmp = new ArrayList<ModelServiceInstance>();
                    List<ModelServiceInstance> oldTmp = new ArrayList<ModelServiceInstance>();
                    
                    for (ModelServiceInstance model : oldInstances) {
                        oldTmp.add(model);
                    }
                    for (ModelServiceInstance model : newInstances) {
                        newTmp.add(model);
                    }
                    
                    Iterator<ModelServiceInstance> itnew = newTmp.iterator();
                    Iterator<ModelServiceInstance> itold = oldTmp.iterator();
                    
                    while (itnew.hasNext()) {
                        while (itold.hasNext()) {
                            ModelServiceInstance curnew = itnew.next();
                            ModelServiceInstance curold = itold.next();
                    
                            if (curnew.getInstanceId().equals(curold.getInstanceId())) {
                                
                                if(curnew.getStatus().equals(OperationalStatus.UP) && curold.getStatus().equals(OperationalStatus.DOWN)) { 
                                    cachedLookupService.onServiceInstanceAvailable(ServiceInstanceUtils.toServiceInstance(curnew));
                                } 
                                if (curnew.getStatus().equals(OperationalStatus.DOWN) && curold.getStatus().equals(OperationalStatus.UP)) {
                                    cachedLookupService.onServiceInstanceUnavailable(ServiceInstanceUtils.toServiceInstance(curnew));
                                }
                                // Check if the service instance metadata has been changed
                                if (curnew.getMetadata() != null && curold.getMetadata() != null && !curnew.getMetadata().equals(curold.getMetadata())) {
                                    cachedLookupService.onServiceInstanceChanged(ServiceInstanceUtils.toServiceInstance(curnew));
                                }
                                
                                itnew.remove();
                                itold.remove();
                            }
                        }
                    }
                    
                    for (ModelServiceInstance model : oldTmp) {
                        if (model.getStatus().equals(OperationalStatus.UP)) {
                            //Change the status to DOWN before the notification when unregistering a running instance
                            model.setStatus(OperationalStatus.DOWN);
                            cachedLookupService.onServiceInstanceUnavailable(ServiceInstanceUtils.toServiceInstance(model));
                        }
                    }
                    
                    for (ModelServiceInstance model : newTmp) {
                        if (model.getStatus().equals(OperationalStatus.UP)) {
                            cachedLookupService.onServiceInstanceAvailable(ServiceInstanceUtils.toServiceInstance(model));
                        }
                    }
                }
            }
        }

    }
}
