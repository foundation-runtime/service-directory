package com.cisco.oss.foundation.directory.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit Test for ServiceDirectoryVersion
 */
public class ServiceDirectoryVersionTest {
    @Test
    public void testGetVersion(){
        assertEquals("1.2.0-0-SNAPSHOT", ServiceDirectoryVersion.getVersion());
    }
}
