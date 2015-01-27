/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * The JSON parser for serializing and deserializing.
 *
 * It wrap the Jackson ObjectMapper to handle the JSON parsing.
 *
 * @author zuxiang
 *
 */
public class JsonSerializer {
    /**
     * The ObjectMapper object.
     */
    private final ObjectMapper mapper;

    /**
     * Constructor.
     */
    public JsonSerializer() {
        mapper = new ObjectMapper();
    }

    public ObjectMapper getObjectMapper(){
        return mapper;
    }

    /**
     * Deserialize from byte array.
     *
     * @param input
     *         the JSON String byte array.
     * @param classType
     *         the target Object class type.
     * @return
     *         the target Object instance.
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public Object deserialize(byte[] input, Class<?> classType)
            throws JsonParseException, JsonMappingException, IOException {
        return mapper.readValue(input, classType);
    }

    /**
     * Deserialize from byte array, it always used in the generic type object.
     *
     * @param input
     *         the JSON String byte array.
     * @param typeRef
     *         the target Object TypeReference.
     * @return
     *         the target Object instance.
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public Object deserialize(byte[] input, TypeReference<?> typeRef)
            throws JsonParseException, JsonMappingException, IOException {
        return mapper.readValue(input, typeRef);
    }

    /**
     * Serialize the Object to JSON String.
     *
     * @param instance
     *         the Object instance.
     * @return
     *         the JSON String byte array.
     * @throws JsonGenerationException
     * @throws JsonMappingException
     * @throws IOException
     */
    public byte[] serialize(Object instance) throws JsonGenerationException,
            JsonMappingException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mapper.writeValue(out, instance);
        return out.toByteArray();
    }

}
