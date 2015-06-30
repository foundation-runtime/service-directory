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
     * Mark the Manager closed. The manager can not be reused when it is closed.
     */
    protected final AtomicBoolean isClosed = new AtomicBoolean(false);

    private CloseListener listener;
    /**
     * Setter
     * @param listener
     *              The CloseListener
     */
    public synchronized void setCloseListener(CloseListener listener){
        this.listener = listener;
    }

    @Override
    public void start() {
        isStarted.set(true);
    }

    @Override
    public void stop() {
        isStarted.set(false);
    }

    /**
     * Check if the manager is started or not
     * @return boolean indicating whether it is started or not
     * @throws ServiceException
     */
    public boolean isStarted() throws ServiceException {
        return isStarted.get();
    }

    @Override
    public void close() throws ServiceException {
        try {
            fireClose();
        }catch(Throwable cause){
            LOGGER.error("{} is failed to close", this);
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_MANAGER_CLOSE_ERROR,cause,
                    ErrorCode.SERVICE_DIRECTORY_MANAGER_CLOSE_ERROR.getMessageTemplate(),this);
        }
        isClosed.set(true);
        LOGGER.info("{} is closed",this);
    }

    protected void fireClose() {
        if (listener !=null) {
            listener.onManagerClose(this);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
    }

    /**
     * Getter
     * 
     * @return The ServiceDirectoryService
     */
    public abstract ServiceDirectoryService getService();
}
