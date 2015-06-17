package com.cisco.oss.foundation.directory.entity.compatible;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.utils.JsonSerializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The unit test for the compatibility of ModelServiceInstance 1.2 vs. 1.1
 */
public class ModelServiceInstanceCompatibleTest {
    private static final Date date = new Date();

    /* for 1.1 */
    private static ModelServiceInstance11 testInstance11;
    private static String testInstance11Str;

    /* for 1.2 */
    private static ModelServiceInstance testInstance12;
    private static String testInstance12Str;



    @BeforeClass
    public static void setUp() throws Exception{
        Map<String, String> metadata = new HashMap<>();
        metadata.put("datacenter", "dc01");
        metadata.put("solution", "core");

        testInstance11 = new ModelServiceInstance11("odrm", "192.168.1.1", "192.168.1.1", "http://cisco.com/vbo/odrm/setupsession",
                OperationalStatus.UP, "192.168.1.1", 12345, date,
                date, metadata);
        testInstance11.setHeartbeatTime(date);
        testInstance11Str = new String(JsonSerializer.serialize(testInstance11));

        /* only different is no "port" in constructor */
        testInstance12 = new ModelServiceInstance("odrm", "192.168.1.1", "192.168.1.1", "http://cisco.com/vbo/odrm/setupsession",
                OperationalStatus.UP, "192.168.1.1",date,
                date, metadata);
        testInstance12.setHeartbeatTime(date);
        testInstance12Str = new String(JsonSerializer.serialize(testInstance12));

        System.out.printf("1.1=%s%n", testInstance11Str);
        System.out.printf("1.2=%s%n", testInstance12Str);

        assertTrue(testInstance11Str.indexOf(",\"port\":12345") > 0);
        assertEquals(testInstance12Str, testInstance11Str.replace(",\"port\":12345", "")); //no port in

    }

    @Test
    public void testStr11ToInstance12() throws Exception {
        // 1.2 str -> 1.2
        ModelServiceInstance instance12 = JsonSerializer.deserialize(testInstance12Str.getBytes(), ModelServiceInstance.class);
        assertEquals(testInstance12Str,new String(JsonSerializer.serialize(instance12)));

        // 1.1 str -> 1.2
        instance12 = JsonSerializer.deserialize(testInstance11Str.getBytes(), ModelServiceInstance.class);
        assertEquals(testInstance12Str,new String(JsonSerializer.serialize(instance12)));
    }

    @Test
    public void testStr12toInstance11() throws Exception {
        // 1.1 str -> 1.1
        ModelServiceInstance11 instance11 = JsonSerializer.deserialize(testInstance11Str.getBytes(), ModelServiceInstance11.class);
        assertEquals(12345, instance11.getPort());
        assertEquals(testInstance11Str,new String(JsonSerializer.serialize(instance11)));

        // 1.2 str -> 1.1
        instance11 = JsonSerializer.deserialize(testInstance12Str.getBytes(), ModelServiceInstance11.class);
        assertEquals(0, instance11.getPort()); //12345 is missing
        assertEquals(testInstance11Str.replace("12345","0"),new String(JsonSerializer.serialize(instance11)));


    }

}
