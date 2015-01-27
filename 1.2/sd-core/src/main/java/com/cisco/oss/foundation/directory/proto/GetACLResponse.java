/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
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
