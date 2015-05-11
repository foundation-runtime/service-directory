package com.cisco.oss.foundation.directory.entity;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import org.junit.Test;

import com.cisco.oss.foundation.directory.utils.JsonSerializer;

import static org.junit.Assert.assertEquals;

/**
 * For 1.1 compatibility test since the providerId changes
 */
public class ServiceInstanceHeartbeatCompatibleTest {

    @Test
    public void testSerialize() throws Exception {
        ServiceInstanceHeartbeat heartbeat = new ServiceInstanceHeartbeat("foo","1.1.1.1") ;; //1.2
        assertEquals("foo",heartbeat.getServiceName());
        assertEquals("1.1.1.1", heartbeat.getProviderAddress());
        assertEquals("1.1.1.1", heartbeat.getProviderId());

        String body = new String(JsonSerializer.serialize(heartbeat));
        System.out.printf("%s\n",body);

        ServiceInstanceHeartbeat heartbeat11 = new ServiceInstanceHeartbeat("foo","1.1.1.1-1234") ;; //1.1
        assertEquals("1.1.1.1",heartbeat11.getProviderAddress());
        assertEquals("1.1.1.1-1234",heartbeat11.getProviderId());
        String body11 = new String(JsonSerializer.serialize(heartbeat11));
        System.out.printf("%s\n", body11);

    }

    @Test
    public void testDeserialize() throws Exception {
        String strOld11 = "{\"serviceName\":\"foo\",\"providerId\":\"1.1.1.1-1234\"}";
        ServiceInstanceHeartbeat heartbeat12 = JsonSerializer.deserialize(strOld11.getBytes(),ServiceInstanceHeartbeat.class);
        assertEquals("1.1.1.1-1234", heartbeat12.getProviderId());
        assertEquals("1.1.1.1", heartbeat12.getProviderAddress());
        System.out.printf("%s\n", new String(JsonSerializer.serialize(heartbeat12)));

    }
}
