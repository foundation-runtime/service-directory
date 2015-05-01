package com.cisco.oss.foundation.directory.impl;

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.ServiceDirectory;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import static com.cisco.oss.foundation.directory.impl.ServiceDirectoryConfig.ClientType.IN_MEMORY;
import static com.cisco.oss.foundation.directory.impl.ServiceDirectoryConfig.ClientType.PROVIDED;

/**
 * UnitTest for ServiceDirectory reset
 */
public class ServiceDirectoryResetTest {

    @BeforeClass
    public static void setup() {
        ConfigurableServiceDirectoryManagerFactory myFactory = ServiceDirectoryConfig.config().setClientType(IN_MEMORY).build();
        ServiceDirectoryImpl.getInstance().reinitServiceDirectoryManagerFactory(myFactory);
        assertSame(myFactory, ServiceDirectoryImpl.getInstance().getServiceDirectoryManagerFactory());
        ProvidedServiceInstance fooInstance = new ProvidedServiceInstance("foo","192.168.1.1");
        fooInstance.setStatus(OperationalStatus.UP);
        fooInstance.setUri("http://cisco/foo");
        ServiceDirectory.getRegistrationManager().registerService(fooInstance);
    }

    @AfterClass
    public static void teardown() {
        ServiceDirectoryImpl.getInstance().restart();
        ServiceDirectoryImpl.getInstance().reinitServiceDirectoryManagerFactory(new DefaultServiceDirectoryManagerFactory());
    }

    @Test
    public void testReset() throws InterruptedException {
        LookupManager lookup = ServiceDirectory.getLookupManager();

        assertEquals(1, lookup.getAllInstances().size());
        verifyInstance(lookup.lookupInstance("foo"));
        ServiceDirectory.shutdown();
        //the manger already closed
        try {
            lookup.getAllInstances();
        } catch (ServiceException e) {
            assertEquals(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED, e.getErrorCode());
        }
        //the SD is shutdown you can't get manager more
        try {
            LookupManager newLookup = ServiceDirectory.getLookupManager();
        } catch (ServiceException e) {
            assertEquals(ErrorCode.SERVICE_DIRECTORY_IS_SHUTDOWN, e.getErrorCode());
        }
        ServiceDirectory.reset();
        //it still closed, because it's the old reference
        try {
            lookup.getAllInstances();
        } catch (ServiceException e) {
            assertEquals(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED, e.getErrorCode());
        }
        //get new one now ok
        LookupManager newLookup = ServiceDirectory.getLookupManager();
        assertEquals(1, newLookup.getAllInstances().size());
        verifyInstance(newLookup.lookupInstance("foo"));

    }

    private void verifyInstance(ServiceInstance instance){
        assertEquals("foo",instance.getServiceName());
        assertEquals("192.168.1.1",instance.getAddress());
    }
}
