package com.cisco.oss.foundation.directory.cache;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cisco.oss.foundation.directory.client.DirectoryServiceInMemoryClient;
import com.cisco.oss.foundation.directory.lookup.DirectoryLookupService;

/**
 * Unit test for ModelServiceClientCache
 */
public class ModelServiceClientCacheTest {

    private final DirectoryLookupService lookupService = new DirectoryLookupService(new DirectoryServiceInMemoryClient());

    @Before
    public void startService() {
        lookupService.start();
    }

    @After
    public void stopService() {
        lookupService.stop();
    }

    @Test
    public void testClientCache() throws Exception {

    }
}
