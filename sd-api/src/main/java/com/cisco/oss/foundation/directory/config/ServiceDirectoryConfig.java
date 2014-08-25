/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.config;

import org.apache.commons.configuration.Configuration;

/**
 * The SD API Configuration class.
 * 
 * It allows application to get the property value, set a new property value and add ConfigurationListener in runtime.
 * 
 * @author zuxiang
 *
 */
public class ServiceDirectoryConfig {
	
	private Configuration configuration;
	
	/**
	 * Constructor of ServiceDirectoryConfig.
	 * 
	 * @param configuration
	 * 		the ServiceDirectory Configuration.
	 */
	public ServiceDirectoryConfig(Configuration configuration){
		this.configuration = configuration;
	}
	
	/**
	 * Get the property object.
	 * 
	 * @param name
	 * 		property name.
	 * @return
	 * 		property value object.
	 */
	public Object getProperty(String name){
		return this.configuration.getProperty(name);
	}
	
	/**
	 * Get the property object as String.
	 * 
	 * @param name
	 * 		property name.
	 * @return
	 * 		property value as String.
	 */
	public String getString(String name){
		return this.configuration.getString(name);
	}
	
	/**
	 * Get the property object as String, or return defaultVal if property is not defined.
	 * 
	 * @param name
	 * 		property name.
	 * @param defaultVal
	 * 		default property value.
	 * @return
	 * 		property value as String, return defaultVal if property is undefined.
	 */
	public String getString(String name, String defaultVal){
		if(this.configuration.containsKey(name)){
			return this.configuration.getString(name);
		} else {
			return defaultVal;
		}
	}
	
	/**
	 * Check whether the property is defined.
	 * 
	 * @param name
	 * 		property name.
	 * @return
	 * 		true if property defined already.
	 */
	public boolean containsProperty(String name){
		return this.configuration.containsKey(name);
	}
	
	/**
	 * Get the property object as Boolean.
	 * 
	 * @param name
	 * 		property name.
	 * @return
	 * 		property value as boolean.
	 */
	public boolean getBoolean(String name){
		return this.configuration.getBoolean(name);
	}
	
	/**
	 * Get the property object as Boolean, or return defaultVal if property is not defined.
	 * 
	 * @param name
	 * 		property name.
	 * @param defaultVal
	 * 		default property value.
	 * @return
	 * 		property value as boolean, return defaultVal if property is undefined.
	 */
	public boolean getBoolean(String name, boolean defaultVal){
		if(this.configuration.containsKey(name)){
			return this.configuration.getBoolean(name);
		} else {
			return defaultVal;
		}
	}
	
	/**
	 * Get the property object as double.
	 * 
	 * @param name
	 * 		property name.
	 * @return
	 * 		property value as double.
	 */
	public double getDouble(String name){
		return this.configuration.getDouble(name);
	}
	
	/**
	 * Get the property object as double, or return defaultVal if property is not defined.
	 * 
	 * @param name
	 * 		property name.
	 * @param defaultVal
	 * 		default property value.
	 * @return
	 * 		property value as double, return defaultVal if property is undefined.
	 */
	public double getDouble(String name, double defaultVal){
		if(this.configuration.containsKey(name)){
			return this.configuration.getDouble(name);
		} else {
			return defaultVal;
		}
	}
	
	/**
	 * Get the property object as float.
	 * 
	 * @param name
	 * 		property name.
	 * @return
	 * 		property value as float.
	 */
	public float getFloat(String name){
		return this.configuration.getFloat(name);
	}
	
	/**
	 * Get the property object as float, or return defaultVal if property is not defined.
	 * 
	 * @param name
	 * 		property name.
	 * @param defaultVal
	 * 		default property value.
	 * @return
	 * 		property value as float, return defaultVal if property is undefined.
	 */
	public float getFloat(String name, float defaultVal){
		if(this.configuration.containsKey(name)){
			return this.configuration.getFloat(name);
		} else {
			return defaultVal;
		}
	}
	
	/**
	 * Get the property object as int.
	 * 
	 * @param name
	 * 		property name.
	 * @return
	 * 		property value as int.
	 */
	public int getInt(String name){
		return this.configuration.getInt(name);
	}
	
	/**
	 * Get the property object as int, or return defaultVal if property is not defined.
	 * 
	 * @param name
	 * 		property name.
	 * @param defaultVal
	 * 		default property value.
	 * @return
	 * 		property value as int, return defaultVal if property is undefined.
	 */
	public int getInt(String name, int defaultVal){
		if(this.configuration.containsKey(name)){
			return this.configuration.getInt(name);
		} else {
			return defaultVal;
		}
	}
	
	/**
	 * Get the property object as long.
	 * 
	 * @param name
	 * 		property name.
	 * @return
	 * 		property value as long.
	 */
	public long getLong(String name){
		return this.configuration.getLong(name);
	}
	
	/**
	 * Get the property object as long, or return defaultVal if property is not defined.
	 * 
	 * @param name
	 * 		property name.
	 * @param defaultVal
	 * 		default property value.
	 * @return
	 * 		property value as long, return defaultVal if property is undefined.
	 */
	public long getLong(String name, long defaultVal){
		if(this.configuration.containsKey(name)){
			return this.configuration.getLong(name);
		} else {
			return defaultVal;
		}
	}
	
	/**
	 * Set the property object.
	 * 
	 * @param name
	 * 		property name.
	 * @param value
	 * 		property value object.
	 */
	public void setProperty(String name, Object value){
		this.configuration.setProperty(name, value);
	}
	
//	/**
//	 * Add a ConfigurationListener.
//	 * 
//	 * @param listener
//	 * 		the Configuration listener.
//	 */
//	public void addConfigurationListener(ConfigurationListener listener){
//		this.configuration.addConfigurationListener(listener);
//	}
//	
//	/**
//	 * Remove the ConfigurationListener.
//	 * 
//	 * @param listener
//	 * 		the Configuration listener.
//	 */
//	public void removeConfigurationListener(ConfigurationListener listener){
//		this.configuration.removeConfigurationListener(listener);
//	}
}
