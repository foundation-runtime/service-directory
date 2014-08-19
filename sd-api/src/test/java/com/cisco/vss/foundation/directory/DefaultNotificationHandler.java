package com.cisco.vss.foundation.directory;

import com.cisco.vss.foundation.directory.NotificationHandler;
import com.cisco.vss.foundation.directory.entity.ServiceInstance;
import com.cisco.vss.foundation.directory.exception.ServiceException;

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
