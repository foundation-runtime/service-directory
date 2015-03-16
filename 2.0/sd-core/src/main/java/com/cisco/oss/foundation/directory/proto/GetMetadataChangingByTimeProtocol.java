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
package com.cisco.oss.foundation.directory.proto;

import java.util.Map;

import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;

/**
 * Get MetadataKey Changing by Time Protocol.
 *
 *
 */
public class GetMetadataChangingByTimeProtocol extends Protocol {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The ModelMetadataKey map.
     */
    private Map<String, ModelMetadataKey> metadatas;

    public GetMetadataChangingByTimeProtocol(){

    }

    public GetMetadataChangingByTimeProtocol(Map<String, ModelMetadataKey> metadatas){
        this.metadatas = metadatas;
    }

    public Map<String, ModelMetadataKey> getMetadatas() {
        return metadatas;
    }

    public void setMetadatas(Map<String, ModelMetadataKey> metadatas) {
        this.metadatas = metadatas;
    }
}
