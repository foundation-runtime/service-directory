/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * The WatchedService for the ServiceInstanceEvent.
 * 
 * @author zuxiang
 *
 */
public class WatchedService{
	/**
	 * The ModelService of the Service.
	 */
	private ModelService service;
	
	/**
	 * The ServiceInstanceEvent list.
	 */
	private List<ServiceInstanceEvent> serviceInstanceEvents;
	
	/**
	 * Constructor.
	 */
	public WatchedService(){};
	
	/**
	 * Constructor.
	 * 
	 * @param service
	 * 		the ModelService object.
	 */
	public WatchedService(ModelService service){
		this.service = service;
		serviceInstanceEvents = new ArrayList<ServiceInstanceEvent>();
	}
	
	/**
	 * get the ModelService.
	 * 
	 * @return
	 * 		the ModelService.
	 */
	public ModelService getService() {
		return service;
	}
	
	/**
	 * Set the ModelService.
	 * 
	 * @param service
	 * 		the ModelService
	 */
	public void setService(ModelService service) {
		this.service = service;
	}
	
	/**
	 * Get the ServiceInstanceEvent list.
	 * @return
	 * 		the ServiceInstanceEvent list.
	 */
	public List<ServiceInstanceEvent> getServiceInstanceEvents() {
		return serviceInstanceEvents;
	}
	
	/**
	 * Set the ServiceInstanceEvent list.
	 * 
	 * @param serviceInstanceEvents
	 * 		the ServiceInstanceEvent list.
	 */
	public void setServiceInstanceEvents(List<ServiceInstanceEvent> serviceInstanceEvents) {
		this.serviceInstanceEvents = serviceInstanceEvents;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("{");
		sb.append(",service=").append(service.getName()).append(",serviceInstanceEvents=[");
		if(serviceInstanceEvents != null && serviceInstanceEvents.size() > 0){
			for(ServiceInstanceEvent i : serviceInstanceEvents){
				sb.append(i).append(",");
			}
		}
		sb.append("]}");
		return sb.toString();
	}
}
