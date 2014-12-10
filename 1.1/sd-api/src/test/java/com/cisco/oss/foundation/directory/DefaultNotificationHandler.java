package com.cisco.oss.foundation.directory;

import com.cisco.oss.foundation.directory.entity.ServiceInstance;

public class DefaultNotificationHandler implements NotificationHandler {

	@Override
	public void serviceInstanceAvailable(ServiceInstance service) {
		System.out.println("[" + service.getServiceName()+"] - "+service.getInstanceId() +": created!");
	}

	@Override
	public void serviceInstanceUnavailable(ServiceInstance service) {
		System.out.println("[" + service.getServiceName()+"] - "+service.getInstanceId() +": unavailable!");
	}

	@Override
	public void serviceInstanceChange(ServiceInstance service) {
		System.out.println("[" + service.getServiceName()+"] - "+service.getInstanceId() +": change!");
	}
	
}
