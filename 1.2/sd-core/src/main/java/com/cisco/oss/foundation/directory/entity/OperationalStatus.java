/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.entity;

/**
 * OperationalStatus of the ServiceInstance.
 *
 * @author zuxiang
 *
 */
public enum OperationalStatus {

    /**
     * UP will be consumed in Service Consumer.
     */
    UP("UP", 1),

    /**
     * The dead ServiceInstance, will be deleted automatically after a while.
     */
    DOWN("DOWN", 2);

    /**
     * the status name.
     */
    private String name;

    /**
     * the index.
     */
    private int index;

    /**
     * Constructor.
     *
     * @param name
     *         the Status name.
     * @param index
     *         the Status index.
     */
    private OperationalStatus(String name, int index){
        this.name = name;
        this.index = index;
    }

    /**
     * Get the OperationalStatus name.
     *
     * @return
     *         the name.
     */
    public String getName(){
        return this.name;
    }

    /**
     * Get the OperatinalStatus index.
     *
     * @return
     *         the index.
     */
    public int getIndex(){
        return this.index;
    }
}
