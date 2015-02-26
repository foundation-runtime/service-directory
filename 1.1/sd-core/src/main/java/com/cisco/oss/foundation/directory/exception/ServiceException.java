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
 * The sub RuntimeException to wrap ServiceDirectory failure and errors.
 *
 * The ServiceException has the ExceptionCode to categorize certain error
 * types.
 *
 * @author zuxiang
 *
 */
public class ServiceException extends Exception {

    private static final long serialVersionUID = -3706093386454084825L;
    /**
     * The ExceptionCode.
     */
    private ServiceDirectoryError error;

    /**
     * Constructor from the ServiceRuntimeException.
     *
     * Transfer the ServiceRuntimeException to the checked ServiceException.
     *
     * @param exception
     *            the root ServiceRuntimeException.
     */
    public ServiceException(ServiceRuntimeException exception) {
        this(exception.getServiceDirectoryError(), exception);
    }

    /**
     * Constructor.
     *
     * @param error
     *            the ServiceDirectoryError.
     */
    public ServiceException(ServiceDirectoryError error) {
        super(error.getErrorMessage());
        this.error = error;
    }

    /**
     * Constructor.
     *
     * @param error
     *            the ServiceDirectoryError.
     * @param ex
     *            the root Exception.
     */
    public ServiceException(ServiceDirectoryError error, Exception ex) {
        super(ex);
        this.error = error;
    }

    /**
     * Constructor.
     *
     * @param error
     *            the ServiceDirectoryError.
     * @param message
     *            the error message.
     * @param ex
     *            the exception.
     */
    public ServiceException(ServiceDirectoryError error, String message,
            Exception ex) {
        super(message, ex);
        this.error = error;
    }

    /**
     * Get the ServiceDirectoryError.
     *
     * @return the ServiceDirectoryError.
     */
    public ServiceDirectoryError getServiceDirectoryError() {
        return error;
    }
}
