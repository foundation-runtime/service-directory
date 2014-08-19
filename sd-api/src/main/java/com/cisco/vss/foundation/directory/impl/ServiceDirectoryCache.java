/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.vss.foundation.directory.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Lists;

/**
 * The ServiceDirectory internal Cache.
 * 
 * It caches the Service as key and Object pair.
 * It is also used to cache ModelServiceInstance in LookupManagerImpl and 
 * ProvidedServiceInstance in the test framework.
 * 
 * @author zuxiang
 *
 */
public class ServiceDirectoryCache<K, V> {

	/**
	 * Internal map cache for Service.
	 */
	private Map<K, V> cache;
	
	/**
	 * Constructor.
	 */
	public ServiceDirectoryCache(){
		this.cache = new ConcurrentHashMap<K, V>();
	}
	
	/**
	 * Refresh the cache.
	 */
	public void refresh(){
		this.cache.clear();
	}
	
	/**
	 * Check whether the Service is cached.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @return
	 * 		true if the Service cached.
	 */
	public boolean isCached(K serviceName){
		return cache.containsKey(serviceName);
	}
	
	/**
	 * Put a service into cache.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param service
	 * 		the service.
	 */
	public void putService(K serviceName, V service){
		if(cache.containsKey(serviceName)){
			cache.remove(serviceName);
		}
		cache.put(serviceName, service);
	}
	
	/**
	 * Get the Service by name.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @return
	 * 		the service.
	 */
	public V getService(K serviceName){
		return cache.get(serviceName);
	}
	
	/**
	 * Get all Services in the cache.
	 * 
	 * @return
	 * 		the list of service.
	 */
	public List<V> getAllServices(){
		if(cache == null || cache.size() == 0){
			return Collections.emptyList();
		}
		
		List<V> list = new ArrayList<V>();
		for(V svc : cache.values()){
			list.add(svc);
		}
		return list;
	}
	
	/**
	 * Get all Services with instances together.
	 * 
	 * @return
	 * 		the List of Service with its ServiceInstances.
	 */
	public List<V> getAllServicesWithInstance(){
		if(cache == null || cache.size() == 0){
			return Collections.emptyList();
		}
		return Lists.newArrayList(cache.values());
	}
}
