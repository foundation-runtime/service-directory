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

        assertEquals(true, ServiceDirectory.globeConfig().isCacheEnabled());
        assertEquals(true, ServiceDirectory.config().isCacheEnabled());

        ServiceDirectory.ServiceDirectoryConfig config = ServiceDirectory.config().setCacheEnabled(false);
        assertEquals(false,config.isCacheEnabled());
        assertEquals(true, ServiceDirectory.globeConfig().isCacheEnabled());

        ServiceDirectory.globeConfig().setCacheEnabled(false);
        assertEquals(false, ServiceDirectory.globeConfig().isCacheEnabled());
        assertEquals(false, ServiceDirectory.config().isCacheEnabled());

        assertEquals(false, config.isCacheEnabled());
        ServiceDirectory.config().setCacheEnabled(true);
        assertEquals(false, config.isCacheEnabled());
        config.setCacheEnabled(true);
        assertEquals(true, config.isCacheEnabled());
        assertEquals(false, ServiceDirectory.globeConfig().isCacheEnabled());
        ServiceDirectory.globeConfig().setCacheEnabled(true);  //need to set back, otherwise, ald 1.1 test will failed
        assertEquals(true, ServiceDirectory.globeConfig().isCacheEnabled());
    }

    @Test
    public void testHeartBearConfig(){
        //heart beat is enabled by default
        assertTrue(ServiceDirectory.config().isHeartBeatEnabled());
        assertTrue(ServiceDirectory.globeConfig().isHeartBeatEnabled());

        ServiceDirectory.ServiceDirectoryConfig config = ServiceDirectory.config();
        config.setHeartbeatEnabled(false);
        assertFalse(config.isHeartBeatEnabled()); //current config is false
        assertTrue(ServiceDirectory.globeConfig().isHeartBeatEnabled()); //globe is still true;

        ServiceDirectory.globeConfig().setHeartbeatEnabled(false); //set globe false

        assertFalse(ServiceDirectory.globeConfig().isHeartBeatEnabled()); //globe is false;
        assertFalse(ServiceDirectory.config().isHeartBeatEnabled());  //new config is false now;

        ServiceDirectory.globeConfig().setHeartbeatEnabled(true); //set globe back to true
    }

    @Test
    public void testSDBuildByConfig(){
        ServiceDirectory.config().build();
    }

    @Test
    public void testClientType(){
        //Default is restful
        assertEquals(ServiceDirectory.ServiceDirectoryConfig.ClientType.RESTFUL,ServiceDirectory.config().getClientType());

        //set to mock
        ServiceDirectory.ServiceDirectoryConfig config = ServiceDirectory.config();
        config.setClientType(ServiceDirectory.ServiceDirectoryConfig.ClientType.MOCK);
        assertEquals(ServiceDirectory.ServiceDirectoryConfig.ClientType.MOCK,config.getClientType());

    }
}
