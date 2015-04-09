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

import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.configuration.ConfigurationFactory;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.impl.ConfigurableServiceDirectoryManagerFactory;
import com.cisco.oss.foundation.directory.impl.ServiceDirectoryImpl;

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
     * The static reference to ServiceDirectoryImpl
     */
    private static final AtomicReference<ServiceDirectoryImpl> sdImplRef = new AtomicReference<>();



    /**
     * Get the LookupManager.
     *
     * @return
     *         the implementation instance of LookupManager.
     * @throws ServiceException
     */
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
     * unless reset() method is called.
     *
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
    private static ServiceDirectoryImpl getImpl() {
        sdImplRef.compareAndSet(null,ServiceDirectoryImpl.getInstance());
        return sdImplRef.get();
    }

    // ------------
    // 1.2 API
    // ------------

    /**
     * reset the ServiceDirectory
     */
    public static void reset(){
        //TODO, currently the background thread is can NOT be restarted in Singleton mode.
        // consider to fix issue by either:
        //    1.) do not shutdown the ScheduledExecutorService in shutdown() method. instead by using future.cancel()
        // or 2.) replace the old one with new ScheduledExecutorService when restart
        getImpl().restart();
    }


    public static ServiceDirectoryConfig config(){ return new ServiceDirectoryConfig(); }
    public static ServiceDirectoryConfig globeConfig(){ return ServiceDirectoryConfig.GLOBE; }

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

        public enum ClientType{
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

        public ConfigurableServiceDirectoryManagerFactory build(){
            return new ConfigurableServiceDirectoryManagerFactory(this);
        }
    }



}
