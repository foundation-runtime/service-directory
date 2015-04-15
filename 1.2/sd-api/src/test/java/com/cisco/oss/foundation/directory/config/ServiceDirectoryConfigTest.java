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
package com.cisco.oss.foundation.directory.config;

import java.util.NoSuchElementException;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;

import com.cisco.oss.foundation.directory.ServiceDirectory;
import com.cisco.oss.foundation.directory.impl.ServiceDirectoryConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TestCases to cover load configures from the config.properties file.
 *
 */
public class ServiceDirectoryConfigTest {

    @Test
    public void testGetProperty(){
        Configuration config= ServiceDirectory.getServiceDirectoryConfig();

        assertFalse(config.getBoolean("ddd"));
        assertTrue(config.getDouble("notexists", 89.1) == 89.1);
        try{
            config.getDouble("notexists");
        } catch(Exception e){
            assertTrue(e instanceof NoSuchElementException);
        }

        assertFalse(config.containsKey("not_property"));
    }

    // -----------------------
    // New 1.2 Config tests
    // -----------------------

    @Test
    public void testCacheConfig(){

        assertEquals(true, ServiceDirectoryConfig.globeConfig().isCacheEnabled());
        assertEquals(true, ServiceDirectoryConfig.config().isCacheEnabled());

        ServiceDirectoryConfig config = ServiceDirectoryConfig.config().setCacheEnabled(false);
        assertEquals(false,config.isCacheEnabled());
        assertEquals(true, ServiceDirectoryConfig.globeConfig().isCacheEnabled());

        ServiceDirectoryConfig.globeConfig().setCacheEnabled(false);
        assertEquals(false, ServiceDirectoryConfig.globeConfig().isCacheEnabled());
        assertEquals(false, ServiceDirectoryConfig.config().isCacheEnabled());

        assertEquals(false, config.isCacheEnabled());
        ServiceDirectoryConfig.config().setCacheEnabled(true);
        assertEquals(false, config.isCacheEnabled());
        config.setCacheEnabled(true);
        assertEquals(true, config.isCacheEnabled());
        assertEquals(false, ServiceDirectoryConfig.globeConfig().isCacheEnabled());
        ServiceDirectoryConfig.globeConfig().setCacheEnabled(true);  //need to set back, otherwise, ald 1.1 test will failed
        assertEquals(true, ServiceDirectoryConfig.globeConfig().isCacheEnabled());
    }

    @Test
    public void testHeartBearConfig(){
        //heart beat is enabled by default
        assertTrue(ServiceDirectoryConfig.config().isHeartBeatEnabled());
        assertTrue(ServiceDirectoryConfig.globeConfig().isHeartBeatEnabled());

        ServiceDirectoryConfig config = ServiceDirectoryConfig.config();
        config.setHeartbeatEnabled(false);
        assertFalse(config.isHeartBeatEnabled()); //current config is false
        assertTrue(ServiceDirectoryConfig.globeConfig().isHeartBeatEnabled()); //globe is still true;

        ServiceDirectoryConfig.globeConfig().setHeartbeatEnabled(false); //set globe false

        assertFalse(ServiceDirectoryConfig.globeConfig().isHeartBeatEnabled()); //globe is false;
        assertFalse(ServiceDirectoryConfig.config().isHeartBeatEnabled());  //new config is false now;

        ServiceDirectoryConfig.globeConfig().setHeartbeatEnabled(true); //set globe back to true
    }

    @Test
    public void testSDBuildByConfig(){
        ServiceDirectoryConfig.config().build();
    }

    @Test
    public void testClientType(){
        //Default is restful
        assertEquals(ServiceDirectoryConfig.ClientType.RESTFUL, ServiceDirectoryConfig.config().getClientType());

        //set to mock
        ServiceDirectoryConfig config = ServiceDirectoryConfig.config();
        config.setClientType(ServiceDirectoryConfig.ClientType.DUMMY);
        assertEquals(ServiceDirectoryConfig.ClientType.DUMMY,config.getClientType());

    }
}
