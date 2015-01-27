/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;

/**
 * Get ServiceIntance Response.
 *
 * @author zuxiang
 *
 */
public class GetServiceInstanceResponse extends Response {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The ModelServiceInstance.
     */
    private ModelServiceInstance serviceInstance;

    public GetServiceInstanceResponse(){

    }

    public GetServiceInstanceResponse(ModelServiceInstance serviceInstance){
        this.serviceInstance = serviceInstance;
    }

    public ModelServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ModelServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }


}
