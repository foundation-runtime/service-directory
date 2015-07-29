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
package com.cisco.oss.foundation.directory.lookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.cache.ModelServiceClientCache;
import com.cisco.oss.foundation.directory.client.DirectoryServiceClient;
import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.lifecycle.Stoppable;

import static com.cisco.oss.foundation.directory.utils.JsonSerializer.serialize;

/**
 * It is the DirectoryLookupService with client-side Cache.
 *
 * It caches ServiceInstance for quick lookup and provides the cache sync function to
 * sync the latest changes of the cached ServiceInstances.
 *
 *
 */
public class CachedDirectoryLookupService extends DirectoryLookupService implements Stoppable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CachedDirectoryLookupService.class);

    // Set this log to DEBUG to enable Service Cache dump in LookupManager.
    // It will dump the whole ServiceCache to log file when the Logger Changed first time,
    // and every time the Service Cache has new update.
    private static final Logger CacheDumpLogger = LoggerFactory.getLogger("com.cisco.oss.foundation.directory.cache.dump");

    /**
     * The LookupManager cache sync executor kickoff delay time property name in seconds.
     * @deprecated
     * use {@link DirectoryLookupService#SD_API_POLLING_DELAY_PROPERTY}
     */
    public static final String SD_API_CACHE_SYNC_DELAY_PROPERTY = SD_API_POLLING_DELAY_PROPERTY;

    /**
     * The default delay time of LookupManager cache sync executor kickoff.
     * @deprecated
     * use {@link DirectoryLookupService#SD_API_POLLING_DELAY_PROPERTY}
     */
    public static final int SD_API_CACHE_SYNC_DELAY_DEFAULT = SD_API_POLLING_DELAY_DEFAULT;

    /**
     * The LookupManager cache sync interval property name in seconds.
     * @deprecated
     * use {@link DirectoryLookupService#SD_API_POLLING_INTERVAL_PROPERTY}
     */
    public static final String SD_API_CACHE_SYNC_INTERVAL_PROPERTY = SD_API_POLLING_INTERVAL_PROPERTY;

    /**
     * The default LookupManager cache sync interval value.
     * @deprecated
     * use {@link DirectoryLookupService#SD_API_POLLING_INTERVAL_DEFAULT}
     */
    public static final int SD_API_CACHE_SYNC_INTERVAL_DEFAULT = SD_API_POLLING_INTERVAL_DEFAULT;


    /**
     * ScheduledExecutorService to sync cache.
     */
    private final AtomicReference<ScheduledExecutorService> syncService = new AtomicReference<>();

    /**
     * Internal cache that maps the service name to a list of service instances.
     */
    private final ConcurrentHashMap<String, ModelServiceClientCache> cache = new ConcurrentHashMap<>();

    /**
     * Internal cache that maps the metadata key name to a list of service instances.
     */
    private final ConcurrentHashMap<String, ModelMetadataKey> metaKeyCache = new ConcurrentHashMap<>();

    /**
     * Mark whether component is started.
     */
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * Constructor.
     *
     * @param directoryServiceClient
     *         the DirectoryServiceClient.
     */
    public CachedDirectoryLookupService(DirectoryServiceClient directoryServiceClient) {
        super(directoryServiceClient);
        syncService.set(newSyncService());

    }

    private ScheduledExecutorService newSyncService(){
        return Executors
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
            super.start();
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
            // if you shutdown it, it can not be use anymore
            super.stop();
            ScheduledExecutorService service = this.syncService.getAndSet(newSyncService());
            service.shutdown();
            LOGGER.info("Cache sync Service is shutdown");
            for (Entry<String,ModelServiceClientCache> entry : cache.entrySet()){
               removeInstanceChangeListener(entry.getKey(),entry.getValue());
            }
            getCache().clear();
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
    public ModelService getModelService(String serviceName){
        ModelServiceClientCache cache = getCache().get(serviceName);
        ModelService lookup;
        if (cache == null) {
            // cache has not never been created, initialize an new one.
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("service has not been cached, try to cache the service {} ", serviceName);
            }
            lookup = super.getModelService(serviceName);
            getCache().putIfAbsent(serviceName, new ModelServiceClientCache(lookup));
            cache = getCache().get(serviceName);
            addInstanceChangeListener(serviceName, cache);
        } else {
            // service cached has been removed
            if (cache.getData() == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("cached service={} is obsoleted, try to get service from server", serviceName);
                }
                lookup = super.getModelService(serviceName);
                if (lookup != null) {
                    // replace old cached service by new one
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("try to replace the obsoleted cached service={} by service from server {}", serviceName,lookup.getServiceInstances());
                    }
                    removeInstanceChangeListener(serviceName, cache);
                    boolean replaced = getCache().replace(serviceName, cache, new ModelServiceClientCache(lookup));
                    if (replaced) {
                        addInstanceChangeListener(serviceName, getCache().get(serviceName));

                    } else {
                        LOGGER.error("fail to replace the obsoleted cached service={}", serviceName);
                    }
                }else{
                    LOGGER.error("fail to lookup service={} from server",serviceName);
                }
            }
            // use the use cached service
            else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("service={} has been cached, get service from cache. {}", serviceName,cache.getData().getServiceInstances());
                }
                lookup = cache.getData();
            }
        }
        return lookup;
    }

    /**
     * initialization of the CacheSyncTask
     */
    private void initCacheSyncTask(){
        LOGGER.info("Cache sync Service is started");
    }


    /**
     * Get the ServiceDirectoryCache that caches Services.
     *
     * It is thread safe.
     *
     * @return
     *         the ServiceDirectoryCache.
     */
    private ConcurrentHashMap<String, ModelServiceClientCache> getCache(){
        return this.cache;
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
                List<ModelService> services = new ArrayList<>();
                for (ModelServiceClientCache cache : getCache().values()){
                    services.add(cache.getData());
                }
                StringBuilder sb = new StringBuilder();
                sb.append("LookupManager dumpped Service Cache at: ").append(System.currentTimeMillis()).append("%n");
                for (ModelService service : services) {
                    sb.append(new String(serialize(service))).append("%n");
                }
                CacheDumpLogger.debug(sb.toString());
            } catch (Exception e) {
                LOGGER.warn("Dump Service Cache failed. Set Logger {} to INFO to disable this message.",
                            CacheDumpLogger.getName());                
                LOGGER.trace("Dump Service Cache failed. ", e);             
                return false;
            }
            return true;
        }
        return false;

    }

}
