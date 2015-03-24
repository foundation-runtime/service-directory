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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Error class acts as a transfer object, so that exception(server-side) -> SDError -> json
 * serializer/deserializer -> SDError (client side) -> exception (client side)
 * the class is immutable;
 */
public class ServiceDirectoryError {

    /**
     * The ExceptionCode.
     */
    private final ErrorCode exceptionCode;

    /**
     * The error message
     */
    private final String errMsg;

    /**
     * Constructor.
     *
     * @param ec
     *            the ExceptionCode.
     * @param errMsg
     *            the error message.
     */
    @JsonCreator
    public ServiceDirectoryError(@JsonProperty("exceptionCode")ErrorCode ec, @JsonProperty("errorMessage")String errMsg) {
        this.exceptionCode = ec;
        this.errMsg = errMsg;
    }

    public ServiceDirectoryError(ErrorCode ec) {
        this.exceptionCode = ec;
        this.errMsg = ec.getMessageTemplate();
    }
    /**
     * Get the error message.
     *
     * @return the error message String.
     */
    public String getErrorMessage() {
       return this.errMsg;
    }

    /**
     * Get the ErrorCode of the error.
     *
     * @return the ErrorCode.
     */
    public ErrorCode getExceptionCode() {
        return exceptionCode;
    }
}
