package com.cisco.oss.foundation.directory.entity;

import com.cisco.oss.foundation.directory.proto.ServiceInstanceOperate.OperateType;

public class ServiceInstanceEvent {
	private String serviceName;
	private String instanceId;
	private OperateType operateType;
	
	public ServiceInstanceEvent(){
		
	}
	
	public ServiceInstanceEvent(String serviceName, String instanceId, OperateType operateType){
		this.serviceName = serviceName;
		this.operateType = operateType;
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

	public OperateType getOperateType() {
		return operateType;
	}

	public void setOperateType(OperateType operateType) {
		this.operateType = operateType;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("{");
		sb.append("serviceName=").append(serviceName).append(",operateType=").append(operateType).append(",instanceId=").append(instanceId);
		sb.append("}");
		return sb.toString();
	}
}
