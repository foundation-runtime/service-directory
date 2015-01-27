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

import java.util.List;
import java.util.Map;

/**
 * Query Service Protocol.
 *
 * @author zuxiang
 *
 */
public class QueryServiceProtocol extends Protocol {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The QueryCommand list.
     */
    private List<QueryCommand> queryCommands;

    /**
     * The extra metadata info.
     */
    private Map<String, String> metadata;

    public QueryServiceProtocol(){

    }

    public QueryServiceProtocol(List<QueryCommand> queryCommands){
        this.queryCommands = queryCommands;
    }

    public List<QueryCommand> getQueryCommands() {
        return queryCommands;
    }

    public void setQueryCommands(List<QueryCommand> queryCommands) {
        this.queryCommands = queryCommands;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }



    public static class QueryCommand{
        private String name;
        private String operate;
        private List<String> values;
        public QueryCommand(){

        }

        public QueryCommand(String name, String operate, List<String> values){
            this.name = name;
            this.operate = operate;
            this.values = values;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getOperate() {
            return operate;
        }
        public void setOperate(String operate) {
            this.operate = operate;
        }
        public List<String> getValues() {
            return values;
        }
        public void setValues(List<String> values) {
            this.values = values;
        }
    }

}
