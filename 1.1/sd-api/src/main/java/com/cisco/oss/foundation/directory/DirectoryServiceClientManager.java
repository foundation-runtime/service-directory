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

/**
 * The Service Directory client manager.
 *
 *
 */
public interface DirectoryServiceClientManager {


    /**
     * Get the DirectoryServiceClient.
     *
     * It is thread safe.
     *
     * @return
     *         the directory server client.
     * @throws ServiceException
     *         Throw SERVICE_DIRECTORY_IS_SHUTDOWN error when ServiceDirectory shutdown.
     */
    public DirectoryServiceClient getDirectoryServiceClient() throws ServiceException;

}
