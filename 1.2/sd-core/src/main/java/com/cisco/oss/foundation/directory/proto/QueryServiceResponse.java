/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.proto;

import java.util.List;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;

/**
 * The Query Service response.
 *
 * @author zuxiang
 *
 */
public class QueryServiceResponse extends Response {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The ModelServiceInstance list.
     */
    private List<ModelServiceInstance> instances;
    public List<ModelServiceInstance> getInstances() {
        return instances;
    }

    public void setInstances(List<ModelServiceInstance> instances) {
        this.instances = instances;
    }

    public QueryServiceResponse(){

    }

    public QueryServiceResponse(List<ModelServiceInstance> instances){
        this.instances = instances;
    }
}
