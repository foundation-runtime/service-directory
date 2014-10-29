package com.cisco.oss.foundation.directory.connect;

import junit.framework.Assert;

import org.junit.Test;

import com.cisco.oss.foundation.directory.async.Watcher;
import com.cisco.oss.foundation.directory.async.WatcherManager;
import com.cisco.oss.foundation.directory.entity.WatcherType;
import com.cisco.oss.foundation.directory.proto.ServiceInstanceOperate;

public class TestWatcherManager {
	
	@Test
	public void testWatcher(){
		WatcherManager mgr = new WatcherManager();
		mgr.addWatcher("svc1", WatcherType.SERVICE, new Watcher(){

			@Override
			public void process(String name, WatcherType type,
					ServiceInstanceOperate operate) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		mgr.addWatcher("svc1", WatcherType.SERVICE, new Watcher(){

			@Override
			public void process(String name, WatcherType type,
					ServiceInstanceOperate operate) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		Assert.assertEquals(2, mgr.getWatchers("svc1", WatcherType.SERVICE).size());
		Assert.assertEquals(null, mgr.getWatchers("svc2", WatcherType.SERVICE));
		mgr.cleanup();
		Assert.assertEquals(null, mgr.getWatchers("svc1", WatcherType.SERVICE));
		
	}
}
