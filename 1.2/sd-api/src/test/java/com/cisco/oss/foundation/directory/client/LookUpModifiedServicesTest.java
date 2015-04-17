package com.cisco.oss.foundation.directory.client;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.client.DirectoryServiceClient.InstanceChange;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * test for new changed look up methods
 */
public class LookUpModifiedServicesTest {

    private final static Logger LOG = LoggerFactory.getLogger(LookUpModifiedServicesTest.class);
    private DirectoryServiceInMemoryClient sharedMemoryClient;


    @Before
    public void setUp(){
        sharedMemoryClient = new DirectoryServiceInMemoryClient();
        final ProvidedServiceInstance instance1 = new ProvidedServiceInstance("myService","192.168.0.1",1111);
        final ProvidedServiceInstance instance2 = new ProvidedServiceInstance("myService","192.168.0.2",2222);
        final ProvidedServiceInstance instance3 = new ProvidedServiceInstance("myService","192.168.0.3",3333);
        sharedMemoryClient.registerInstance(instance1);
        sharedMemoryClient.registerInstance(instance2);
        sharedMemoryClient.registerInstance(instance3);
    }

    @Test
    public void testLookUpForChange(){
        final long sinceStarted = System.currentTimeMillis();
        List<ModelServiceInstance> all = sharedMemoryClient.getAllInstances();
        assertEquals(3,all.size());
        for (ModelServiceInstance instance : all){
            LOG.debug("Instance {}, modified time {} : now {}",instance.getId(), instance.getModifiedTime().getTime(), sinceStarted);
            assertTrue(instance.getModifiedTime().getTime() <= sinceStarted);
        }
        ModelService service = sharedMemoryClient.lookupService("myService");
        LOG.debug("Service {}, modified time {} : now {}", service.getId(), service.getModifiedTime().getTime(),sinceStarted);
        assertTrue(service.getModifiedTime().getTime() <= sinceStarted);

        List<ServiceInstance> result = sharedMemoryClient.lookUpChangedServiceInstancesSince("myService", sinceStarted);
        // no one changed now
        assertEquals(0, result.size());

        final long now = System.currentTimeMillis()-1;//sub to 1 ms so that make sure update happened
        List<ServiceInstance> result2 = sharedMemoryClient.lookUpChangedServiceInstancesSince("myService", now);
        // still no one changed till now
        assertEquals(0,result2.size());
        // now change it
        sharedMemoryClient.updateInstanceStatus("myService", "192.168.0.1-1111", OperationalStatus.UP, true);
        List<ServiceInstance> result3 = sharedMemoryClient.lookUpChangedServiceInstancesSince("myService", now);
        assertEquals(1, result3.size());

        List<InstanceChange<ServiceInstance>> changes = sharedMemoryClient.lookupChangesSince("myService", now);
        assertEquals(1, changes.size());
        assertEquals(InstanceChange.ChangeType.Status, changes.get(0).changeType);
        assertEquals("UP", changes.get(0).to);
        sharedMemoryClient.updateInstanceStatus("myService", "192.168.0.1-1111", OperationalStatus.DOWN, true);

        changes = sharedMemoryClient.lookupChangesSince("myService", now);
        assertEquals(2, changes.size());
        //remove the one
        sharedMemoryClient.unregisterInstance("myService", "192.168.0.1-1111", true);

        changes = sharedMemoryClient.lookupChangesSince("myService", now);
        assertEquals(0, changes.size());
    }
}
