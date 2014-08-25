package com.cisco.oss.foundation.directory;

import com.cisco.oss.foundation.directory.NotificationHandler;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.exception.ServiceException;

public class DefaultNotificationHandler implements NotificationHandler {

	@Override
	public void serviceInstanceAvailable(ServiceInstance service) throws ServiceException {
		System.out.println("[" + service.getServiceName()+"] - "+service.getInstanceId() +": created!");
	}

	@Override
	public void serviceInstanceUnavailable(ServiceInstance service) throws ServiceException {
		System.out.println("[" + service.getServiceName()+"] - "+service.getInstanceId() +": unavailable!");
	}
	
}
