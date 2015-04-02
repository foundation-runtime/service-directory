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




package com.cisco.oss.foundation.directory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.configuration.ConfigurationFactory;
import com.cisco.oss.foundation.directory.client.DirectoryServiceClient;
import com.cisco.oss.foundation.directory.client.DirectoryServiceClientProvider;
import com.cisco.oss.foundation.directory.client.DirectoryServiceDummyClient;
import com.cisco.oss.foundation.directory.client.DirectoryServiceRestfulClient;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.impl.AbstractServiceDirectoryManager;
import com.cisco.oss.foundation.directory.impl.CloseListener;
import com.cisco.oss.foundation.directory.impl.ServiceDirectoryImpl;
import com.cisco.oss.foundation.directory.impl.ServiceDirectoryService;
import com.cisco.oss.foundation.directory.lookup.CachedDirectoryLookupService;
import com.cisco.oss.foundation.directory.lookup.CachedLookupManagerImpl;
import com.cisco.oss.foundation.directory.lookup.DirectoryLookupService;
import com.cisco.oss.foundation.directory.lookup.LookupManagerImpl;
import com.cisco.oss.foundation.directory.registration.DirectoryRegistrationService;
import com.cisco.oss.foundation.directory.registration.HeartbeatDirectoryRegistrationService;
import com.cisco.oss.foundation.directory.registration.HeartbeatRegistrationManagerImpl;
import com.cisco.oss.foundation.directory.registration.RegistrationManagerImpl;

/**
 * ServiceDirectory client class.
 *
 * Applications obtain the instance of LookupManager/RegistrationManager/ServiceDirectoryConfig from this class.
 *
 *
 */
public class ServiceDirectory {

    public static final Logger LOGGER = LoggerFactory.getLogger(ServiceDirectory.class);

    /**
     * The ServiceDirectory enabled property name, indicating whether ServiceDirectory is enabled for directory service.
     */
    public static final String SD_API_SERVICE_DIRECTORY_ENABLED_PROPERTY = "com.cisco.oss.foundation.directory.enabled";

    /**
     * Default is to enable ServiceDirectory for directory service.
     */
    public static final boolean SD_API_SERVICE_DIRECTORY_ENABLED_DEFAULT = true;

    /**
     * The default config load by foundation runtime from config.properties or configSchema.xml
     */
    private static final Configuration defaultConfigLoadByFoundationRuntime = ConfigurationFactory.getConfiguration();

    /**
     * Get the LookupManager.
     *
     * @return
     *         the implementation instance of LookupManager.
     * @throws ServiceException
     */
    @Deprecated
    public static LookupManager getLookupManager() throws ServiceException {
        return getImpl().getLookupManager();
    }

    /**
     * Get the RegistrationManager.
     *
     * @return
     *         the implementation instance of RegistrationManager.
     * @throws ServiceException
     */
    @Deprecated
    public static RegistrationManager getRegistrationManager() throws ServiceException {
        return getImpl().getRegistrationManager();
    }

    /**
     * Get the ServiceDirectoryConfig.
     *
     * @return
     *         the ServiceDirectoryConfig
     * @deprecated as release 1.2. it's globe configuration
     */
    @Deprecated
    public static Configuration getServiceDirectoryConfig() {
        return defaultConfigLoadByFoundationRuntime;
    }

    /**
     * Re-initialize the ServiceDirectoryManagerFactory.
     *
     * It allows applications to change the ServiceDirectoryManagerFactory in the runtime.
     *
     * @param factory
     *         the ServiceDirectoryManagerFactory which generates LookupManager and RegistrationManager.
     */
    @Deprecated
    public static void reinitServiceDirectoryManagerFactory(ServiceDirectoryManagerFactory factory) throws ServiceException{
        getImpl().reinitServiceDirectoryManagerFactory(factory);
    }

    /**
     * Check whether ServiceDirectory is enabled for the directory service.
     *
     * The value comes from the configuration property "com.cisco.oss.foundation.directory.enabled".
     *
     * By default the ServiceDirectory is enabled.
     *
     * @return
     *         true if the ServiceDirectory is enabled.
     */
    @Deprecated
    public static boolean isEnabled(){
        return getServiceDirectoryConfig().getBoolean(SD_API_SERVICE_DIRECTORY_ENABLED_PROPERTY,
                SD_API_SERVICE_DIRECTORY_ENABLED_DEFAULT);
    }

