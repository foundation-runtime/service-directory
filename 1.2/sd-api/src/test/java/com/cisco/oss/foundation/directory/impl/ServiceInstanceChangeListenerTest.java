package com.cisco.oss.foundation.directory.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.BeforeClass;
import org.junit.Test;

import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceInstanceChangeListener;
import com.cisco.oss.foundation.directory.entity.InstanceChange;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import static com.cisco.oss.foundation.directory.impl.ServiceDirectoryConfig.ClientType.IN_MEMORY;

/**
 * Unit Test for ServiceInstanceChangeListener
 */
public class ServiceInstanceChangeListenerTest {
    @BeforeClass
    public static void setUp() throws Exception {
        factory = ServiceDirectoryConfig.config().setCacheEnabled(false).setClientType(IN_MEMORY).build();
        final ProvidedServiceInstance fooInstance = new ProvidedServiceInstance("foo","192.168.1.1");
        fooInstance.setUri("http://foo/service");
        fooInstance.setStatus(OperationalStatus.DOWN);
        factory.getRegistrationManager().registerService(fooInstance);
        TimeUnit.MILLISECONDS.sleep(1L); //make sure change check start time is later than registration.
        factory.start();
    }

    private static ConfigurableServiceDirectoryManagerFactory factory;

    @Test
    public void testAddRemove() {
        ServiceInstanceChangeListener listener = mock(ServiceInstanceChangeListener.class);

        try (LookupManager lookup = factory.getLookupManager()){
            // test for normal add/remove OK
            lookup.addInstanceChangeListener("foo", listener);
            lookup.removeInstanceChangeListener("foo", listener);

            // test for not_exist service
            try {
                lookup.addInstanceChangeListener("not_exist", listener);
                fail();
            } catch (ServiceException e) {
                assertEquals(ErrorCode.SERVICE_NOT_EXIST, e.getErrorCode());
            }

            // test for handler not found
            lookup.addInstanceChangeListener("foo", listener);
            try {
                // the handler is not myHandler
                lookup.removeInstanceChangeListener("foo", mock(ServiceInstanceChangeListener.class));
            }catch(ServiceException e){
                assertEquals(ErrorCode.SERVICE_INSTANCE_LISTENER_NOT_EXIST,e.getErrorCode());
            }
        }
    }

    @Test
    public void testOnChange() throws Exception {
        final CountDownLatch countDown = new CountDownLatch(1);
        final AtomicReference<ServiceInstance> lastInst = new AtomicReference<>();
        final ServiceInstanceChangeListener listener = new ServiceInstanceChangeListener() {
            @Override
            public void onChange(InstanceChange.ChangeType type, InstanceChange<ServiceInstance> change) throws Exception {
                System.out.printf("%s\n",change);
                if(type== InstanceChange.ChangeType.Remove){ // the last one is remove
                    countDown.countDown();
                    lastInst.set(change.from);
                }
            }
        };
        try (LookupManager lookup = factory.getLookupManager();RegistrationManager reg = factory.getRegistrationManager()){
            lookup.addInstanceChangeListener("foo", listener);
            reg.updateServiceOperationalStatus("foo", "192.168.1.1", OperationalStatus.UP);
            assertEquals(OperationalStatus.UP, lookup.lookupInstance("foo").getStatus());
            reg.updateServiceOperationalStatus("foo", "192.168.1.1", OperationalStatus.DOWN);
            // you can never find a DOWN
            //assertEquals(OperationalStatus.DOWN, lookup.lookupInstance("foo").getStatus());
            reg.updateServiceOperationalStatus("foo", "192.168.1.1", OperationalStatus.UP);
            assertEquals(OperationalStatus.UP, lookup.lookupInstance("foo").getStatus());

            reg.registerService(new ProvidedServiceInstance("foo", "192.168.1.2", 2222, "http://cisco.com/foo/2",
                    OperationalStatus.DOWN, null));
            TimeUnit.MILLISECONDS.sleep(10L);
            reg.updateServiceOperationalStatus("foo", "192.168.1.2", OperationalStatus.UP);
            reg.unregisterService("foo", "192.168.1.2");
            assertTrue("Shouldn't wait more than 5 sec",countDown.await(5, TimeUnit.SECONDS));
            assertEquals("192.168.1.2",lastInst.get().getAddress());
        }
    }
}
