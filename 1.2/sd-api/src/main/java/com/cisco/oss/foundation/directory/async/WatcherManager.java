package com.cisco.oss.foundation.directory.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.entity.WatcherType;
import com.cisco.oss.foundation.directory.impl.DirectoryServiceClient.WatcherRegistration;
import com.cisco.oss.foundation.directory.utils.ServiceInstanceOperateUtils;

public class WatcherManager {
	private final static Logger LOGGER = LoggerFactory
			.getLogger(WatcherManager.class);
	
	public final Map<String, Set<Watcher>> watchers = new HashMap<String, Set<Watcher>>();
	
	public void register(WatcherRegistration watcherRegistration){
		this.addWatcher(watcherRegistration.getName(), watcherRegistration.getWatcherType(), watcherRegistration.getWatcher());
	}
	
	public List<Watcher> getWatchers(String name, WatcherType watcherType){
		String path = ServiceInstanceOperateUtils.getPathByWatcherType(name, watcherType);
		synchronized(watchers){
			
			if (watchers.containsKey(path)) {
				Set<Watcher> set = watchers.get(path);
				if(set.size() > 0){
					return new ArrayList<Watcher>(set);
				}
			} 
		}
		return null;
	}
	
	public void addWatcher(String name, WatcherType watcherType, Watcher watcher){
		if(LOGGER.isTraceEnabled()){
			LOGGER.trace("Add a watcher, type={}, name={}", watcherType, name);
		}
		
		String path = ServiceInstanceOperateUtils.getPathByWatcherType(name, watcherType);
		synchronized(watchers){
			
			if (watchers.containsKey(path)) {
				watchers.get(path).add(watcher);
			} else {
				Set<Watcher> set = new HashSet<Watcher>();
				set.add(watcher);
				watchers.put(path, set);
			}
		}
	}
	
	public boolean deleteWatcher(String name, WatcherType watcherType, Watcher watcher){
		if(LOGGER.isTraceEnabled()){
			LOGGER.trace("Delete a watcher, type={}, name={}", watcherType, name);
		}
		String path = ServiceInstanceOperateUtils.getPathByWatcherType(name, watcherType);
		synchronized(watchers){
			
			if (watchers.containsKey(path)) {
				return watchers.get(path).remove(watcher);
			} 
		}
		return false;
	}
	
	public boolean validateWatcher(String name, WatcherType watcherType, Watcher watcher){
		String path = ServiceInstanceOperateUtils.getPathByWatcherType(name, watcherType);
		synchronized(watchers){
			if(watchers.containsKey(path)){
				return watchers.get(path).contains(watcher);
			}
			return false;
		}
	}
	
	public void cleanWatchers(String name, WatcherType watcherType){
		String path = ServiceInstanceOperateUtils.getPathByWatcherType(name, watcherType);
		synchronized(watchers){
			if(watchers.containsKey(path)){
				watchers.get(path).clear();
			}
		}
	}
	
	public void cleanup(){
		if(LOGGER.isTraceEnabled()){
			LOGGER.trace("Cleanup the watchers.");
		}
		synchronized(watchers){
			watchers.clear();
		}
	}
}