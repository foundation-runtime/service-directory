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
package com.cisco.oss.foundation.directory.utils;

/**
 * The wrapper to the HttpResponse.
 *
 * @author zuxiang
 *
 */
public class HttpResponse {
    /**
     * The HttpCode
     */
    private int httpCode;

    /**
     * The response body String.
     */
    private String retBody;

    /**
     * Constructor.
     *
     * @param httpCode
     *         the HttpCode.
     * @param retBody
     *         the Http body String.
     */
    public HttpResponse(int httpCode, String retBody) {
        super();
        this.httpCode = httpCode;
        this.retBody = retBody;
    }

    /**
     * Get HttpCode.
     *
     * @return
     *         the HttpCode.
     */
    public int getHttpCode() {
        return httpCode;
    }

    /**
     * Set HttpCode.
     *
     * @param httpCode
     *         the HttpCode.
     */
    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    /**
     * Get the Http body String.
     * @return
     *         the Http body String.
     */
    public String getRetBody() {
        return retBody;
    }

    /**
     * Set the Http body String.
     *
     * @param retBody
     *         the Http body String.
     */
    public void setRetBody(String retBody) {
        this.retBody = retBody;
    }

}
