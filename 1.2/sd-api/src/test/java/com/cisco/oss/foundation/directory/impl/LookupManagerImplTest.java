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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.ServiceDirectory;
import com.cisco.oss.foundation.directory.client.DirectoryServiceRestfulClient;
import com.cisco.oss.foundation.directory.entity.InstanceChange;
import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.lookup.CachedDirectoryLookupService;
import com.cisco.oss.foundation.directory.lookup.CachedLookupManagerImpl;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;
import com.cisco.oss.foundation.directory.registration.HeartbeatDirectoryRegistrationService;

public class LookupManagerImplTest {

    public static final Logger LOGGER = LoggerFactory.getLogger(LookupManagerImplTest.class);

    @Test
    public void test01 () throws InterruptedException {
        ServiceDirectory.getServiceDirectoryConfig().setProperty(HeartbeatDirectoryRegistrationService.SD_API_HEARTBEAT_INTERVAL_PROPERTY, 1);
        ServiceDirectory.getServiceDirectoryConfig().setProperty(CachedDirectoryLookupService.SD_API_CACHE_SYNC_INTERVAL_PROPERTY, 1);

        final String serviceName = "odrm";
        final String instanceAddress = "192.168.2.3";
        final String keyName = "solution";

        final Date date = new Date();

        Map<String, String> metadata = new HashMap<>();
        metadata.put("datacenter", "dc01");
        metadata.put("solution", "core");
        List<ModelServiceInstance> instances = new ArrayList<>();
        final ModelServiceInstance instance = new ModelServiceInstance("odrm", "192.168.2.3", "192.168.2.3", "http://cisco.com/vbo/odrm/setupsession",
                OperationalStatus.UP, "192.168.2.3",date,
                date, metadata);
        instance.setHeartbeatTime(date);
        instances.add(instance);
        final ModelService result = new ModelService("odrm", "odrm", date);
        result.setServiceInstances(instances);

        final ModelMetadataKey keyResult = new ModelMetadataKey(keyName, keyName, date, date);
        keyResult.setServiceInstances(instances);

        final AtomicInteger serviceInvoked = new AtomicInteger(0);
        final AtomicInteger keyInvoked = new AtomicInteger(0);
        final AtomicInteger serviceChangingInvoked = new AtomicInteger(0);

        CachedLookupManagerImpl impl = new CachedLookupManagerImpl(new CachedDirectoryLookupService(new DirectoryServiceRestfulClient(){
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
                if (keyInvoked.get()==1) {
                    LOGGER.debug("When first execute getMetadataKey by {} return {}",keyName, keyResult.getServiceInstances());
                    return keyResult;
                }else{
                    Map<String, String> metadata = new HashMap<>();
                    metadata.put("datacenter", "dc03");
                    metadata.put("solution", "core03");
                    List<ModelServiceInstance> instances = new ArrayList<>();
                    ModelServiceInstance instance = new ModelServiceInstance("odrm", "192.168.2.3", "192.168.2.3", "http://cisco.com/vbo/odrm/setupsession/v03",
                            OperationalStatus.UP, "192.168.2.3", date,
                            date, metadata);
                    instance.setHeartbeatTime(date);
                    instances.add(instance);
                    ModelMetadataKey key = new ModelMetadataKey(keyName, keyName, date, date);
                    key.setServiceInstances(instances);
                    LOGGER.debug("When {} execute getMetadataKey by {} return {}",keyInvoked.get(),keyName, key);
                    return key;
                }
            }

            @Override
            public List<InstanceChange<ModelServiceInstance>> lookupChangesSince(String serviceName, long since) {
                final int count = serviceChangingInvoked.incrementAndGet();
                Assert.assertTrue(serviceName.equals("odrm"));
                List<InstanceChange<ModelServiceInstance>> result = new ArrayList<>();
                if (count==1) {
                    Map<String, String> metadata = new HashMap<>();
                    metadata.put("datacenter", "dc02");
                    metadata.put("solution", "core02");
                    ModelServiceInstance newInstance = new ModelServiceInstance("odrm", "192.168.2.3", "192.168.2.3", "http://cisco.com/vbo/odrm/setupsession/v02",
                            OperationalStatus.UP, "192.168.2.3", date,
                            date, metadata);
                    newInstance.setHeartbeatTime(date);
                    result.add(new InstanceChange<>(since, instance.getServiceName(), InstanceChange.ChangeType.META,
                            instance, newInstance));
                    result.add(new InstanceChange<>(since, instance.getServiceName(), InstanceChange.ChangeType.URL,
                            instance, newInstance));
                    LOGGER.debug("Mock lookupChangesSince '{}' is called first time since {}, return mock list {}", serviceName, since, result);
                }else{
                    LOGGER.debug("Mock lookupChangesSince '{}' is called {} since {},  return empty list ",serviceName,count,since);
                }
                return result;
            }

        }));


        impl.start();

        ServiceInstanceQuery query = new ServiceInstanceQuery().getEqualQueryCriterion("solution", "core");
        try {
            Assert.assertEquals(impl.getAllInstances(serviceName).get(0).getAddress(), instanceAddress);
            Assert.assertTrue(impl.getAllInstances(serviceName, query).get(0).getAddress().equals(instanceAddress));
            Assert.assertTrue(impl.getAllInstancesByMetadataKey(query).get(0).getAddress().equals(instanceAddress));
        } catch (ServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Assert.assertEquals(serviceInvoked.get(), 1);
        Assert.assertEquals(keyInvoked.get(), 1);


        // wait for cache sync.
        LOGGER.info("Start sleep.....");
        TimeUnit.SECONDS.sleep(1L);
        LOGGER.info("finished sleep.....");

        List<String> list = new ArrayList<>();
        list.add("core02");
        list.add("core03");
        query = new ServiceInstanceQuery().getInQueryCriterion("solution", list);
        try {
            Assert.assertEquals(instanceAddress, impl.getAllInstances(serviceName).get(0).getAddress());
            Assert.assertEquals("http://cisco.com/vbo/odrm/setupsession/v02", impl.getAllInstances(serviceName, query).get(0).getUri());
            Assert.assertTrue(impl.getAllInstancesByMetadataKey(query).get(0).getAddress().equals(instanceAddress));
            Assert.assertTrue(impl.getAllInstancesByMetadataKey(query).get(0).getUri().equals("http://cisco.com/vbo/odrm/setupsession/v03"));
        } catch (ServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        Assert.assertEquals(1,serviceInvoked.get());
        Assert.assertEquals(3, keyInvoked.get());
        Assert.assertTrue(serviceChangingInvoked.get()> 0);
    }
}
