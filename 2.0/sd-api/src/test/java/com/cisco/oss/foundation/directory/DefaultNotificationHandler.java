/**
 * Copyright 2014 Cisco Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cisco.oss.foundation.directory;

import com.cisco.oss.foundation.directory.NotificationHandler;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.exception.ServiceException;

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
