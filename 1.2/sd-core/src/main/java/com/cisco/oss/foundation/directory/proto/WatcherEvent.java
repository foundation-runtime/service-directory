/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

import java.util.List;

import com.cisco.oss.foundation.directory.entity.EventType;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.WatchedService;

/**
 * The WatcherEvent that directory server push to SD API.
 * 
 * @author zuxiang
 *
 */
public class WatcherEvent extends Event {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The WatchedService list.
	 */
	private List<WatchedService> services;
	
	/**
	 * The touched ServiceInstance list.
	 */
	private List<ModelServiceInstance> serviceInstances;
	
	public WatcherEvent(){
		super(EventType.Watcher);
	}
	public WatcherEvent(List<WatchedService> services, List<ModelServiceInstance> instances) {
		super(EventType.Watcher);
		this.serviceInstances = instances;
		this.services = services;
	}

	public List<WatchedService> getServices() {
		return services;
	}
	public void setServices(List<WatchedService> services) {
		this.services = services;
	}
	public List<ModelServiceInstance> getServiceInstances() {
		return serviceInstances;
	}

	public void setServiceInstances(List<ModelServiceInstance> serviceInstances) {
		this.serviceInstances = serviceInstances;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("{");
		sb.append("services=");
		if(services == null){
			sb.append("null");
		}else{
			sb.append("[");
			for(WatchedService o : services){
				sb.append(o.toString()).append(",");
			}
			sb.append("]");
		}
		sb.append(",serviceInstances=");
		if(serviceInstances == null){
			sb.append("null");
		}else{
			sb.append("[");
			for(ModelServiceInstance o : serviceInstances){
				sb.append(o.toString()).append(",");
			}
			sb.append("]");
		}
		
		sb.append("}");
		return sb.toString();
	}

}