    /**
     * Get the Service Directory API version
     * 
     * @return
     *        the Service Directory API version 
     */
    public static String getAPIVersion() {
        return getImpl().getVersion();
    }
    
    /**
     * Shut down the ServiceDirectory.
     *
     * Be careful to invoke this method. When shutdown() is called, ServiceDirectory cannot be used 
     * unless jvm is restarted to reload the ServiceDirectory class. 
     */
    public static void shutdown(){
        getImpl().shutdown();
    }

    /**
     * Get the ServiceDirectory implementation.
     *
     * @return
     *         the ServiceDirectoryImpl instance.
     */
    @Deprecated
    private static ServiceDirectoryImpl getImpl() {
        return ServiceDirectoryImpl.getInstance();
    }


    // ------------
    // 1.2 API
    // ------------

    public static ServiceDirectoryConfig config(){ return new ServiceDirectoryConfig(); }
    public static ServiceDirectoryConfig globeConfig(){ return ServiceDirectoryConfig.GLOBE; }



    /**
     * SD is build by SDConfig
     */
    public final static class ServiceDirectoryConfig {
        //TODO, move all config key here !
        /**
         * The LookupManager cache enabled property.
         */
        public static final String SD_API_CACHE_ENABLED_PROPERTY = "com.cisco.oss.foundation.directory.cache.enabled";

        /**
         * The default cache enabled property value.
         */
        public static final boolean SD_API_CACHE_ENABLED_DEFAULT = true;

        /**
         * The Registration heartbeat and health check enabled property name.
         */
        public static final String SD_API_HEARTBEAT_ENABLED_PROPERTY = "com.cisco.oss.foundation.directory.heartbeat.enabled";

        /**
         * the default value of heartbeat enabled property value.
         */
        public static final boolean SD_API_HEARTBEAT_ENABLED_DEFAULT = true;

        /**
         * The Client type property name
         *
         */
        public static final String SD_API_CLIENT_TYPE_PROPERTY = "com.cisco.oss.foundation.directory.client.type";

        /**
         * The default value of Client type : RESTFUL
         */
        public static final String SD_API_CLIENT_TYPE_PROPERTY_DEFAULT = ClientType.RESTFUL.name();

        public static enum ClientType{
            RESTFUL, //only support 1 kind of client in 1.2
            DUMMY,  //its used for unitTest, so that no actual request is send to server side
            PROVIDED, //user will supply a customized Client by using ClientProvider interface.
        }
        private static final ServiceDirectoryConfig GLOBE = new ServiceDirectoryConfig(defaultConfigLoadByFoundationRuntime);
        private final Configuration _apacheConfig;

        private ServiceDirectoryConfig(Configuration root) {
            _apacheConfig=root;
        }
        private ServiceDirectoryConfig(){
            _apacheConfig = new BaseConfiguration();
        }

        public ClientType getClientType(){
            try {
                return ClientType.valueOf(_get(SD_API_CLIENT_TYPE_PROPERTY));
            }catch (Exception e){
                throw new IllegalArgumentException("Unknown Client type");
            }
        }
        public ServiceDirectoryConfig setClientType(ClientType clientType){
            _set(SD_API_CLIENT_TYPE_PROPERTY,clientType.name());
            return this;
        }
        public ServiceDirectoryConfig setCacheEnabled(boolean cacheEnable){
            _set(SD_API_CACHE_ENABLED_PROPERTY, cacheEnable);
            return this;
        }
        public boolean isCacheEnabled(){
            return _checkEnable(SD_API_CACHE_ENABLED_PROPERTY);
        }
        public ServiceDirectoryConfig setHeartbeatEnabled(boolean heartbeatEnable){
            _set(SD_API_HEARTBEAT_ENABLED_PROPERTY,heartbeatEnable);
            return this;
        }
        public boolean isHeartBeatEnabled(){
            return _checkEnable(SD_API_HEARTBEAT_ENABLED_PROPERTY);
        }
        private void _set(String key, Object value){
            _apacheConfig.setProperty(key,value);
            if (this == GLOBE){
                LOGGER.warn("GLOBE ServiceDirectoryConfig changed! '{}' = '{}'",key,value);
            }
        }
        private String _get(String key){
            if (_apacheConfig.containsKey(key)){
                return _apacheConfig.getProperty(key).toString();
            }else{
                if (this==GLOBE){ // not found in GLOBE, throw ex
                    throw new IllegalArgumentException("Unknown Service Directory configuration key '"+key+"'");

                }
                return GLOBE._get(key);
            }
        }
        private boolean _checkEnable(String key){
            String value = _get(key); //not null guaranteed
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) { //protector of Boolean.valuesOf/parseBoolean
                return Boolean.valueOf(value);
            } else {
                throw new IllegalArgumentException("The Service Directory configuration key '" + key + "'="+value+" is not boolean type");
            }
        }

