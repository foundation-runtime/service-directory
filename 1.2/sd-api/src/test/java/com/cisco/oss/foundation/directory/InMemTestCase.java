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
package com.cisco.oss.foundation.directory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;
import com.cisco.oss.foundation.directory.test.TestServiceDirectoryManagerFactory;

public class InMemTestCase {
    @Before
    public void build() throws ServiceException {
        // For the Application do integration test, it just need to see the ServiceDirectoryManagerFactory configuration.
//        ServiceDirectory.getServiceDirectoryConfig().setProperty(ConfigurationConstants.SD_API_SERVICE_DIRECTORY_MANAGER_FACTORY_PROVIDER_PROPERTY,
//                "com.cisco.oss.foundation.directory.test.TestServiceDirectoryManagerFactory");

        // Set the ServiceDirectoryManagerFactory in runtime.
        ServiceDirectoryManagerFactory manager = new TestServiceDirectoryManagerFactory();
        ServiceDirectory.reinitServiceDirectoryManagerFactory(manager);
    }

    @Test
    public void testRegisterService() throws ServiceException{
        String serviceName = "InMemTest-01";
        ProvidedServiceInstance ist = createInstance(serviceName);
        ist.setStatus(OperationalStatus.UP);

        RegistrationManager register = ServiceDirectory.getRegistrationManager();
        LookupManager lookup = ServiceDirectory.getLookupManager();


        register.registerService(ist);
        ServiceInstance instance = lookup.lookupInstance(serviceName);

        Assert.assertEquals(ist.getProviderId(), instance.getInstanceId() );

        register.unregisterService(serviceName, ist.getProviderId());

        ServiceInstance instance1 = lookup.lookupInstance(serviceName);
        Assert.assertNull(instance1);
    }

    @Test
    public void testRegisterWithStatus() throws ServiceException{
        String serviceName = "InMemTest-01";
        ProvidedServiceInstance ist = createInstance(serviceName);

        RegistrationManager register = ServiceDirectory.getRegistrationManager();
        LookupManager lookup = ServiceDirectory.getLookupManager();

        ist.setStatus(OperationalStatus.UP);
        register.registerService(ist);
        ServiceInstance instance = lookup.lookupInstance(serviceName);

        Assert.assertEquals(instance.getInstanceId(), ist.getProviderId());

        register.unregisterService(serviceName, ist.getProviderId());
        ServiceInstance instance1 = lookup.lookupInstance(serviceName);
        Assert.assertNull(instance1);
    }

    @Test
    public void testUpdateServiceInstance() throws ServiceException{
        String serviceName = "MockSvc001";

        ProvidedServiceInstance instance = createInstance(serviceName);

        RegistrationManager register = ServiceDirectory.getRegistrationManager();
        instance.setStatus(OperationalStatus.UP);
        register.registerService(instance);


        LookupManager lookup = ServiceDirectory.getLookupManager();

        ServiceInstance sInstance= lookup.lookupInstance(serviceName);
        Assert.assertEquals(sInstance.getInstanceId(), instance.getProviderId());

        // Update the URI and metadata in the ServiceInstance.
        String newUri = "http://new.cisco.test:8081/test";
        instance.setUri(newUri);
        instance.getMetadata().put("keynew", "valuenew");
        register.updateService(instance);

        sInstance= lookup.lookupInstance(serviceName);
        Assert.assertEquals(sInstance.getInstanceId(), instance.getProviderId());
        Assert.assertEquals(newUri, sInstance.getUri());
        Assert.assertEquals(sInstance.getMetadata().get("keynew"), "valuenew");

        // Update the OperationalStatus in the ServiceInstance.
        register.updateServiceOperationalStatus(serviceName, instance.getProviderId(), OperationalStatus.DOWN);
        sInstance= lookup.lookupInstance(serviceName);
        // The ServiceInstance switched to DOWN, can not be found in LookupMananger.
        Assert.assertNull(sInstance);

        // cleanup.
        register.unregisterService(serviceName, instance.getProviderId());
        ServiceInstance sInstance1= lookup.lookupInstance(serviceName);
        Assert.assertNull(sInstance1);
    }

