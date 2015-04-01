package com.cisco.oss.foundation.directory.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.lifecycle.Stoppable;

/**
 * Abstract base class for all ServiceDirectory Services
 */
public abstract class ServiceDirectoryService implements Stoppable {
    public static final Logger LOGGER = LoggerFactory.getLogger(ServiceDirectoryService.class);
    @Override
    public void start() {
        LOGGER.info("{} started",this);

    }

    @Override
    public void stop() {
        LOGGER.info("{} stopped",this);
    }
}
