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

import java.text.MessageFormat;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * The official ServiceDirectory ERROR.
 *
 * This error will be thrown to upper Application in Exception. Application can do
 * recovery based on the ExceptionCode if desired.
 *
 * @author zuxiang
 *
 */
public class ServiceDirectoryError {

    /**
     * The ExceptionCode.
     */
    private ErrorCode exceptionCode;

    /**
     * resource bundle replacement.
     */
    private Object[] params;

    /**
     * Default constructor for JSON serializer.
     */
    public ServiceDirectoryError() {

    }

    /**
     * Constructor.
     *
     * @param ec
     *            the ExceptionCode.
     * @param params
     *            the string holder parameters.
     */
    public ServiceDirectoryError(ErrorCode ec, Object... params) {
        this.params = params;
        this.exceptionCode = ec;
    }

    /**
     * Get the locale-specific error message.
     *
     * @return the error message String.
     */
    @JsonIgnore
    public String getErrorMessage() {
        if (params != null && params.length > 0) {
            return (MessageFormat.format(exceptionCode.getMessage(), params));
        } else {
            return (exceptionCode.getMessage());
        }
    }

    /**
     * Get the String holder parameters.
     *
     * @return the String holder parameters.
     */
    public Object[] getParams() {
        return this.params;
    }

    /**
     * Get the ExceptionCode of the error.
     *
     * @return the ExceptionCode.
     */
    public ErrorCode getExceptionCode() {
        return exceptionCode;
    }
}
