package com.cisco.oss.foundation.directory.async;

import com.cisco.oss.foundation.directory.entity.WatcherType;
import com.cisco.oss.foundation.directory.proto.ServiceInstanceOperate;


public interface Watcher {

	public void process(String name, WatcherType type, ServiceInstanceOperate operate);
}
