package com.cisco.oss.foundation.directory.entity;

public class ServiceInstanceToken {

	private String serviceName;
	private String instanceId;
	
	public ServiceInstanceToken(){
		
	}
	
	public ServiceInstanceToken(String serviceName, String instanceId){
		this.serviceName = serviceName;
		this.instanceId = instanceId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	@Override
	public boolean equals(Object object){
		if(object != null && object instanceof ServiceInstanceToken){
			if(object == this){
				return true;
			}
			
			ServiceInstanceToken target = (ServiceInstanceToken)object;
			return (serviceName.equals(target.serviceName) && instanceId.equals(target.instanceId));
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		int i = 37;
		i = 31 * i + serviceName == null ? 0 : serviceName.hashCode();
		i = 31 * i + instanceId == null ? 0 : instanceId.hashCode();
		return i;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("{");
		sb.append("serviceName=").append(serviceName).append(",instanceId=").append(instanceId).append("}");
		return sb.toString();
	}
	
	
}
