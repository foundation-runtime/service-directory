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

import com.cisco.oss.foundation.directory.config.ServiceDirectoryConfig;
import com.cisco.oss.foundation.directory.exception.ServiceException;

/**
 * The ServiceDirectoryManagerFactory to initialize LookupManager and RegistrationManager.
 *
 * Two default ServiceDirectoryManagerFactory implementations are supplied:
 * DefaultServiceDirectoryManagerFactory for the production use.
 * TestServiceDirectoryManagerFactory for the integration test purpose.
 *
 *
 */
public interface ServiceDirectoryManagerFactory{

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
     * Initialize the factory with the DirectoryServiceClientManager.
     * The method is considered to be removed in the future. if reinit() method in ServiceDirectory has been removed.
     * {@link ServiceDirectory#reinitServiceDirectoryManagerFactory(ServiceDirectoryManagerFactory)}
     *
     * @param manager
     *         The DirectoryServiceClientManager.
     */
    @Deprecated
    public void initialize(DirectoryServiceClientManager manager);

    /**
     * Set the ServiceDirectoryConfig for factory.
     *
     * @param config
     *         the ServiceDirectory Configuration.
     */
    public void setServiceDirectoryConfig(ServiceDirectoryConfig config);

    /**
     * Get the DirectoryServiceClientManager
     * @return DirectoryServiceClientManager
     */
    public DirectoryServiceClientManager getDirectoryServiceClientManager();
}
