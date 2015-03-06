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

import com.cisco.oss.foundation.directory.entity.ACL;

/**
 * Get ACL Response.
 *
 * @author zuxiang
 *
 */
public class GetACLResponse extends Response {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The ACL.
     */
    private ACL acl;
    public ACL getAcl() {
        return acl;
    }

    public void setAcl(ACL acl) {
        this.acl = acl;
    }

    public GetACLResponse(){

    }

    public GetACLResponse(ACL acl){
        this.acl = acl;
    }
}
