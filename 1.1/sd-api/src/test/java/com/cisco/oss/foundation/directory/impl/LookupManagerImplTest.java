/**
 * Copyright 2014 Cisco Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cisco.oss.foundation.directory.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.ServiceDirectory;
import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationResult;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;

public class LookupManagerImplTest {

    public static final Logger LOGGER = LoggerFactory.getLogger(LookupManagerImplTest.class);

    @Test
    public void test01(){
        ServiceDirectory.getServiceDirectoryConfig().setProperty(HeartbeatDirectoryRegistrationService.SD_API_HEARTBEAT_INTERVAL_PROPERTY, 1);
        ServiceDirectory.getServiceDirectoryConfig().setProperty(CachedDirectoryLookupService.SD_API_CACHE_SYNC_INTERVAL_PROPERTY, 1);

        final String serviceName = "odrm";
        final String instanceId = "192.168.2.3-8901";
        final String keyName = "solution";

        final Date date = new Date();

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("datacenter", "dc01");
        metadata.put("solution", "core");
        List<ModelServiceInstance> instances = new ArrayList<ModelServiceInstance>();
        ModelServiceInstance instance = new ModelServiceInstance("odrm", "192.168.2.3-8901", "192.168.2.3-8901", "http://cisco.com/vbo/odrm/setupsession",
                OperationalStatus.UP, null, 0, date,
                date, metadata);
        instance.setHeartbeatTime(date);
        instances.add(instance);
        final ModelService result = new ModelService("odrm", "odrm", date);
        result.setServiceInstances(instances);

        final ModelMetadataKey keyResult = new ModelMetadataKey(keyName, keyName, date, date);
        keyResult.setServiceInstances(instances);

//        final ProvidedServiceInstance instance = new ProvidedServiceInstance("odrm", "192.168.7.4", 8901);
//        instance.setMonitorEnabled(true);
//        instance.setStatus(OperationalStatus.UP);
//        instance.setUri("http://cisco.com/vbo/odrm/setupsession");
//        Map<String, String> metadata = new HashMap<String, String>();
//        metadata.put("datacenter", "dc01");
//        metadata.put("solution", "core");
//        instance.setMetadata(metadata);
//
//        final ProvidedServiceInstance instance2 = new ProvidedServiceInstance("odrm", "192.168.7.4", 8902);
//        instance2.setMonitorEnabled(false);
//        instance2.setStatus(OperationalStatus.UP);
//        instance2.setUri("http://cisco.com/vbo/odrm/setupsession");
//        instance2.setMetadata(metadata);
//
        final AtomicInteger serviceInvoked = new AtomicInteger(0);
        final AtomicInteger keyInvoked = new AtomicInteger(0);
        final AtomicInteger serviceChangingInvoked = new AtomicInteger(0);
        final AtomicInteger keyChangingInvoked = new AtomicInteger(0);
//        final AtomicInteger unregisterInvoked = new AtomicInteger(0);

        CachedLookupManagerImpl impl = new CachedLookupManagerImpl(new CachedDirectoryLookupService(new DirectoryServiceClient(){
            @Override
            public ModelService lookupService(String serviceName) {
                Assert.assertTrue(serviceName.equals("odrm"));
                serviceInvoked.incrementAndGet();
                return result;
            }

            @Override
            public ModelMetadataKey getMetadataKey(String keyName) {
                Assert.assertTrue(keyName.equals("solution"));
                keyInvoked.incrementAndGet();
                return keyResult;
            }

            @Override
            public Map<String, OperationResult<ModelService>> getChangedServices(Map<String, ModelService> services) {
                serviceChangingInvoked.incrementAndGet();
                Assert.assertTrue(services.containsKey("odrm"));
                Map<String, OperationResult<ModelService>> rr = new HashMap<String, OperationResult<ModelService>>();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Map<String, String> metadata = new HashMap<String, String>();
                metadata.put("datacenter", "dc02");
                metadata.put("solution", "core02");
                List<ModelServiceInstance> instances = new ArrayList<ModelServiceInstance>();
                ModelServiceInstance instance = new ModelServiceInstance("odrm", "192.168.2.3-8901", "192.168.2.3-8901", "http://cisco.com/vbo/odrm/setupsession/v02",
                        OperationalStatus.UP, null, 0, date,
                        date, metadata);
                instance.setHeartbeatTime(date);
                instances.add(instance);
                ModelService service = new ModelService("odrm", "odrm", date);
                service.setServiceInstances(instances);
                rr.put("odrm", new OperationResult<ModelService>(true, service, null));
                return rr;
            }

            @Override
            public Map<String, OperationResult<ModelMetadataKey>> getChangedMetadataKeys(Map<String, ModelMetadataKey> keys) {
                keyChangingInvoked.incrementAndGet();
                Assert.assertTrue(keys.containsKey("solution"));
                Map<String, OperationResult<ModelMetadataKey>> rr = new HashMap<String, OperationResult<ModelMetadataKey>>();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Map<String, String> metadata = new HashMap<String, String>();
                metadata.put("datacenter", "dc03");
                metadata.put("solution", "core03");
                List<ModelServiceInstance> instances = new ArrayList<ModelServiceInstance>();
                ModelServiceInstance instance = new ModelServiceInstance("odrm", "192.168.2.3-8901", "192.168.2.3-8901", "http://cisco.com/vbo/odrm/setupsession/v03",
                        OperationalStatus.UP, null, 0, date,
                        date, metadata);
                instance.setHeartbeatTime(date);
                instances.add(instance);
                ModelMetadataKey key = new ModelMetadataKey(keyName, keyName, date, date);
                key.setServiceInstances(instances);

                rr.put("solution", new OperationResult<ModelMetadataKey>(true, key, null));
                return rr;
            }
        }));


        impl.start();

        ServiceInstanceQuery query = new ServiceInstanceQuery().getEqualQueryCriterion("solution", "core");
        try {
            Assert.assertEquals(impl.getAllInstances(serviceName).get(0).getInstanceId(), instanceId);
            Assert.assertTrue(impl.getAllInstances(serviceName, query).get(0).getInstanceId().equals(instanceId));
            Assert.assertTrue(impl.getAllInstancesByMetadataKey(query).get(0).getInstanceId().equals(instanceId));
            Assert.assertTrue(impl.getInstance(serviceName, instanceId).getInstanceId().equals(instanceId));
        } catch (ServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Assert.assertEquals(serviceInvoked.get(), 1);
        Assert.assertEquals(keyInvoked.get(), 1);

        // wait for cache sync.
        LOGGER.info("Start sleep.....");
        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        LOGGER.info("finished sleep.....");

        List<String> list = new ArrayList<String>();
        list.add("core02");
        list.add("core03");
        query = new ServiceInstanceQuery().getInQueryCriterion("solution", list);
        try {
            Assert.assertEquals(impl.getAllInstances(serviceName).get(0).getInstanceId(), instanceId);
            Assert.assertTrue(impl.getAllInstances(serviceName, query).get(0).getUri().equals("http://cisco.com/vbo/odrm/setupsession/v02"));
            Assert.assertTrue(impl.getAllInstancesByMetadataKey(query).get(0).getInstanceId().equals(instanceId));
            Assert.assertTrue(impl.getAllInstancesByMetadataKey(query).get(0).getUri().equals("http://cisco.com/vbo/odrm/setupsession/v03"));
            Assert.assertTrue(impl.getInstance(serviceName, instanceId).getInstanceId().equals(instanceId));
            Assert.assertTrue(impl.getInstance(serviceName, instanceId).getMetadata().get("solution").equals("core02"));
        } catch (ServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        Assert.assertEquals(serviceInvoked.get(), 1);
        Assert.assertEquals(keyInvoked.get(), 1);
        Assert.assertTrue(serviceChangingInvoked.get()> 0);
        Assert.assertTrue(keyChangingInvoked.get()> 0);
    }
}
