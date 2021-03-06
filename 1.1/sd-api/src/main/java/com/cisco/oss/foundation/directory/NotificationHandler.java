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

import com.cisco.oss.foundation.directory.entity.ServiceInstance;

/**
 * The callback interface when the service instance status is changed.
 *
 *
 */
public interface NotificationHandler {
    /**
     * A service instance is available
     *
     *
     * @param service
     *          The ServiceInstance which the NotificationHandler is triggered for.
     *
     */
    public void serviceInstanceAvailable(ServiceInstance service);

    /**
     * A service instance is unavailable
     *
     *
     * @param service
     *             The ServiceInstance which the NotificationHandler is triggered for.
     *
     */
    public void serviceInstanceUnavailable(ServiceInstance service);

    /**
     * A service instance metadata is changed
     *
     *
     * @param service
     *             The ServiceInstance which the NotificationHandler is triggered for.
     */
    public void serviceInstanceChange(ServiceInstance service);

}
