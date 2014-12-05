package com.cisco.oss.foundation.directory.connect;

import junit.framework.Assert;

import org.junit.Test;

import com.cisco.oss.foundation.directory.async.Watcher;
import com.cisco.oss.foundation.directory.async.WatcherManager;
import com.cisco.oss.foundation.directory.proto.ServiceInstanceOperate;

public class TestWatcherManager {
	
	@Test
	public void testWatcher(){
		WatcherManager mgr = new WatcherManager();
		mgr.addWatcher("svc1", new Watcher(){

			@Override
			public void process(String name, 
					ServiceInstanceOperate operate) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		mgr.addWatcher("svc1", new Watcher(){

			@Override
			public void process(String name, 
					ServiceInstanceOperate operate) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		Assert.assertEquals(2, mgr.getWatchers("svc1").size());
		Assert.assertEquals(null, mgr.getWatchers("svc2"));
		mgr.cleanup();
		Assert.assertEquals(null, mgr.getWatchers("svc1"));
		
	}
}
