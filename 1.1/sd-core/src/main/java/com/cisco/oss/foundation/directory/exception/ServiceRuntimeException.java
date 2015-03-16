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
 * It is the unchecked Exception used in Directory API.
 *
 * The LookupManager and RegistrationManager will capture this Exception and
 * convert it to the ServiceException. Exceptions that cannot be handled in 
 * ServiceDirectory should extends this exception, it will be thrown to the 
 * higher layer Application.
 *
 *
 */
public class ServiceRuntimeException extends RuntimeException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The ServiceDirectoryError.
     */
    private ServiceDirectoryError ec;

    /**
     * Constructor.
     *
     * @param ec
     *            the ServiceDirectoryError.
     */
    public ServiceRuntimeException(ServiceDirectoryError ec) {
        super(ec.getErrorMessage());
        this.ec = ec;
    }

    /**
     * Constructor.
     *
     * @param ec
     *            the ServiceDirectoryError.
     * @param message
     *            the error message.
     */
    public ServiceRuntimeException(ServiceDirectoryError ec, String message) {
        super(message);
        this.ec = ec;
    }

    /**
     * Constructor.
     *
     * @param ec
     *            the ServiceDirectoryError.
     * @param ex
     *            the root Exception.
     */
    public ServiceRuntimeException(ServiceDirectoryError ec, Exception ex) {
        super(ec.getErrorMessage(), ex);
        this.ec = ec;
    }

    /**
     * Constructor.
     *
     * @param ec
     *            the ServiceDirectoryError.
     * @param message
     *            the error message.
     * @param ex
     *            the root Exception.
     */
    public ServiceRuntimeException(ServiceDirectoryError ec, String message,
            Exception ex) {
        super(message, ex);
        this.ec = ec;
    }

    /**
     * Get the ServiceDirectoryError.
     *
     * @return the ServiceDirectoryError.
     */
    public ServiceDirectoryError getServiceDirectoryError() {
        return ec;
    }
}
