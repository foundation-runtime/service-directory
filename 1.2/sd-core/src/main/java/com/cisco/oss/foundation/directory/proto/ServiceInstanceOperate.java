package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;

public class ServiceInstanceOperate {

	private String serviceName;
	private String instanceId;
	private ModelServiceInstance serviceInstance;
	private OperateType type;
	
	public ServiceInstanceOperate(){
		
	}
	
	public ServiceInstanceOperate(String serviceName, String instanceId, ModelServiceInstance serviceInstance, OperateType type){
		this.serviceName = serviceName;
		this.instanceId = instanceId;
		this.serviceInstance = serviceInstance;
		this.type = type;
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



	public ModelServiceInstance getServiceInstance() {
		return serviceInstance;
	}



	public void setServiceInstance(ModelServiceInstance serviceInstance) {
		this.serviceInstance = serviceInstance;
	}



	public OperateType getType() {
		return type;
	}



	public void setType(OperateType type) {
		this.type = type;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("{");
		sb.append("serviceName=").append(serviceName).append(",instanceId=").append(instanceId).append(",type=").append(type);
		sb.append("}");
		return sb.toString();
	}

	public static enum OperateType{
		Add(0),
		Update(1),
		Delete(2),
		;
		private final int id;
		private OperateType(int id){
			this.id = id;
		}
		public int getId(){
			return this.id;
		}
	}
}
