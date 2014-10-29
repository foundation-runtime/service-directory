package com.cisco.oss.foundation.directory.connect;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class TestDirectoryServers {

	@Test
	public void testGetServer(){
		DirectoryServers servers = new DirectoryServers();
		
		Assert.assertEquals("vcsdirsvc", servers.getNextDirectoryServer().getHostName());
		Assert.assertEquals(2013, servers.getNextDirectoryServer().getPort());
		
		List<String> list = new ArrayList<String>();
		list.add("localhost:8091");
		list.add("cisco.com:9991");
		list.add("192.168.1.2:9080");
		servers.setServers(list);;
		
		Map<String, Integer> hosts = new HashMap<String, Integer>();
		hosts.put("localhost", 8091);
		hosts.put("cisco.com", 9991);
		hosts.put("192.168.1.2", 9080);
		
		InetSocketAddress addr = servers.getNextDirectoryServer();
		Assert.assertEquals(hosts.get(addr.getHostName()).intValue(), addr.getPort());
		
		addr = servers.getNextDirectoryServer();
		Assert.assertEquals(hosts.get(addr.getHostName()).intValue(), addr.getPort());
		
		addr = servers.getNextDirectoryServer();
		Assert.assertEquals(hosts.get(addr.getHostName()).intValue(), addr.getPort());
		
		System.out.println(servers.getNextDirectoryServer());
		System.out.println(servers.getNextDirectoryServer());
		System.out.println(servers.getNextDirectoryServer());
	}
}
