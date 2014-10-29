package com.cisco.oss.foundation.directory.async;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.proto.Response;

public class ServiceDirectoryFuture implements Future<Response> {

	private volatile boolean completed = false;
	private volatile boolean cancelled = false;
	private volatile Response result;
	private volatile ServiceException ex;

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

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public boolean isDone() {
		return completed;
	}

	@Override
	public synchronized Response get() throws InterruptedException, ExecutionException {
		while (! this.completed) {
			wait();
		}
		return this.getResult();
	}

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

	public synchronized boolean complete(Response result) {
		if (completed) {
			return false;
		}
		completed = true;
		this.result = result;
		notifyAll();
		return true;
	}

	public synchronized boolean fail(ServiceException ex) {
		if (completed) {
			return false;
		}
		this.completed = true;
		this.ex = ex;
		notifyAll();
		return true;
	}

	private synchronized Response getResult() throws ExecutionException {
		if (this.ex != null) {
			throw new ExecutionException(ex);
		}
		return this.result;
	}
}
