package com.cisco.oss.foundation.directory.client;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.entity.InstanceChange;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static com.cisco.oss.foundation.directory.entity.InstanceChange.ChangeType.Status;

/**
 * test for new changed look up methods by using InMemory client
 */
public class LookupChangesTestByInMemoryClient {

    private final static Logger LOG = LoggerFactory.getLogger(LookupChangesTestByInMemoryClient.class);
    private DirectoryServiceInMemoryClient sharedMemoryClient;
    private final static long INIT_TIME = System.currentTimeMillis();


    @Before
    public void setUp() throws InterruptedException {
        sharedMemoryClient = new DirectoryServiceInMemoryClient();
        final ProvidedServiceInstance instance1 = new ProvidedServiceInstance("myService","192.168.0.1");
        final ProvidedServiceInstance instance2 = new ProvidedServiceInstance("myService","192.168.0.2");
        final ProvidedServiceInstance instance3 = new ProvidedServiceInstance("myService","192.168.0.3");
        sharedMemoryClient.registerInstance(instance1);
        TimeUnit.MILLISECONDS.sleep(1L);
        sharedMemoryClient.registerInstance(instance2);
        TimeUnit.MILLISECONDS.sleep(1L);
        sharedMemoryClient.registerInstance(instance3);
        TimeUnit.MILLISECONDS.sleep(1L);
    }

    @Test
    public void testLookUpForChange() throws InterruptedException{
        final long sinceStarted = System.currentTimeMillis();
        TimeUnit.MILLISECONDS.sleep(1L);
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

        final long now = System.currentTimeMillis();
        TimeUnit.MILLISECONDS.sleep(1L); //wait for 1 ms so that make sure update happened
        List<ServiceInstance> result2 = sharedMemoryClient.lookUpChangedServiceInstancesSince("myService", now);
        // still no one changed till now
        assertEquals(0,result2.size());
        // now change it
        sharedMemoryClient.updateInstanceStatus("myService", "192.168.0.1", OperationalStatus.UP, true);
        List<ServiceInstance> result3 = sharedMemoryClient.lookUpChangedServiceInstancesSince("myService", now);
        assertEquals(1, result3.size());

        List<InstanceChange<ModelServiceInstance>> changes = sharedMemoryClient.lookupChangesSince("myService", now);
        assertEquals(1, changes.size());
        assertEquals(Status, changes.get(0).changeType);
        assertEquals(OperationalStatus.UP, changes.get(0).to.getStatus());
        sharedMemoryClient.updateInstanceStatus("myService", "192.168.0.1", OperationalStatus.DOWN, true);

        changes = sharedMemoryClient.lookupChangesSince("myService", now);
        assertEquals(2, changes.size());
        //remove the one
        sharedMemoryClient.unregisterInstance("myService", "192.168.0.1", true);

        changes = sharedMemoryClient.lookupChangesSince("myService", now);
        assertEquals(3, changes.size()); //it's should be 3, because now we add one record for the delete!
    }

    @Test
    public void testGetLastChangedTimeMills(){
        assertNotNull(sharedMemoryClient.getLastChangedTimeMills("not_exist"));
        assertEquals(-1L, sharedMemoryClient.getLastChangedTimeMills("not_exist"));
        assertEquals(-1L, new Date(-1L).getTime());

        // default timezone is china (Asia/Shanghai, Etc/GMT+8), switch to UTC, so that the -1,0 is shown as its defined
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        System.out.printf("%1tc \n",new Date(-1L)); //no different than default, notice the printf for date need to use
        System.out.printf("%s \n",new Date(-2L));
        System.out.printf("%s \n", new Date(0L));
        System.out.printf("%s \n", new Date(1L));

        final long now = System.currentTimeMillis();
        // happen before now
        assertTrue(now > sharedMemoryClient.getLastChangedTimeMills("myService"));

    }

    @Test
    public void testForComparatorInstanceChange() throws InterruptedException{
        TimeUnit.MILLISECONDS.sleep(1L);
        List<InstanceChange<ModelServiceInstance>> changes = sharedMemoryClient.lookupChangesSince("myService", INIT_TIME);
        assertEquals(3, changes.size());

        Collections.sort(changes, InstanceChange.Comparator);
        //old first
        assertTrue(changes.get(0).changedTimeMills < changes.get(2).changedTimeMills);

        Collections.sort(changes, InstanceChange.ReverseComparator);
        //latest first
        assertTrue(changes.get(0).changedTimeMills > changes.get(2).changedTimeMills);

    }

    @Test
    public void testAddAnotherServiceForLookupChanges() throws InterruptedException {
        final long now = System.currentTimeMillis();
        //make sure register operation is happen-after now
        TimeUnit.MILLISECONDS.sleep(1L);
        //add a new service
        final ProvidedServiceInstance newService = new ProvidedServiceInstance("newService","192.168.1.1");
        sharedMemoryClient.registerInstance(newService);
        List<InstanceChange<ModelServiceInstance>> changes = sharedMemoryClient.lookupChangesSince("newService", now);
        assertEquals(1, changes.size());
        final long sinceCreated = System.currentTimeMillis();
        //make sure update operation is happen-after 'sinceCreated'
        TimeUnit.MILLISECONDS.sleep(1L);
        sharedMemoryClient.updateInstanceStatus("newService", "192.168.1.1", OperationalStatus.UP, true);
        assertEquals(2, sharedMemoryClient.lookupChangesSince("newService", now).size());
        assertEquals(1, sharedMemoryClient.lookupChangesSince("newService", sinceCreated).size());
        assertEquals(Status,sharedMemoryClient.lookupChangesSince("newService",sinceCreated).get(0).changeType);
    }
}
