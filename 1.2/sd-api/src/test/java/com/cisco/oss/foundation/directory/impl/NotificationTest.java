package com.cisco.oss.foundation.directory.impl;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.cisco.oss.foundation.directory.ServiceDirectory;
import com.cisco.oss.foundation.directory.client.DirectoryServiceClient;
import com.cisco.oss.foundation.directory.client.DirectoryServiceClientProvider;
import com.cisco.oss.foundation.directory.client.DirectoryServiceMockClient;
import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.OperationResult;

import static com.cisco.oss.foundation.directory.ServiceDirectory.ServiceDirectoryConfig.ClientType.PROVIDED;

/**
 * Test for notification handler in lookup
 *
 */
public class NotificationTest {

    private static final DirectoryServiceMockClient notificationTestMockClient = new DirectoryServiceMockClient(){
        @Override
        public Map<String, OperationResult<ModelService>> getChangedServices(Map<String, ModelService> services) {
            //TODO: implements of logic which can used for unit-test
            return null;
        }
    };

    @BeforeClass
    public static void setUpTestingClient(){
        ServiceDirectory.globeConfig().setClientType(PROVIDED);
        ServiceDirectory.setClientProvider(new DirectoryServiceClientProvider() {
            @Override
            public DirectoryServiceClient getClient() {
                return notificationTestMockClient;
            }
        });
    }
    @Test
    public void testAddNotification(){
        ServiceDirectory.config().build();
    }

}
