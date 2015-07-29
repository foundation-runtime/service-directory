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
import com.cisco.oss.foundation.directory.lookup.DirectoryLookupService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static com.cisco.oss.foundation.directory.entity.OperationalStatus.DOWN;
import static com.cisco.oss.foundation.directory.entity.OperationalStatus.UP;


/**
 * Unit test for ModelServiceClientCache
 */
public class ModelServiceClientCacheTest {

    private final DirectoryServiceClient client = new DirectoryServiceInMemoryClient();
    private final DirectoryLookupService lookupService = new DirectoryLookupService(client);

    @Before
    public void setup() {
        client.registerInstance(new ProvidedServiceInstance("foo", "192.168.1.1", "http://foo.cisco.com", DOWN
                , null));
        lookupService.start();
    }

    @After
    public void teardown() {
        lookupService.stop();
    }

    @Test
    public void testClientCache() throws Exception {

        final ModelService fooService = client.lookupService("foo");
        assertNotNull(fooService);
        final ModelServiceClientCache fooCache = new ModelServiceClientCache(fooService);

        lookupService.addInstanceChangeListener(fooService.getName(), fooCache);

        final CountDownLatch latch = new CountDownLatch(1);
        lookupService.addServiceInstanceChangeListener(fooService.getName(), new ServiceInstanceChangeListener() {
            @Override
            public void onChange(InstanceChange.ChangeType type, InstanceChange<ServiceInstance> change) throws Exception {
                assertEquals(InstanceChange.ChangeType.STATUS, type);
                assertEquals(DOWN, change.from.getStatus());
                assertEquals(1, fooCache.getAllModelServiceInstance().size());
                assertEquals(UP, fooCache.getAllModelServiceInstance().get(0).getStatus());
                latch.countDown();
            }
        });
        client.updateInstanceStatus("foo", "192.168.1.1", UP, true);
        assertTrue("error if wait 5 secs no changes found", latch.await(5, TimeUnit.SECONDS));
        lookupService.removeInstanceChangeListener("foo", fooCache);

    }
}
