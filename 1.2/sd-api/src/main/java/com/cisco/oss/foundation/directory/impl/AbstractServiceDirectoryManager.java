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
package com.cisco.oss.foundation.directory.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.lifecycle.Stoppable;

/**
 * Abstract base class for lookup and registration manger
 */
public abstract class AbstractServiceDirectoryManager implements Stoppable, AutoCloseable {

    public static final Logger LOGGER = LoggerFactory.getLogger(AbstractServiceDirectoryManager.class);

    /**
     * Mark component started or not
     */
    protected final AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * Mark the Manager is closed, when it closed, the manager can't use again.
     */
    protected final AtomicBoolean isClosed = new AtomicBoolean(false);

    @Override
    public void start() {
        isStarted.set(true);
    }

    @Override
    public void stop() {
        isStarted.set(false);
    }

    public boolean isStarted() throws ServiceException {
        return isStarted.get();
    }

    @Override
    public void close() throws ServiceException {
        try {
            stop();
        }catch(Throwable cause){
            //TODO, close should have an ERROR Code
            LOGGER.error("{} is failed to close", this);
            throw new ServiceException(ErrorCode.GENERAL_ERROR,cause,"STOP ERROR when try to close %s",this);
        }
        isClosed.set(true);
        LOGGER.info("{} is closed",this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
    }
}
