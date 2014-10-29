package com.cisco.oss.foundation.directory.entity;

import java.util.ArrayList;
import java.util.List;

public class WatchedMetadataKey {
	private ModelMetadataKey metadataKey;
	private List<ServiceInstanceEvent> serviceInstanceEvents;
	
	public WatchedMetadataKey(){};
	public WatchedMetadataKey(ModelMetadataKey metadataKey){
		this.metadataKey = metadataKey;
		serviceInstanceEvents = new ArrayList<ServiceInstanceEvent>();
	}
	
	public ModelMetadataKey getMetadataKey() {
		return metadataKey;
	}
	public void setMetadataKey(ModelMetadataKey metadataKey) {
		this.metadataKey = metadataKey;
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
		sb.append(",metadataKey=").append(metadataKey).append(",serviceInstanceEvents=[");
		if(serviceInstanceEvents != null && serviceInstanceEvents.size() > 0){
			for(ServiceInstanceEvent i : serviceInstanceEvents){
				sb.append(i).append(",");
			}
		}
		sb.append("]}");
		return sb.toString();
	}
}
