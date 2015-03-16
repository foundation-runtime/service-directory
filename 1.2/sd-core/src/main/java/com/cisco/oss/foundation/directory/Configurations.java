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

import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.configuration.ConfigurationFactory;

/**
 * The ServiceDirectory Configuration component.
 *
 * It loads the common configuration and gets the configuration properties.
 *
 *
 */
public class Configurations {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(Configurations.class);

    /**
     * The Apache Configuration.
     */
    private static Configuration configurations;

    /**
     * Constructor.
     */
    public Configurations() {
    }

    /**
     * Get the Configuration.
     *
     * @return the Apache Configuration.
     */
    public static Configuration getConfiguration() {
        if (configurations == null) {
            configurations = ConfigurationFactory.getConfiguration();
            LOGGER.info("Initialized the Configurations.");
        }
        return configurations;
    }

    /**
     * Get the property object.
     *
     * @param name
     *            property name.
     * @return property value object.
     */
    public static Object getProperty(String name) {
        return getConfiguration().getProperty(name);
    }

    /**
     * Get the property object as String.
     *
     * @param name
     *            property name.
     * @return property value as String.
     */
    public static String getString(String name) {
        return getConfiguration().getString(name);
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
    public static String getString(String name, String defaultVal) {
        if (getConfiguration().containsKey(name)) {
            return getConfiguration().getString(name);
        } else {
            return defaultVal;
        }
    }

    /**
     * Check whether the property is defined.
     *
     * @param name
     *            property name.
     * @return true if property defined already.
     */
    public static boolean containsProperty(String name) {
        return getConfiguration().containsKey(name);
    }

    /**
     * Get the property object as Boolean.
     *
     * @param name
     *            property name.
     * @return property value as boolean.
     */
    public static boolean getBoolean(String name) {
        return getConfiguration().getBoolean(name);
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
    public static boolean getBoolean(String name, boolean defaultVal) {
        if (getConfiguration().containsKey(name)) {
            return getConfiguration().getBoolean(name);
        } else {
            return defaultVal;
        }
    }

    /**
     * Get the property object as double.
     *
     * @param name
     *            property name.
     * @return property value as double.
     */
    public static double getDouble(String name) {
        return getConfiguration().getDouble(name);
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
    public static double getDouble(String name, double defaultVal) {
        if (getConfiguration().containsKey(name)) {
            return getConfiguration().getDouble(name);
        } else {
            return defaultVal;
        }
    }

    /**
     * Get the property object as float.
     *
     * @param name
     *            property name.
     * @return property value as float.
     */
    public static float getFloat(String name) {
        return getConfiguration().getFloat(name);
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
    public static float getFloat(String name, float defaultVal) {
        if (getConfiguration().containsKey(name)) {
            return getConfiguration().getFloat(name);
        } else {
            return defaultVal;
        }
    }

    /**
     * Get the property object as int.
     *
     * @param name
     *            property name.
     * @return property value as int.
     */
    public static int getInt(String name) {
        return getConfiguration().getInt(name);
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
    public static int getInt(String name, int defaultVal) {
        if (getConfiguration().containsKey(name)) {
            return getConfiguration().getInt(name);
        } else {
            return defaultVal;
        }
    }

    /**
     * Get the property object as long.
     *
     * @param name
     *            property name.
     * @return property value as long.
     */
    public static long getLong(String name) {
        return getConfiguration().getLong(name);
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
    public static long getLong(String name, long defaultVal) {
        if (getConfiguration().containsKey(name)) {
            return getConfiguration().getLong(name);
        } else {
            return defaultVal;
        }
    }

    /**
     * Get the property object as String Array.
     *
     * @param name
     *            property name.
     * @return property value as String Array.
     */
    public static String[] getStringArray(String name) {
        if (getConfiguration().containsKey(name)) {
            return getConfiguration().getStringArray(name);
        }
        return null;
    }

    /**
     * Get the property object as List.
     *
     * @param name
     *            property name.
     * @return property value as List.
     */
    public static List<Object> getList(String name) {
        if (getConfiguration().containsKey(name)) {
            return getConfiguration().getList(name);
        }
        return null;
    }

    /**
     * Set the property object.
     *
     * @param name
     *            property name.
     * @param value
     *            property value object.
     */
    public static void setProperty(String name, Object value) {
        getConfiguration().setProperty(name, value);
    }

    /**
     * Load the customer Properties to Configurations.
     *
     * @param props
     *            the customer Properties.
     */
    public static void loadCustomerProperties(Properties props) {
        for (Entry<Object, Object> entry : props.entrySet()) {
            setProperty((String) entry.getKey(), entry.getValue());
        }
    }
}
