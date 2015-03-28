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

import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.impl.DirectoryServiceClient;
import com.cisco.oss.foundation.directory.lifecycle.Stoppable;

/**
 * The ServiceDirectoryManagerFactory is the factory to create LookupManager and RegistrationManager.
 *
 */
public interface ServiceDirectoryManagerFactory extends Stoppable{

    /**
     * Get the RegistrationManager.
     *
     * @return
     *         the RegistrationManager implementation instance.
     * @throws ServiceException
     */
    public RegistrationManager getRegistrationManager() throws ServiceException;

    /**
     * Get the LookupManager.
     *
     * @return
     *         the LookupManager implementation instance.
     * @throws ServiceException
     */
    public LookupManager getLookupManager() throws ServiceException;

    /**
     * Get the DirectoryServiceClient
     * @return DirectoryServiceClient
     */
    public DirectoryServiceClient getDirectoryServiceClient();
}
