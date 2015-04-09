package com.cisco.oss.foundation.directory.impl;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.ServiceDirectory;

/**
 * UnitTest for ServiceDirectory reset
 */
public class ServiceDirectoryResetTest {

    @Test
    public void testReset() throws InterruptedException {
        LookupManager lookup = ServiceDirectory.getLookupManager();
        ServiceDirectory.shutdown();
        ServiceDirectory.reset();
        ServiceDirectory.shutdown();
    }
}
