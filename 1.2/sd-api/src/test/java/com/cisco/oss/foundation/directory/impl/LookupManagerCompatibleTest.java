package com.cisco.oss.foundation.directory.impl;

import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import static com.cisco.oss.foundation.directory.impl.ServiceDirectoryConfig.ClientType.IN_MEMORY;

/**
 * The Backward Compatibility Test for
 *
 */
public class LookupManagerCompatibleTest {

    private static ConfigurableServiceDirectoryManagerFactory factory;
    @BeforeClass
    public static void setUp() throws Exception {
        factory = ServiceDirectoryConfig.config().setCacheEnabled(false).setHeartbeatEnabled(false).setClientType(IN_MEMORY).build();
        // OLD 1.1 providerId like instance
        final ProvidedServiceInstance old11Instance = new ProvidedServiceInstance("old","192.168.1.1-1234");
        old11Instance.setUri("http://old/service");
        old11Instance.setStatus(OperationalStatus.UP);
        factory.getRegistrationManager().registerService(old11Instance);
        // NEW 1.2 address only instance
        final ProvidedServiceInstance new12Instance = new ProvidedServiceInstance("new","192.168.1.1");
        new12Instance.setUri("http://new/service");
        new12Instance.setStatus(OperationalStatus.UP);
        factory.getRegistrationManager().registerService(new12Instance);
        factory.start();
    }
    @AfterClass
    public static void shutdown() throws Exception {
        factory.stop();
    }

    @Test
    public void testLookupByProviderId(){
        // test for the deprecated method using provider format
        ServiceInstance ins11 = factory.getLookupManager().getInstance("old", "192.168.1.1-1234");
        assertNotNull(ins11);
        assertEquals("old", ins11.getServiceName());
        assertEquals(OperationalStatus.UP, ins11.getStatus());

        ServiceInstance ins12 = factory.getLookupManager().getInstance("new", "192.168.1.1");
        assertNotNull(ins12);
        assertEquals("new",ins12.getServiceName());
        assertEquals(OperationalStatus.UP, ins12.getStatus());

        ServiceInstance stillWork = factory.getLookupManager().getInstance("new", "192.168.1.1-1234");
        assertNotNull(stillWork);
        assertEquals("new", ins12.getServiceName());
        assertEquals(OperationalStatus.UP, stillWork.getStatus());
    }

    @Test
    public void testUsingNewAPI(){
        ServiceInstance instance = factory.getLookupManager().getInstanceByAddress("new", "192.168.1.1");
        assertNotNull(instance);
        assertEquals("new",instance.getServiceName());
        assertEquals(OperationalStatus.UP,instance.getStatus());
    }
}