    @Test
    public void testQueryServiceInstances() throws ServiceException{
        String serviceName = "InMemTest-01";
        ProvidedServiceInstance ist = createInstance(serviceName);

        RegistrationManager register = ServiceDirectory.getRegistrationManager();
        LookupManager lookup = ServiceDirectory.getLookupManager();

        ist.getMetadata().put("key", "instance1");
        ist.setPort(8091);
        ist.setStatus(OperationalStatus.UP);
        register.registerService(ist);

        ist.getMetadata().put("key", "instance2");
        ist.setPort(8092);
        register.registerService(ist);

        ist.getMetadata().put("key", "instance3");
        ist.setPort(8093);
        register.registerService(ist);

        ist.getMetadata().put("key", "instance4");
        ist.setPort(8094);
        register.registerService(ist);

        ServiceInstanceQuery query = new ServiceInstanceQuery();
        query.addQueryCriterion(new ServiceInstanceQuery.EqualQueryCriterion("key", "instance1"));
        ServiceInstance inst = lookup.queryInstanceByName(serviceName, query);
        Assert.assertEquals("127.0.0.1-8091", inst.getInstanceId());

        query = new ServiceInstanceQuery();
        query.addQueryCriterion(new ServiceInstanceQuery.EqualQueryCriterion("key", "instance2"));
        inst = lookup.queryInstanceByName(serviceName, query);
        Assert.assertEquals("127.0.0.1-8092", inst.getInstanceId());

        query = new ServiceInstanceQuery();
        query.addQueryCriterion(new ServiceInstanceQuery.ContainQueryCriterion("key"));
        List<ServiceInstance> insts = lookup.queryInstancesByName(serviceName, query);
        Assert.assertEquals(4, insts.size());

        register.unregisterService(serviceName, "127.0.0.1-8091");
        register.unregisterService(serviceName, "127.0.0.1-8092");
        register.unregisterService(serviceName, "127.0.0.1-8093");
        register.unregisterService(serviceName, "127.0.0.1-8094");

        ServiceInstance instance1 = lookup.lookupInstance(serviceName);
        Assert.assertNull(instance1);
    }

    @Test
    public void testQueryServiceByKey() throws ServiceException{
        String serviceName1 = "InMemTest-01";
        String serviceName2 = "InMemTest-02";


        RegistrationManager register = ServiceDirectory.getRegistrationManager();
        LookupManager lookup = ServiceDirectory.getLookupManager();

        ProvidedServiceInstance ist1 = createInstance(serviceName1);
        ist1.getMetadata().put("datacenter", "dc01");
        ist1.setPort(8091);
        ist1.setStatus(OperationalStatus.UP);
        register.registerService(ist1);

        ProvidedServiceInstance ist2 = createInstance(serviceName1);
        ist2.getMetadata().put("datacenter", "dc02");
        ist2.setPort(8092);
        ist2.setStatus(OperationalStatus.UP);
        register.registerService(ist2);

        ProvidedServiceInstance ist3 = createInstance(serviceName2);
        ist3.getMetadata().put("datacenter", "dc01");
        ist3.setPort(8093);
        ist3.setStatus(OperationalStatus.UP);
        register.registerService(ist3);

        ProvidedServiceInstance ist4 = createInstance(serviceName2);
        ist4.getMetadata().put("datacenter", "dc02");
        ist4.setPort(8094);
        ist4.setStatus(OperationalStatus.UP);
        register.registerService(ist4);

        ServiceInstanceQuery query = new ServiceInstanceQuery().getEqualQueryCriterion("meta1", "value1");
        List<ServiceInstance> list1 = lookup.queryInstancesByKey(query);
        Assert.assertEquals(list1.size(), 4);

        query.getEqualQueryCriterion("datacenter", "dc01");
        List<ServiceInstance> list2 = lookup.queryInstancesByKey(query);
        Assert.assertEquals(list2.size(), 2);


        Map<String, Integer> countMap = new HashMap<String, Integer>();
        countMap.put(ist2.getProviderId(), 0);
        countMap.put(ist4.getProviderId(), 0);
        ServiceInstanceQuery query1 = new ServiceInstanceQuery().getEqualQueryCriterion("datacenter", "dc02");

        ServiceInstance queryInstance = lookup.queryInstanceByKey(query1);
        Integer count = countMap.get(queryInstance.getInstanceId());
        countMap.put(queryInstance.getInstanceId(), count + 1);

        queryInstance = lookup.queryInstanceByKey(query1);
        count = countMap.get(queryInstance.getInstanceId());
        countMap.put(queryInstance.getInstanceId(), count + 1);

        queryInstance = lookup.queryInstanceByKey(query1);
        count = countMap.get(queryInstance.getInstanceId());
        countMap.put(queryInstance.getInstanceId(), count + 1);

        queryInstance = lookup.queryInstanceByKey(query1);
        count = countMap.get(queryInstance.getInstanceId());
        countMap.put(queryInstance.getInstanceId(), count + 1);
        Assert.assertEquals(countMap.size(), 2);
        Assert.assertTrue(countMap.get(ist2.getProviderId()) == 2);
        Assert.assertTrue(countMap.get(ist4.getProviderId()) == 2);



        register.unregisterService(ist1.getServiceName(), ist1.getProviderId());
        register.unregisterService(ist2.getServiceName(), ist2.getProviderId());
        register.unregisterService(ist3.getServiceName(), ist3.getProviderId());
        register.unregisterService(ist4.getServiceName(), ist4.getProviderId());

        ServiceInstance instance1 = lookup.lookupInstance(serviceName1);
        Assert.assertNull(instance1);
    }

