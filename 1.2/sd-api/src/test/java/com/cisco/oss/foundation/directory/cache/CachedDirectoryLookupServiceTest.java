package com.cisco.oss.foundation.directory.cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cisco.oss.foundation.directory.client.DirectoryServiceClient;
import com.cisco.oss.foundation.directory.client.DirectoryServiceInMemoryClient;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.lookup.CachedDirectoryLookupService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static com.cisco.oss.foundation.directory.entity.OperationalStatus.DOWN;
import static com.cisco.oss.foundation.directory.entity.OperationalStatus.UP;

/**
 * The unit test for new CachedDirectoryLookupService by using
 * ClientCache
 */
public class CachedDirectoryLookupServiceTest {

    private final DirectoryServiceClient client = new DirectoryServiceInMemoryClient();
    private final CachedDirectoryLookupService lookupService = new CachedDirectoryLookupService(client);


    @Before
    public void setup() {
        client.registerInstance(new ProvidedServiceInstance("foo","192.168.1.1","http://foo.cisco.com", DOWN
                ,null));
        lookupService.start();
    }
    @After

    public void teardown() {
        lookupService.stop();
    }

    @Test
    public void testCache() {
        final ModelService fooService = client.lookupService("foo");
        assertEquals(DOWN, fooService.getServiceInstances().get(0).getStatus());
        assertNotNull(fooService);
        client.updateInstanceStatus("foo", "192.168.1.1", UP, true);
        final ModelService fooService1 = client.lookupService("foo");
        final ModelService fooService2 = lookupService.getModelService("foo");
        final ModelService fooService3 = lookupService.getModelService("foo");
        assertEquals(fooService1,fooService2);
        assertTrue(fooService1 != fooService2); //client always get an new one
        assertTrue(fooService2 == fooService3);    //cached service return the same one
        assertEquals(fooService2 , fooService3);    //cached service return the same one
        assertEquals(UP,fooService1.getServiceInstances().get(0).getStatus());
        assertEquals(UP,fooService2.getServiceInstances().get(0).getStatus());
        assertEquals(UP,fooService3.getServiceInstances().get(0).getStatus());
        client.updateInstanceStatus("foo", "192.168.1.1", DOWN, true);
        final ModelService fooService4 = lookupService.getModelService("foo");
        final ModelService fooService5 = client.lookupService("foo");
        assertTrue(fooService3 == fooService4);
        //assertEquals(DOWN,fooService4.getServiceInstances().get(0).getStatus());
        assertEquals(DOWN,fooService5.getServiceInstances().get(0).getStatus());
    }

}
