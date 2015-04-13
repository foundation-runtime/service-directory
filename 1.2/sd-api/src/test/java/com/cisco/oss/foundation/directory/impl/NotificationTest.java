package com.cisco.oss.foundation.directory.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.NotificationHandler;
import com.cisco.oss.foundation.directory.ServiceDirectory;
import com.cisco.oss.foundation.directory.client.DirectoryServiceClient;
import com.cisco.oss.foundation.directory.client.DirectoryServiceClientProvider;
import com.cisco.oss.foundation.directory.client.DirectoryServiceDummyClient;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.OperationResult;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import static com.cisco.oss.foundation.directory.ServiceDirectory.ServiceDirectoryConfig.ClientType.PROVIDED;

/**
 * Test for notification handler in lookup
 *
 */
public class NotificationTest {
    final static ModelService mockService = new ModelService("foo","192.168.0.1",new Date());
    final static Map<String, OperationResult<ModelService>> map = Collections.singletonMap(
            mockService.getName(),new OperationResult<>(true,mockService,null)
    );
    private static final DirectoryServiceDummyClient notificationTestMockClient = new DirectoryServiceDummyClient(){
        @Override
        public ModelService lookupService(String serviceName) {
            return  (mockService.getName().equals(serviceName)) ? mockService : null;
        }

        @Override
        public Map<String, OperationResult<ModelService>> getChangedServices(Map<String, ModelService> services) {
            //Always return mockService for now
            mockService.setModifiedTime(new Date());
            return map;
        }
    };

    @BeforeClass
    public static void setUpTestingClient(){
        ConfigurableServiceDirectoryManagerFactory.setClientProvider(new DirectoryServiceClientProvider() {
            @Override
            public DirectoryServiceClient getClient() {
                return notificationTestMockClient;
            }
        });
    }

    @Before
    public void setUp(){
        factory = ServiceDirectory.config().setClientType(PROVIDED).build();
        assertTrue(factory.getDirectoryServiceClient() == notificationTestMockClient);
        assertTrue(map == factory.getDirectoryServiceClient().getChangedServices(new HashMap<String, ModelService>()));
    }

    private ConfigurableServiceDirectoryManagerFactory factory;

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
}
