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
package com.cisco.oss.foundation.directory.lb;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.utils.ServiceInstanceUtils;

public class RoundRobinLoadBalancerTest {

    @Test
    public void test(){
        Date date = new Date();
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("node", "n1");
        List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
        ModelServiceInstance instance1 = new ModelServiceInstance("odrm", "192.168.1.1", "192.168.1.1", "http://cisco.com/",
                OperationalStatus.UP, "192.168.1.1", date, date,  metadata);
        ModelServiceInstance instance2 = new ModelServiceInstance("odrm", "192.168.1.2", "192.168.1.2", "http://cisco.com/",
                OperationalStatus.UP, "192.168.1.2", date, date,  metadata);

        instances.add(ServiceInstanceUtils.toServiceInstance(instance2));
        instances.add(ServiceInstanceUtils.toServiceInstance(instance1));

        MockLB lb = new MockLB();

        Map<String, Integer> count = new HashMap<String, Integer>();
        count.put("192.168.1.1", 0);
        count.put("192.168.1.2", 0);

        int i = 0;
        while(i < 100){
            ServiceInstance inst = lb.vote(instances);
            int v = count.get(inst.getAddress()) + 1;
            count.put(inst.getAddress(), v);
            i ++;
        }
        Assert.assertTrue(count.get("192.168.1.1") == 50);
        Assert.assertTrue(count.get("192.168.1.2")== 50);

    }

    private static class MockLB extends RoundRobinLoadBalancer{
    }
}
