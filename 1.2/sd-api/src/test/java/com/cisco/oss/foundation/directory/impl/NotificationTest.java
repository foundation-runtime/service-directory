package com.cisco.oss.foundation.directory.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.NotificationHandler;
import com.cisco.oss.foundation.directory.RegistrationManager;
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
 * Test for notification handler in lookup
 *
 */
public class NotificationTest {

    @BeforeClass
    public static void setUp(){
        factory = ServiceDirectoryConfig.config().setCacheEnabled(false).setClientType(IN_MEMORY).build();
        final ProvidedServiceInstance fooInstance = new ProvidedServiceInstance("foo","192.168.1.1",1234);
        fooInstance.setUri("http://foo/service");
        fooInstance.setStatus(OperationalStatus.DOWN);
        factory.getRegistrationManager().registerService(fooInstance);
        factory.start();
    }

    private static ConfigurableServiceDirectoryManagerFactory factory;

    @Test
    public void testAddNotification() {

        NotificationHandler myHandler = mock(NotificationHandler.class);

        try (LookupManager lookup = factory.getLookupManager()){
            // test for normal add/remove OK
            lookup.addNotificationHandler("foo", myHandler);
            lookup.removeNotificationHandler("foo", myHandler);

            // test for not_exist service
            try {
                lookup.addNotificationHandler("not_exist", myHandler);
                fail();
            } catch (ServiceException e) {
                assertEquals(ErrorCode.SERVICE_NOT_EXIST, e.getErrorCode());
            }

            // test for handler not found
            lookup.addNotificationHandler("foo", myHandler);
            try {
                // the handler is not myHandler
                lookup.removeNotificationHandler("foo", mock(NotificationHandler.class));
            }catch(ServiceException e){
                //TODO, might a specified error code for handler not found.
                assertEquals(ErrorCode.GENERAL_ERROR,e.getErrorCode());
            }
        }
    }

    @Test
    public void testNotifyOnAvailable() throws InterruptedException {
        final CountDownLatch countDown = new CountDownLatch(1);
        NotificationHandler myHandler = new NotificationHandler() {
            @Override
            public void serviceInstanceAvailable(ServiceInstance service) {
                System.out.printf("serviceInstance %s is %s now \n",service,service.getStatus());
                countDown.countDown();
            }

            @Override
            public void serviceInstanceUnavailable(ServiceInstance service) {
                System.out.printf("serviceInstance %s is %s now \n",service,service.getStatus());
                countDown.countDown();

            }

            @Override
            public void serviceInstanceChange(ServiceInstance service) {

            }
        };
        try (LookupManager lookup = factory.getLookupManager();RegistrationManager reg = factory.getRegistrationManager()){
            lookup.addNotificationHandler("foo",myHandler);
            reg.updateServiceOperationalStatus("foo", "192.168.1.1-1234", OperationalStatus.UP);
            assertEquals(OperationalStatus.UP, lookup.lookupInstance("foo").getStatus());
            reg.updateServiceOperationalStatus("foo", "192.168.1.1-1234", OperationalStatus.DOWN);
            // you can never find a DOWN
            //assertEquals(OperationalStatus.DOWN, lookup.lookupInstance("foo").getStatus());
            reg.updateServiceOperationalStatus("foo", "192.168.1.1-1234", OperationalStatus.UP);
            assertEquals(OperationalStatus.UP, lookup.lookupInstance("foo").getStatus());

            assertTrue(countDown.await(5,TimeUnit.SECONDS)); //should not more than 5 sec
        }
    }
}
