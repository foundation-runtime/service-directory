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
package com.cisco.oss.foundation.directory.async;

import java.util.List;
import java.util.Map;

import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstanceToken;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.proto.AttachSessionResponse.ItemResult;
import com.cisco.oss.foundation.directory.proto.Response;

/**
 * The Callback interface for DirectoryRequest.
 *
 * When submit a request in DirecotryServiceClient, we can register a Callback.
 * SD API will invoke the Callback for request finished.
 *
 * @author zuxiang
 *
 */
public interface Callback {

    /**
     * The Register ServiceInstance request Callback.
     *
     * @author zuxiang
     *
     */
    public interface RegistrationCallback extends Callback{

        /**
         * The method invoke when Register ServiceInstance finished.
         *
         * @param result
         *         the request result, true for success.
         * @param error
         *         the ErrorCode of the request.
         * @param ctx
         *         the Context object.
         */
        public void call(boolean result,  ErrorCode error, Object ctx);
    }

    /**
     * The Attach ServiceInstance to Session request Callback.
     *
     * @author zuxiang
     *
     */
    public interface AttachSessionCallback extends Callback{

        /**
         * The method invoke when Attach ServiceInstance to Session request finished.
         *
         * @param result
         *         the request result, true for success.
         * @param items
         *         the result list.
         * @param error
         *         the ErrorCode of the request.
         * @param ctx
         *         the Context object.
         */
        public void call(boolean result, Map<ServiceInstanceToken, ItemResult> items, ErrorCode error, Object ctx);
    }

    /**
     * The Get Service request Callback.
     *
     * @author zuxiang
     *
     */
    public interface GetServiceCallback extends Callback{

        /**
         * The method invoked when Get Service request finished.
         *
         * @param result
         *         the request result, true for success.
         * @param service
         *         the ModelService.
         * @param error
         *         the ErrorCode of the request.
         * @param ctx
         *         the Context object.
         */
        public void call(boolean result, ModelService service, ErrorCode error, Object ctx);
    }

    /**
     * The Get Metadata request Callback.
     *
     */
    public interface GetMetadataCallback extends Callback{

        /**
         * The method invoked when Get Metadata request finished.
         *
         * @param result
         *         the request result, true for success.
         * @param key
         *         the ModelMetadataKey.
         * @param error
         *         the ErrorCode of the request.
         * @param ctx
         *         the Context object.
         */
        public void call(boolean result, ModelMetadataKey key, ErrorCode error, Object ctx);
    }

    /**
     * The general Protocol Callback.
     *
     * @author zuxiang
     *
     */
    public interface ProtocolCallback extends Callback {

        /**
         * The method invoked when the Protocol finished.
         *
         * @param result
         *         the request result, true for success.
         * @param response
         *         the Protocol Response.
         * @param error
         *         the ErrorCode of the request.
         * @param ctx
         *         the Context object.
         */
        public void call(boolean result, Response response, ErrorCode error, Object ctx);
    }

    /**
     * The query Service request Callback.
     *
     * @author zuxiang
     *
     */
    public interface QueryServiceCallback extends Callback {

        /**
         * The method invoked when QueryService request finished.
         *
         * @param result
         *         the request result, true for success.
         * @param instances
         *         the ModelServiceInstance list.
         * @param error
         *         the ErrorCode of the request.
         * @param ctx
         *         the Context object.
         */
        public void call(boolean result, List<ModelServiceInstance> instances, ErrorCode error, Object ctx);
    }
}
