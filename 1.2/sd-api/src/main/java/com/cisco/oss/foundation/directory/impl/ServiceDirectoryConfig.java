package com.cisco.oss.foundation.directory.impl;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import com.cisco.oss.foundation.directory.ServiceDirectory;

/**
 * The ServiceDirectoryConfig class works as a Copy of the the default configure get from
 * {@link ServiceDirectory#getServiceDirectoryConfig()}. So that {@link ConfigurableServiceDirectoryManagerFactory}
 * can be build according to the customized configuration object.<p>
 *
 * The Class is used only for Unit-test purpose now.
 *
 * @since 1.2
 */
public final class ServiceDirectoryConfig {
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
     */
    public static final String SD_API_CLIENT_TYPE_PROPERTY = "com.cisco.oss.foundation.directory.client.type";

    /**
     * Client Types
     */
    public enum ClientType {
        RESTFUL, //only support 1 kind of client in 1.2
        DUMMY,    //its used for unitTest, so that no actual request is send to server side
        IN_MEMORY, //also used in unit-test. so that all r/w into memory, without require a real sd-server.
        PROVIDED, //user will supply a customized Client by using ClientProvider interface.
    }

    /**
     * The private static singleton which hold the reference of Configuration which
     * is load by foundation runtime from config.properties or configSchema.xml
     */
    private static final ServiceDirectoryConfig GLOBE = new ServiceDirectoryConfig(ServiceDirectory.getServiceDirectoryConfig());

    /**
     * The public accessor to Globe configuration
     * @return the static singleton ServiceDirectoryConfig hold the reference to configuration load by foundation runtime.
     */
    public static ServiceDirectoryConfig globeConfig(){ return GLOBE; }

    /**
     * Factory method to create a new ServiceDirectoryConfig instance
     * @return a new ServiceDirectoryConfig instance
     */
    public static ServiceDirectoryConfig config(){ return new ServiceDirectoryConfig(new BaseConfiguration()); }

    private final Configuration _apacheConfig;

    private ServiceDirectoryConfig(Configuration root) {
        _apacheConfig = root;
    }

    public ClientType getClientType() {
        try {
            return ClientType.valueOf(_get(SD_API_CLIENT_TYPE_PROPERTY));
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown Client type");
        }
    }

    public ServiceDirectoryConfig setClientType(ClientType clientType) {
        _set(SD_API_CLIENT_TYPE_PROPERTY, clientType.name());
        return this;
    }

    public ServiceDirectoryConfig setCacheEnabled(boolean cacheEnable) {
        _set(SD_API_CACHE_ENABLED_PROPERTY, cacheEnable);
        return this;
    }

    public boolean isCacheEnabled() {
        return _checkEnable(SD_API_CACHE_ENABLED_PROPERTY);
    }

    public ServiceDirectoryConfig setHeartbeatEnabled(boolean heartbeatEnable) {
        _set(SD_API_HEARTBEAT_ENABLED_PROPERTY, heartbeatEnable);
        return this;
    }

    public boolean isHeartBeatEnabled() {
        return _checkEnable(SD_API_HEARTBEAT_ENABLED_PROPERTY);
    }

    private void _set(String key, Object value) {
        _apacheConfig.setProperty(key, value);
        if (this == GLOBE) {
            ServiceDirectory.LOGGER.warn("GLOBE ServiceDirectoryConfig changed! '{}' = '{}'", key, value);
        }
    }

    private String _get(String key) {
        if (_apacheConfig.containsKey(key)) {
            return _apacheConfig.getProperty(key).toString();
        } else {
            if (this == GLOBE) { // not found in GLOBE, throw ex
                throw new IllegalArgumentException("Unknown Service Directory configuration key '" + key + "'");

            }
            return GLOBE._get(key);
        }
    }

    private boolean _checkEnable(String key) {
        String value = _get(key); //not null guaranteed
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) { //protector of Boolean.valuesOf/parseBoolean
            return Boolean.valueOf(value);
        } else {
            throw new IllegalArgumentException("The Service Directory configuration key '" + key + "'=" + value + " is not boolean type");
        }
    }

    public ConfigurableServiceDirectoryManagerFactory build() {
        return new ConfigurableServiceDirectoryManagerFactory(this);
    }
}
