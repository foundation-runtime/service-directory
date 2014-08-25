/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.test;

import com.cisco.oss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceDirectoryManagerFactory;
import com.cisco.oss.foundation.directory.config.ServiceDirectoryConfig;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.lifecycle.Closable;

/**
 * The default ServiceDirectoryManagerFactory for integration test on top of SD API.
 * 
 * It is an in-memory service directory provider which stores and looks up services in local.
 * Set configuration "service.directory.manager.factory.provider" to
 * "test.TestServiceDirectoryManagerFactory" before invoking the ServiceDirectory.
 * 
 * @author zuxiang
 *
 */
public class TestServiceDirectoryManagerFactory implements
		ServiceDirectoryManagerFactory, Closable {
	
	/**
	 * The DefaultTestServiceDirectoryManager.
	 */
	private DefaultTestServiceDirectoryManager testManager;
	
	/**
	 * Constructor.
	 */
	public TestServiceDirectoryManagerFactory(){
	}
	
	public TestServiceDirectoryManagerFactory(DefaultTestServiceDirectoryManager testManager){
		this.testManager = testManager;
		testManager.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RegistrationManager getRegistrationManager() throws ServiceException {
		return getDefaultTestServiceDirectoryManager();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LookupManager getLookupManager() throws ServiceException {
		return getDefaultTestServiceDirectoryManager();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(DirectoryServiceClientManager manager) {
		// do nothing.

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setServiceDirectoryConfig(ServiceDirectoryConfig config) {
		// Do nothing.
	}
	
	/**
	 * Get the DefaultTestServiceDirectoryManager, it is lazy initialized.
	 * 
	 * @return
	 * 		the DefaultTestServiceDirectoryManager.
	 */
	public DefaultTestServiceDirectoryManager getDefaultTestServiceDirectoryManager(){
		if(testManager == null){
			synchronized(this){
				if(testManager == null){
					testManager = new DefaultTestServiceDirectoryManager();
					testManager.start();
				}
			}
		}
		return testManager;
	}

	@Override
	public void start() {
		// Do nothing here.
	}

	@Override
	public void stop() {
		if(testManager != null){
			if(testManager instanceof Closable){
				((Closable) testManager).stop();
			}
		}
	}
	
	
	

}
