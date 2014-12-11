package com.cisco.oss.foundation.directory.lb;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.impl.DirectoryLookupService;
import com.cisco.oss.foundation.directory.lb.RoundRobinLoadBalancer;

public class RoundRobinLoadBalancerTest {
	
	@Test
	public void test(){
		Date date = new Date();
		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put("node", "n1");
		List<ModelServiceInstance> instances = new ArrayList<ModelServiceInstance>();
		ModelServiceInstance instance1 = new ModelServiceInstance("odrm", "192.168.1.1-8901", "192.168.1.1-8901", "http://cisco.com/", 
				OperationalStatus.UP, null, 0, date, date,  metadata);
		ModelServiceInstance instance2 = new ModelServiceInstance("odrm", "192.168.1.1-8902", "192.168.1.1-8902", "http://cisco.com/", 
				OperationalStatus.UP, null, 0, date, date,  metadata);
		
		instances.add(instance2);
		instances.add(instance1);
		
		MockLB lb = new MockLB(null, instances);
		
		Map<String, Integer> count = new HashMap<String, Integer>();
		count.put("192.168.1.1-8901", 0);
		count.put("192.168.1.1-8902", 0);
		
		int i = 0;
		while(i < 100){
			ServiceInstance inst = lb.vote();
			int v = count.get(inst.getInstanceId()) + 1;
			count.put(inst.getInstanceId(), v);
			i ++;
		}
		Assert.assertTrue(count.get("192.168.1.1-8901") == 50);
		Assert.assertTrue(count.get("192.168.1.1-8902")== 50);
		
	}

	class MockLB extends RoundRobinLoadBalancer{
		private List<ModelServiceInstance> instances;
		public MockLB(DirectoryLookupService lookupService, List<ModelServiceInstance> instances) {
			super(lookupService);
			this.instances = instances;
		}

		@Override
		public List<ModelServiceInstance> getServiceInstanceList() {
			return instances;
		}
		
	}
}
