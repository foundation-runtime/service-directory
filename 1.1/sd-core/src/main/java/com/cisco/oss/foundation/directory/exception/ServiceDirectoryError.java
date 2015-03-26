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
import com.fasterxml.jackson.annotation.JsonIgnore;
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
     * The error message arguments holder
     * compatible with 1.1.0.5 api, so that we don't change the naming
     */
    private final Object[] params;

    /**
     * Constructor.
     *
     * @param ec
     *            the ExceptionCode.
     * @param errMsgArgs
     *            the array of message parameters.
     */
    @JsonCreator
    public ServiceDirectoryError(@JsonProperty("exceptionCode")ErrorCode ec, @JsonProperty("params")Object... errMsgArgs) {
        this.exceptionCode = ec;
        this.params = errMsgArgs;
    }

    /**
     * Get the error message.
     * The message is json ignored, so that no serialize/deserialize.
     * @return the error message String.
     */
    @JsonIgnore
    public String getErrorMessage() {
        return (params.length == 0) ? exceptionCode.getMessageTemplate() :
                String.format(exceptionCode.getMessageTemplate(), params);
    }

    /**
     * Get the ErrorCode of the error.
     *
     * @return the ErrorCode.
     */
    public ErrorCode getExceptionCode() {
        return exceptionCode;
    }

    /**
     * Get the String holder parameters.
     *
     * @return
     * 		the String holder parameters.
     */
    public Object[] getParams(){
        return this.params;
    }
}