    @Test
    public void testGetInstances() throws ServiceException{
        String serviceName = "MockSvc001";
        String serviceName1 = "OtherSvc001";

        String key1 = "datacenter";
        String key2 = "version";

        ProvidedServiceInstance instance1 = createInstance(serviceName);
        instance1.setPort(8091);
        instance1.setStatus(OperationalStatus.DOWN);
        instance1.getMetadata().put(key1, "dc01");
        instance1.getMetadata().put(key2, "1.1.2");

        ProvidedServiceInstance instance2 = createInstance(serviceName);
        instance2.setPort(8092);
        instance2.setStatus(OperationalStatus.UP);
        instance2.getMetadata().put(key1, "dc02");

        ProvidedServiceInstance instance3 = createInstance(serviceName1);
        instance3.setPort(8093);
        instance3.setStatus(OperationalStatus.DOWN);
        instance3.getMetadata().put(key1, "dc01");

        ProvidedServiceInstance instance4 = createInstance(serviceName1);
        instance4.setPort(8094);
        instance4.setStatus(OperationalStatus.UP);
        instance4.getMetadata().put(key2, "1.1.1");

        RegistrationManager register = ServiceDirectory.getRegistrationManager();
        register.registerService(instance1);
        register.registerService(instance2);
        register.registerService(instance3);
        register.registerService(instance4);

        try {
            // Sleep 2 seconds wait for cache sync.....
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LookupManager lookup = ServiceDirectory.getLookupManager();
        List<ServiceInstance> instances = lookup.getAllInstances(serviceName);
        Assert.assertEquals(instances.size(), 2);

        ServiceInstance getedInstance = lookup.getInstance(serviceName, instance2.getProviderId());
        Assert.assertEquals(getedInstance.getUri(), instance2.getUri());
        Assert.assertEquals(getedInstance.getStatus(), instance2.getStatus());

        ServiceInstanceQuery query = new ServiceInstanceQuery().getEqualQueryCriterion("datacenter", "dc01");
        instances = lookup.getAllInstances(serviceName, query);
        Assert.assertEquals(instances.size(), 1);

        instances = lookup.getAllInstancesByKey(query);
        Assert.assertEquals(instances.size(), 2);

        // cleanup.
        register.unregisterService(serviceName, instance1.getProviderId());
        register.unregisterService(serviceName, instance2.getProviderId());
        register.unregisterService(serviceName1, instance3.getProviderId());
        register.unregisterService(serviceName1, instance4.getProviderId());

        try {
            // Sleep 2 seconds wait for cache sync.....
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ServiceInstance sInstance1 = lookup.lookupInstance(serviceName);
        Assert.assertNull(sInstance1);
    }

    private ProvidedServiceInstance createInstance(String serviceName) {

        String address = "127.0.0.1";
        int port = 8990;
        ProvidedServiceInstance si = new ProvidedServiceInstance(serviceName, address, port);
        si.setUri("http://www.sina.com.cn");
        Map<String, String> pair = new HashMap<String, String>();
        pair.put("meta1", "value1");
        pair.put("meta2", "value2");
        si.setMetadata(pair);
        si.setStatus(OperationalStatus.UP);
        return si;
    }
}
