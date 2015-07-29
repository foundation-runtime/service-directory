package com.cisco.oss.foundation.directory.cache;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.cisco.oss.foundation.directory.ServiceInstanceChangeListener;
import com.cisco.oss.foundation.directory.client.DirectoryServiceClient;
import com.cisco.oss.foundation.directory.client.DirectoryServiceInMemoryClient;
import com.cisco.oss.foundation.directory.entity.InstanceChange;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.lookup.CachedDirectoryLookupService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
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
    public void testCache() throws Exception{
        final ModelService fooService = client.lookupService("foo");
        assertEquals(DOWN, fooService.getServiceInstances().get(0).getStatus());
        assertNotNull(fooService);
        client.updateInstanceStatus("foo", "192.168.1.1", UP, true);
        final ModelService fooService1 = client.lookupService("foo");
        final ModelService fooService2 = lookupService.getModelService("foo");
        final ModelService fooService3 = lookupService.getModelService("foo");
        assertEquals(fooService1, fooService2);
        assertNotSame(fooService1, fooService2); //client always get an new one
        assertSame(fooService2, fooService3);    //cached service return the same one
        assertEquals(fooService2, fooService3);    //cached service return the same one
        assertEquals(UP,fooService1.getServiceInstances().get(0).getStatus());
        assertEquals(UP, fooService2.getServiceInstances().get(0).getStatus());
        assertEquals(UP, fooService3.getServiceInstances().get(0).getStatus());
        final CountDownLatch latch = new CountDownLatch(1);
        lookupService.addServiceInstanceChangeListener(fooService.getName(), new ServiceInstanceChangeListener() {
            @Override
            public void onChange(InstanceChange.ChangeType type, InstanceChange<ServiceInstance> change) throws Exception {
                if (type == InstanceChange.ChangeType.STATUS && change.from.getStatus()==UP && change.to.getStatus()==DOWN) {
                    latch.countDown();
                }
            }
        });
        client.updateInstanceStatus("foo", "192.168.1.1", DOWN, true);
        assertTrue("error if wait 5 secs no changes found", latch.await(5, TimeUnit.SECONDS));

        final ModelService fooService4 = lookupService.getModelService("foo");
        final ModelService fooService5 = client.lookupService("foo");
        assertSame(fooService3, fooService4);
        assertNotSame(fooService4, fooService5);
        assertEquals(DOWN, fooService4.getServiceInstances().get(0).getStatus());
        assertEquals(DOWN, fooService5.getServiceInstances().get(0).getStatus());
        assertEquals(fooService4.getServiceInstances(),fooService5.getServiceInstances());
    }

}
