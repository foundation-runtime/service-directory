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
package com.cisco.oss.foundation.directory.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.proto.Response;

/**
 * The Future of the Service Directory request.
 *
 * For the asynchronized method in DirectoryServiceClient, it returns this Future object.
 * Then the upper application can use the Future.
 *
 * @author zuxiang
 *
 */
public class ServiceDirectoryFuture implements Future<Response> {

    /**
     * Indicate whether the Future complete.
     */
    private volatile boolean completed = false;

    /**
     * Indicate whether the Future cancelled.
     */
    private volatile boolean cancelled = false;

    /**
     * The Directory Request Response.
     */
    private volatile Response result;

    /**
     * The ServiceException of the Directory Request.
     */
    private volatile ServiceException ex;

    /**
     * Cancel the Directory Request.
     *
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (completed) {
            return false;
        }
        this.completed = true;
        this.cancelled = true;
        notifyAll();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDone() {
        return completed;
    }

    /**
     * Get the Directory Request Response.
     *
     * {@inheritDoc}
     */
    @Override
    public synchronized Response get() throws InterruptedException, ExecutionException {
        while (! this.completed) {
            wait();
        }
        return this.getResult();
    }

    /**
     * Get the Directory Request Response.
     *
     * {@inheritDoc}
     */
    @Override
    public synchronized Response get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        long now = System.currentTimeMillis();
        long mills = unit.toMillis(timeout);
        long toWait = mills;
        if (this.completed) {
            return this.getResult();
        } else if (toWait <= 0) {
            throw new TimeoutException("Timeout is smaller than 0.");
        } else {
            for (;;) {
                wait(toWait);
                if (this.completed) {
                    return this.getResult();
                }
                long gap = System.currentTimeMillis() - now;
                toWait = toWait - gap;
                if (toWait <= 0) {
                    throw new TimeoutException();
                }
            }
        }
    }

    /**
     * Complete the Future.
     *
     * @param result
     *         The Directory Request Response.
     * @return
     *         true for complete success.
     */
    public synchronized boolean complete(Response result) {
        if (completed) {
            return false;
        }
        completed = true;
        this.result = result;
        notifyAll();
        return true;
    }

    /**
     * Fail the Future.
     *
     * @param ex
     *         the ServiceException of the Directory Request.
     * @return
     *         true for success.
     */
    public synchronized boolean fail(ServiceException ex) {
        if (completed) {
            return false;
        }
        this.completed = true;
        this.ex = ex;
        notifyAll();
        return true;
    }

    /**
     * Get the Directory Request Response.
     *
     * If the Future failed, return the Exception.
     *
     * @return
     *         the Directory Request Response.
     * @throws ExecutionException
     *         the ServiceException.
     */
    private synchronized Response getResult() throws ExecutionException {
        if (this.ex != null) {
            throw new ExecutionException(ex);
        }
        return this.result;
    }
}
