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

import org.junit.Assert;
import org.junit.Test;

import com.cisco.oss.foundation.directory.ServiceDirectory;

/**
 * TestCases to cover load configures from the config.properties file.
 * @author zuxiang
 *
 */
public class ServiceDirectoryConfigTest {

    @Test
    public void testGetProperty(){
        ServiceDirectoryConfig config = ServiceDirectory.getServiceDirectoryConfig();

        Assert.assertFalse(config.getBoolean("ddd"));
        Assert.assertTrue(config.getDouble("notexists", 89.1) == 89.1);
        try{
            config.getDouble("notexists");
        } catch(Exception e){
            Assert.assertTrue(e instanceof NoSuchElementException);
        }

        Assert.assertFalse(config.containsProperty("not_property"));
    }
}
