package com.cisco.oss.foundation.directory.entity;

import java.util.ArrayList;
import java.util.List;


public class WatchedService{
	private ModelService service;
	private List<ServiceInstanceEvent> serviceInstanceEvents;
	
	public WatchedService(){};
	public WatchedService(ModelService service){
		this.service = service;
		serviceInstanceEvents = new ArrayList<ServiceInstanceEvent>();
	}
	
	public ModelService getService() {
		return service;
	}
	public void setService(ModelService service) {
		this.service = service;
	}
	public List<ServiceInstanceEvent> getServiceInstanceEvents() {
		return serviceInstanceEvents;
	}
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
