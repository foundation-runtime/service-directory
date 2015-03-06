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




package com.cisco.oss.foundation.directory.exception;

/**
 * It is the unchecked Exception used in DirectoryServiceClient
 *
 */
public class DirectoryServerClientException extends ServiceRuntimeException{
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param ec
     *         the ServiceDirectoryError.
     */
    public DirectoryServerClientException(ServiceDirectoryError ec){
        super(ec);
    }

    /**
     * Constructor.
     *
     * @param ec
     *         the ServiceDirectoryError.
     * @param ex
     *         the root Exception.
     */
    public DirectoryServerClientException(ServiceDirectoryError ec, Exception ex) {
        super(ec, ex);
    }



    /**
     * Constructor.
     *
     * @param ec
     *         the ServiceDirectoryError.
     * @param message
     *         the error message.
     */
    public DirectoryServerClientException(ServiceDirectoryError ec, String message) {
        super(ec, message);
    }

    /**
     * Constructor.
     *
     * @param ec
     *         the ServiceDirectoryError.
     * @param message
     *         the error message.
     * @param ex
     *         the root Exception.
     */
    public DirectoryServerClientException(ServiceDirectoryError ec, String message, Exception ex) {
        super(ec, message, ex);
    }

}
