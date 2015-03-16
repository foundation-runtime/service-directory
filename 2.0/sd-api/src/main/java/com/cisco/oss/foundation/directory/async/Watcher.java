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
package com.cisco.oss.foundation.directory.async;

import com.cisco.oss.foundation.directory.proto.ServiceInstanceOperate;

/**
 * The Service Watcher interface.
 *
 * We can register the watcher for a Service. Then SD API can invoke the
 * Watcher for the Service update.
 *
 *
 */
public interface Watcher {

    /**
     * The method invoked for the Watcher for the Service update.
     *
     * @param name
     *         the ServiceName.
     * @param operate
     *         the actual ServiceInstance update Operation of the Service.
     */
    public void process(String name, ServiceInstanceOperate operate);
}
