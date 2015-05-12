package com.cisco.oss.foundation.directory.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.entity.InstanceChange;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.utils.HttpResponse;
import com.cisco.oss.foundation.directory.utils.HttpUtils;

import static org.junit.Assert.assertEquals;

import static com.cisco.oss.foundation.directory.utils.JsonSerializer.serialize;

/**
 * Test for lookup changes by using restful client
 */
public class LookupChangesTestByRestfulClient {

    private final static Logger LOG = LoggerFactory.getLogger(LookupChangesTestByRestfulClient.class);

    private final DirectoryServiceRestfulClient restfulClient = new DirectoryServiceRestfulClient();

    private final ModelServiceInstance instance;

    public LookupChangesTestByRestfulClient(){
        final Date date = new Date();
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("datacenter", "dc01");
        metadata.put("solution", "core");
        this.instance = new ModelServiceInstance("odrm", "192.168.2.3", "192.168.2.3", "http://cisco.com/vbo/odrm/setupsession",
                OperationalStatus.UP, null, 0, date,
                date, metadata);
        this.instance.setHeartbeatTime(date);
    }

    @Before
    public void setUp(){
        restfulClient.setInvoker(new DirectoryServiceRestfulClient.DirectoryHttpInvoker() {
            @Override
            public HttpResponse invoke(String uri, String payload, HttpUtils.HttpMethod method, Map<String, String> headers) {
                LOG.debug("invoke url:{} method:{} ", uri, method);
                List<InstanceChange<ModelServiceInstance>> list = new ArrayList<>();
                list.add(new InstanceChange<>(0L, "test", InstanceChange.ChangeType.Create,
                        null, instance));
                String json = "";
                try {
                    json = new String(serialize(list));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return new HttpResponse(200, json);
            }
        });
    }

    @Test
    public void testLookupChangesSince() throws InterruptedException {
        final long now = System.currentTimeMillis();
        TimeUnit.MILLISECONDS.sleep(10L);
        List<InstanceChange<ModelServiceInstance>> result = restfulClient.lookupChangesSince("test", now);
        assertEquals(1, result.size());
        LOG.debug("{}", result.get(0));
    }

}
