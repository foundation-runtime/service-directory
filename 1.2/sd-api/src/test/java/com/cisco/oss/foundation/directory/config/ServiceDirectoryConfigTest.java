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

import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.ServiceDirectory;
import com.cisco.oss.foundation.directory.exception.ServiceException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    @Test
    public void testNew12Config(){

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
        ServiceDirectory.globeConfig().setCacheEnabled(true);
        assertEquals(true, ServiceDirectory.globeConfig().isCacheEnabled());
    }

    @Test
    public void testBuildByConfig(){

        LookupManager lookupMgr = ServiceDirectory.config().build().newLookupManager();
        lookupMgr.close(); //explicitly close

        try (LookupManager lookupManager2 = ServiceDirectory.config().build().newLookupManager()){
            assertTrue(lookupManager2.isStarted()); //started
        }
        //Auto-close OK

        lookupMgr = ServiceDirectory.config().setCacheEnabled(false).build().newLookupManager();
        try {
            assertTrue(lookupMgr.isStarted()); //started
            lookupMgr.close(); //explicitly close fail
            fail(); //can't go here
        }catch(ServiceException e){
            assertEquals(true, e.getCause() instanceof ClassCastException);
        }

        try {
            try (LookupManager lookupManager = ServiceDirectory.config().setCacheEnabled(false).build().newLookupManager()) {
                assertFalse(lookupMgr.isStarted()); //fail to start
            }//auto-close failed
            fail(); //can't go there, auto-close failed
        }catch(ServiceException e){
            assertEquals(true, e.getCause() instanceof ClassCastException);
        }

    }
}
