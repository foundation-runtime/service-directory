/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.impl.DirectoryServiceClient.WatcherRegistration;

/**
 * The Watcher manager.
 * 
 * It manages the Watcher of the Service in the SD API.
 * 
 * @author zuxiang
 *
 */
public class WatcherManager {
	private final static Logger LOGGER = LoggerFactory
			.getLogger(WatcherManager.class);
	
	/**
	 * The Watcher and Service mapping.
	 */
	public final Map<String, Set<Watcher>> watchers = new HashMap<String, Set<Watcher>>();
	
	/**
	 * Register a watcher.
	 * 
	 * @param watcherRegistration
	 * 		The Service WatcherRegistration.
	 */
	public void register(WatcherRegistration watcherRegistration){
		this.addWatcher(watcherRegistration.getName(), watcherRegistration.getWatcher());
	}
	
	/**
	 * Get all Watchers of the Service by name.
	 * 
	 * @param name
	 * 		the ServiceName.
	 * @return
	 * 		the Watcher List.
	 */
	public List<Watcher> getWatchers(String name){
		synchronized(watchers){
			
			if (watchers.containsKey(name)) {
				Set<Watcher> set = watchers.get(name);
				if(set.size() > 0){
					return new ArrayList<Watcher>(set);
				}
			} 
		}
		return null;
	}
	
	/**
	 * Add a Watcher for the Service.
	 * 
	 * @param name
	 * 		the ServiceName.
	 * @param watcher
	 * 		the Watcher.
	 */
	public void addWatcher(String name, Watcher watcher){
		if(LOGGER.isTraceEnabled()){
			LOGGER.trace("Add a watcher, name={}", name);
		}
		
		synchronized(watchers){
			
			if (watchers.containsKey(name)) {
				watchers.get(name).add(watcher);
			} else {
				Set<Watcher> set = new HashSet<Watcher>();
				set.add(watcher);
				watchers.put(name, set);
			}
		}
	}
	
	/**
	 * Delete a watcher from the Service.
	 * 
	 * @param name
	 * 		the Service Name.
	 * @param watcher
	 * 		the Watcher object.
	 * @return
	 * 		true if success.
	 */
	public boolean deleteWatcher(String name, Watcher watcher){
		if(LOGGER.isTraceEnabled()){
			LOGGER.trace("Delete a watcher, name={}", name);
		}
		synchronized(watchers){
			
			if (watchers.containsKey(name)) {
				return watchers.get(name).remove(watcher);
			} 
		}
		return false;
	}
	
	/**
	 * Validate whether the Watcher register to the Service.
	 * 
	 * @param name
	 * 		the Service Name.
	 * @param watcher
	 * 		the Watcher Object.
	 * @return
	 * 		return true if the Watcher registered to the Service.
	 */
	public boolean validateWatcher(String name, Watcher watcher){
		synchronized(watchers){
			if(watchers.containsKey(name)){
				return watchers.get(name).contains(watcher);
			}
			return false;
		}
	}
	
	/**
	 * Clean all watchers of the Service.
	 * 
	 * @param name
	 * 		the Service Name.
	 */
	public void cleanWatchers(String name){
		synchronized(watchers){
			if(watchers.containsKey(name)){
				watchers.get(name).clear();
			}
		}
	}
	
	/**
	 * Cleanup all Watchers in the WatcherManager.
	 * 
	 * Normally, it invoked when shutdown SD API.
	 */
	public void cleanup(){
		if(LOGGER.isTraceEnabled()){
			LOGGER.trace("Cleanup the watchers.");
		}
		synchronized(watchers){
			watchers.clear();
		}
	}
}