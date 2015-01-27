/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.proto;

import java.util.Map;

/**
 * Get Service Change by version Protocol.
 *
 * @author zuxiang
 *
 */
public class GetServiceChangingByVersionProtocol extends Protocol {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * the ModelService version map.
     */
    private Map<String, Long> services;

    public GetServiceChangingByVersionProtocol(){

    }

    public GetServiceChangingByVersionProtocol(Map<String, Long> services){
        this.services = services;
    }

    public Map<String, Long> getServices() {
        return services;
    }

    public void setServices(Map<String, Long> services) {
        this.services = services;
    }


}
