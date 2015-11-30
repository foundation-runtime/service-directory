package com.cisco.oss.foundation.directory.cache;

import com.cisco.oss.foundation.directory.ServiceInstanceChangeListener;
import com.cisco.oss.foundation.directory.client.DirectoryServiceInMemoryClient;
import com.cisco.oss.foundation.directory.entity.*;
import com.cisco.oss.foundation.directory.lookup.CachedDirectoryLookupService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.cisco.oss.foundation.directory.entity.OperationalStatus.DOWN;
import static com.cisco.oss.foundation.directory.entity.OperationalStatus.UP;
import static org.junit.Assert.*;

/**
 * The unit test for new CachedDirectoryLookupService by using
 * ClientCache
 */
public class CachedDirectoryLookupServiceTest {

    private final DirectoryServiceInMemoryClient client = new DirectoryServiceInMemoryClient();
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
        if (fooService != null) {
            final List<ModelServiceInstance> serviceInstances = fooService.getServiceInstances();
            assertNotNull(serviceInstances);
            assertEquals(DOWN, serviceInstances.get(0).getStatus());
            assertNotNull(fooService);
            client.updateInstanceStatus("foo", "192.168.1.1", UP, true);


        } else {
            fail("foo service not found!");
        }

        final ModelService fooService1 = client.lookupService("foo");
        final ModelService fooService2 = lookupService.getModelService("foo");
        final ModelService fooService3 = lookupService.getModelService("foo");

        if (fooService1 != null && fooService2 != null && fooService3 != null) {

            assertEquals(fooService1, fooService2);
            assertNotSame(fooService1, fooService2); //client always get an new one
            assertSame(fooService2, fooService3);    //cached service return the same one
            assertEquals(fooService2, fooService3);    //cached service return the same one
            assertEquals(UP, fooService1.getServiceInstances().get(0).getStatus());
            assertEquals(UP, fooService2.getServiceInstances().get(0).getStatus());
            assertEquals(UP, fooService3.getServiceInstances().get(0).getStatus());

        } else {
            assertNotNull(fooService1);
            assertNotNull(fooService2);
            assertNotNull(fooService3);
        }

        if (fooService != null) {
            final CountDownLatch latch = new CountDownLatch(1);
            lookupService.addServiceInstanceChangeListener(fooService.getName(), new ServiceInstanceChangeListener() {
                @Override
                public void onChange(InstanceChange.ChangeType type, InstanceChange<ServiceInstance> change) throws Exception {
                    if (type == InstanceChange.ChangeType.STATUS && change.from.getStatus() == UP && change.to.getStatus() == DOWN) {
                        latch.countDown();
                    }
                }
            });
            client.updateInstanceStatus("foo", "192.168.1.1", DOWN, true);
            assertTrue("error if wait 5 secs no changes found", latch.await(5, TimeUnit.SECONDS));
        }else{
            assertNotNull(fooService);
        }


        final ModelService fooService4 = lookupService.getModelService("foo");
        final ModelService fooService5 = client.lookupService("foo");
        assertSame(fooService3, fooService4);
        assertNotSame(fooService4, fooService5);
        assertEquals(DOWN, fooService4.getServiceInstances().get(0).getStatus());
        assertEquals(DOWN, fooService5.getServiceInstances().get(0).getStatus());
        assertEquals(fooService4.getServiceInstances(), fooService5.getServiceInstances());
    }

}
