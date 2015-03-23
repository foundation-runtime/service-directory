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

package com.cisco.oss.foundation.directory.config;

import org.apache.commons.configuration.Configuration;

/**
 * The SD API Configuration class.
 * 
 * It allows application to get a property value, set a new property value and
 * add ConfigurationListener in runtime.
 * 
 * 
 */
public class ServiceDirectoryConfig {

    private Configuration configuration;

    /**
     * Constructor of ServiceDirectoryConfig.
     * 
     * @param configuration
     *            the ServiceDirectory Configuration.
     */
    public ServiceDirectoryConfig(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Get the property object.
     * 
     * @param name
     *            property name.
     * @return property value object.
     */
    public Object getProperty(String name) {
        return this.configuration.getProperty(name);
    }

    /**
     * Get the property object as String.
     * 
     * @param name
     *            property name.
     * @return property value as String.
     */
    public String getString(String name) {
        return this.configuration.getString(name);
    }

    /**
     * Get the property object as String, or return defaultVal if property is
     * not defined.
     * 
     * @param name
     *            property name.
     * @param defaultVal
     *            default property value.
     * @return property value as String, return defaultVal if property is
     *         undefined.
     */
    public String getString(String name, String defaultVal) {
        return this.configuration.getString(name, defaultVal);
    }

    /**
     * Check whether the property is defined.
     * 
     * @param name
     *            property name.
     * @return true if property defined already.
     */
    public boolean containsProperty(String name) {
        return this.configuration.containsKey(name);
    }

    /**
     * Get the property object as Boolean.
     * 
     * @param name
     *            property name.
     * @return property value as boolean.
     */
    public boolean getBoolean(String name) {
        return this.configuration.getBoolean(name);
    }

    /**
     * Get the property object as Boolean, or return defaultVal if property is
     * not defined.
     * 
     * @param name
     *            property name.
     * @param defaultVal
     *            default property value.
     * @return property value as boolean, return defaultVal if property is
     *         undefined.
     */
    public boolean getBoolean(String name, boolean defaultVal) {
        return this.configuration.getBoolean(name, defaultVal);
    }

    /**
     * Get the property object as double.
     * 
     * @param name
     *            property name.
     * @return property value as double.
     */
    public double getDouble(String name) {
        return this.configuration.getDouble(name);
    }

    /**
     * Get the property object as double, or return defaultVal if property is
     * not defined.
     * 
     * @param name
     *            property name.
     * @param defaultVal
     *            default property value.
     * @return property value as double, return defaultVal if property is
     *         undefined.
     */
    public double getDouble(String name, double defaultVal) {
        return this.configuration.getDouble(name, defaultVal);
    }

    /**
     * Get the property object as float.
     * 
     * @param name
     *            property name.
     * @return property value as float.
     */
    public float getFloat(String name) {
        return this.configuration.getFloat(name);
    }

    /**
     * Get the property object as float, or return defaultVal if property is not
     * defined.
     * 
     * @param name
     *            property name.
     * @param defaultVal
     *            default property value.
     * @return property value as float, return defaultVal if property is
     *         undefined.
     */
    public float getFloat(String name, float defaultVal) {
        return this.configuration.getFloat(name, defaultVal);
    }

    /**
     * Get the property object as int.
     * 
     * @param name
     *            property name.
     * @return property value as int.
     */
    public int getInt(String name) {
        return this.configuration.getInt(name);
    }

    /**
     * Get the property object as int, or return defaultVal if property is not
     * defined.
     * 
     * @param name
     *            property name.
     * @param defaultVal
     *            default property value.
     * @return property value as int, return defaultVal if property is
     *         undefined.
     */
    public int getInt(String name, int defaultVal) {
        return this.configuration.getInt(name, defaultVal);
    }

    /**
     * Get the property object as long.
     * 
     * @param name
     *            property name.
     * @return property value as long.
     */
    public long getLong(String name) {
        return this.configuration.getLong(name);
    }

    /**
     * Get the property object as long, or return defaultVal if property is not
     * defined.
     * 
     * @param name
     *            property name.
     * @param defaultVal
     *            default property value.
     * @return property value as long, return defaultVal if property is
     *         undefined.
     */
    public long getLong(String name, long defaultVal) {
        return this.configuration.getLong(name, defaultVal);
    }

    /**
     * Set the property object.
     * 
     * @param name
     *            property name.
     * @param value
     *            property value object.
     */
    public void setProperty(String name, Object value) {
        this.configuration.setProperty(name, value);
    }

    // /**
    // * Add a ConfigurationListener.
    // *
    // * @param listener
    // * the Configuration listener.
    // */
    // public void addConfigurationListener(ConfigurationListener listener){
    // this.configuration.addConfigurationListener(listener);
    // }
    //
    // /**
    // * Remove the ConfigurationListener.
    // *
    // * @param listener
    // * the Configuration listener.
    // */
    // public void removeConfigurationListener(ConfigurationListener listener){
    // this.configuration.removeConfigurationListener(listener);
    // }
}
