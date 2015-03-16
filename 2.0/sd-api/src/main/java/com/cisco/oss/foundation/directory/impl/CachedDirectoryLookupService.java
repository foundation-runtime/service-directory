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

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.oss.foundation.directory.async.Watcher;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.lifecycle.Closable;
import com.cisco.oss.foundation.directory.proto.ServiceInstanceOperate;
import com.cisco.oss.foundation.directory.utils.JsonSerializer;
import com.cisco.oss.foundation.directory.utils.ServiceInstanceUtils;

/**
 * It is the DirectoryLookupService with Cache.
 *
 * It caches ServiceInstance for quick lookup and provides the cache sync function to
 * sync the latest change of the cached ServiceInstances.
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
     * Mark whether component is started.
     */
    private boolean isStarted = false;

    private Watcher watcher;

    /**
     * The JsonSerializer used in dump cache to serialize the ModelService.
     */
    private JsonSerializer dumper = null;

    /**
     * Indicate whether last cache dumped.
     */
    private boolean lastCacheDump = false;

    /**
     * Constructor.
     *
     * @param directoryServiceClientManager
     *         the DirectoryServiceClientManager.
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
     *         the Service name.
     * @return
     *         the ModelService.
     */
    @Override
    public ModelService getModelService(String serviceName){
        ModelService service = null;
        if (getCache().isCached(serviceName)) {
            service = getCache().getService(serviceName);
        } else {
            service= this.getDirectoryServiceClient().getService(serviceName, watcher);
            if(service != null){
                getCache().putService(serviceName, service);
            }
        }
        return service;
    }

    /**
     * Get the ServiceDirectoryCache that caches Services, it is lazy initialized.
     *
     * It is thread safe.
     *
     * @return
     *         the ServiceDirectoryCache.
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
     *         true if dump complete.
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
     * The CacheSync wather.
     *
     * Used to listen to the Server Watcher and update the local cache.
     *
     *
     */
    class CacheSyncWatcher implements Watcher{

        /**
         * listen the ServiceInstanceOperate and update the cache.
         *
         * {@inheritDoc}
         */
        @Override
        public void process(String name, ServiceInstanceOperate operate) {
            boolean cacheUpdated = false;

            if(getCache().isCached(name)){
                processServiceInstanceOperate(getCache().getService(name).getServiceInstances(), operate);
                cacheUpdated = true;
                LOGGER.warn("Update the ModelService in the cache, serviceName=" + name + ", instanceId=" + operate.getInstanceId());
            } else {
                LOGGER.warn("Drop the ServiceInstanceEvent, the service doesn't in the cache, serviceName=" + operate.getServiceName() + ", instanceId=" + operate.getInstanceId());
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

        /**
         * Process the cache update.
         *
         * @param instances
         *         the instances list.
         * @param operate
         *         the ServiceInstanceOperate.
         */
        private void processServiceInstanceOperate(List<ModelServiceInstance> instances, ServiceInstanceOperate operate){
            switch(operate.getType()){
            case Add:
                ModelServiceInstance model = operate.getServiceInstance();
                instances.add(model);
                CachedDirectoryLookupService.this.onServiceInstanceAvailable(ServiceInstanceUtils.transferFromModelServiceInstance(model));
                break;
            case Update:

                for(ModelServiceInstance instance : instances){

                    if(operate.getServiceName().equals(instance.getServiceName())
                            && operate.getInstanceId().equals(instance.getInstanceId())){
                        ModelServiceInstance updated = operate.getServiceInstance();
                        instance.setStatus(updated.getStatus());
                        instance.setUri(updated.getUri());
                        instance.setMetadata(updated.getMetadata());
                        instance.setAddress(updated.getAddress());
                        instance.setId(updated.getId());
                        instance.setInfo(updated.getInfo());
                        instance.setModifiedTime(updated.getModifiedTime());
                        instance.setPort(updated.getPort());
                        CachedDirectoryLookupService.this.onServiceInstanceChanged(ServiceInstanceUtils.transferFromModelServiceInstance(updated));
                        break;
                    }
                }
                break;
            case Delete:
                Iterator<ModelServiceInstance> it = instances.iterator();
                while(it.hasNext()){
                    ModelServiceInstance instance = it.next();

                    if(instance != null){
                        if(operate.getServiceName().equals(instance.getServiceName())
                                && operate.getInstanceId().equals(instance.getInstanceId())){
                            CachedDirectoryLookupService.this.onServiceInstanceUnavailable(ServiceInstanceUtils.transferFromModelServiceInstance(instance));
                            it.remove();
                            break;
                        }
                    }
                }
                break;
            default:
                break;
            }
        }

    }
}
