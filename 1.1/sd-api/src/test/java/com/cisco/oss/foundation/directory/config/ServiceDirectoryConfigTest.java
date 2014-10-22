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