        // the builder of SD
        public ServiceDirectory build(){
            return new ServiceDirectory(this);
        }
    }

    /*
     * SD Constructor by using SD Config
     * The constructor is protected by private, so that only
     * builder can call it
     */
    private ServiceDirectory(ServiceDirectoryConfig config){
            this._config = config;
            if (_config.isCacheEnabled()){
                this._lookUpService = new CachedDirectoryLookupService(getClient());
            }else{
                this._lookUpService = new DirectoryLookupService(getClient());
            }

    }

    private final List<LookupManager> lookupManagerReferences = new ArrayList<>();
    private final List<RegistrationManager> RegistrationManagerReferences = new ArrayList<>();

    private final CloseListener managerCloseListener = new CloseListener(){
        @Override
        public void fireServiceClose(ServiceDirectoryService service) {
            service.stop();
        }
        @Override
        public void onManagerClose(AbstractServiceDirectoryManager manager) {
            if (manager instanceof LookupManager) {
                synchronized (lookupManagerReferences) {
                    if (lookupManagerReferences.contains(manager)) {
                        lookupManagerReferences.remove(manager);
                        manager.stop();
                    }
                    if (lookupManagerReferences.size() == 0) {
                        // when all lookup manager closed, fire service close
                        fireServiceClose(manager.getService());
                    }
                }
            }else if (manager instanceof RegistrationManager){
                //TODO, handle Registration Mangers
            }else{
                throw new IllegalStateException("Unknown manager "+manager);
            }
        }

    };

    private final ServiceDirectoryConfig _config;
    private final DirectoryLookupService _lookUpService;

    // -----------------------
    // DirectoryServiceClient
    // -----------------------

    /** restful (http) client */
    private static final DirectoryServiceClient _restfulClient = new DirectoryServiceRestfulClient();

    /** dummy client */
    private static final DirectoryServiceClient _dummyClient = new DirectoryServiceDummyClient();

    /** provided client */
    private static final AtomicReference<DirectoryServiceClientProvider> _clientProvider =
            new AtomicReference<>();
    public static void setClientProvider(DirectoryServiceClientProvider provider){
        if (provider==null){
            throw new IllegalArgumentException("DirectoryServiceClientProvider can't be null");
        }
        _clientProvider.set(provider);
    }

    DirectoryServiceClient getClient(){
        DirectoryServiceClient client;
        switch (_config.getClientType()) {
            case RESTFUL:
                client=_restfulClient;
                break;
            case DUMMY:
                client= _dummyClient;
                break;
            case PROVIDED:
                DirectoryServiceClientProvider provider = _clientProvider.get();
                if (provider!=null){
                    client=provider.getClient();
                }else{
                    throw new IllegalStateException("No DirectoryServiceClientProvider is set up for Client Type PROVIDED");
                }
                break;
            default:
                //don't support other client type now.
                throw new IllegalStateException("UNKNOWN Client Type "+_config.getClientType());
        }
        return client;
    }

    DirectoryLookupService getLookupService(){
        return this._lookUpService;
    }

    public LookupManager newLookupManager() throws ServiceException {
        if (_config.isCacheEnabled()){
            //TODO, fix the force conversion
            CachedLookupManagerImpl cachedMgr = new CachedLookupManagerImpl((CachedDirectoryLookupService)getLookupService());
            cachedMgr.setCloseListener(managerCloseListener);
            lookupManagerReferences.add(cachedMgr);
            return cachedMgr;
        }else {
            LookupManagerImpl mgr =  new LookupManagerImpl(getLookupService());
            mgr.setCloseListener(managerCloseListener);
            lookupManagerReferences.add(mgr);
            return mgr;
        }
    }
    public RegistrationManager newRegistrationManager() throws ServiceException {
        if (_config.isHeartBeatEnabled()){
            return new HeartbeatRegistrationManagerImpl(new HeartbeatDirectoryRegistrationService(getClient()));
        }else{
            return new RegistrationManagerImpl(new DirectoryRegistrationService(getClient()));
        }
    }

}
