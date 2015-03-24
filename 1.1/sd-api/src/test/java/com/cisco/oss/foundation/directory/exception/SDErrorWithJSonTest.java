package com.cisco.oss.foundation.directory.exception;

import static com.cisco.oss.foundation.directory.utils.JsonSerializer.deserialize;
import static com.cisco.oss.foundation.directory.utils.JsonSerializer.serialize;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Created by alex on 3/24/15.
 */
public class SDErrorWithJSonTest {

    @Test
    public void testServiceDirectoryError() throws Exception{
        ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_ALREADY_EXIST);
        String body = new String(serialize(error));
        System.out.println(body);
        ServiceDirectoryError errorBack = deserialize(body.getBytes(),
                ServiceDirectoryError.class);
        assertEquals("The Service already exists.",errorBack.getErrorMessage());
    }
}
