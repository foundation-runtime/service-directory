/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.ModelService;

/**
 * Get Service Response.
 *
 * @author zuxiang
 *
 */
public class GetServiceResponse extends Response {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The ModelService.
     */
    private ModelService service;

    public GetServiceResponse(){}

    public GetServiceResponse(ModelService service){
        this.service = service;
    }

    public ModelService getService() {
        return service;
    }

    public void setService(ModelService service) {
        this.service = service;
    }



}
