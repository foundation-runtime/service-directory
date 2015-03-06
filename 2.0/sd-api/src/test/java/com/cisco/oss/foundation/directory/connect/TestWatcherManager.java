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
