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
package com.cisco.oss.foundation.directory.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.entity.compatible.ModelServiceInstance11;
import com.cisco.oss.foundation.directory.entity.compatible.ServiceInstance11;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceException;

/**
 * ServiceInstance util methods.
 *
 */
public class ServiceInstanceUtils {

    public static final String nameRegEx = "^[0-9a-zA-Z][\\w-.:]{0,127}$";
    public static final String idRegEx = "^[0-9a-zA-Z][\\w-.]{0,63}$";
    public static final String urlRegEx = "^[0-9a-zA-Z{}][^\\s]{0,1023}$";
    public static final String hostnameRegEx = "^[0-9a-zA-Z][\\w-.]{0,253}[0-9a-zA-Z]$";
    public static final String versionRegEx = "^[0-9a-zA-Z][\\w-.]{0,127}$";
    public static final String metaKeyRegEx = "^[0-9a-zA-Z][\\w-.]{0,127}$";
    public static final String ipRegEx = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    /**
     * Convert a ModelServiceInstance to a ServiceInstance object.
     *
     * It will do the deep clone for the metadata.
     *
     * @param modelInstance
     *            the ModelServiceInstance object.
     * @return the ServiceInstance Object.
     */
    public static ServiceInstance toServiceInstance(
            ModelServiceInstance modelInstance) {
        if (modelInstance==null) throw new NullPointerException();
        Map<String, String> meta = new HashMap<>();
        if (modelInstance.getMetadata() != null) {
            for (Entry<String, String> en : modelInstance.getMetadata()
                    .entrySet()) {
                meta.put(en.getKey(), en.getValue());
            }
        }
        return new ServiceInstance(modelInstance.getServiceName(),
                modelInstance.getUri(),
                modelInstance.isMonitorEnabled(), modelInstance.getStatus(),
                modelInstance.getAddress(),meta, modelInstance.getPort(), modelInstance.getTls_port(), modelInstance.getProtocol());
    }

    /**
     * convert 1.1 modelServiceInstance ot serviceInstance
     * @param modelInstance the ModelServiceInstance11 object
     * @return the ServiceInstance11 object
     */
    public static ServiceInstance11 toServiceInstance11(ModelServiceInstance11 modelInstance){
        if (modelInstance==null) throw new NullPointerException();
        Map<String, String> meta = new HashMap<>();
        if (modelInstance.getMetadata() != null) {
            for (Entry<String, String> en : modelInstance.getMetadata()
                    .entrySet()) {
                meta.put(en.getKey(), en.getValue());
            }
        }
        return new ServiceInstance11(modelInstance.getServiceName(),
                modelInstance.getInstanceId(),
                modelInstance.getUri(),
                modelInstance.isMonitorEnabled(), modelInstance.getStatus(),
                modelInstance.getAddress(), modelInstance.getPort(), meta);
    }

    /**
     * Create new copy of ModelServiceInstance from the original one.
     */
    public static ModelServiceInstance copyModelInstFrom(ModelServiceInstance original) {
        ModelServiceInstance copied = new ModelServiceInstance();
        copied.setServiceName(original.getServiceName());
        copied.setAddress(original.getAddress());
        copied.setStatus(original.getStatus());
        copied.setUri(original.getUri());
        copied.setId(original.getId());
        copied.setInstanceId(original.getInstanceId());
        copied.setMonitorEnabled(original.isMonitorEnabled());
        copied.setCreateTime(original.getCreateTime());
        copied.setModifiedTime(original.getModifiedTime());
        copied.setHeartbeatTime(original.getHeartbeatTime());
        Map<String,String> newMeta = new HashMap<>();
        Map<String,String> oldMeta = original.getMetadata();
        if (oldMeta!=null) newMeta.putAll(oldMeta);
        copied.setMetadata(newMeta);
        return copied;
    }

    /**
     * Validate the required field String against the regex.
     *
     * @param field
     *            the field value String.
     * @param reg
     *            the regex.
     * @return true if matched.
     */
    public static boolean validateRequiredField(String field, String reg) {
        return !(field == null || field.isEmpty()) && Pattern.matches(reg, field);
    }

    /**
     * Validate the optional field String against regex.
     *
     * @param field
     *            the field value String.
     * @param reg
     *            the regex.
     * @return true if field is empty of matched the pattern.
     */
    public static boolean validateOptionalField(String field, String reg) {
        return field == null || field.isEmpty() || Pattern.matches(reg, field);
    }

    /**
     * Validate the instance service name.
     *
     * @param name
     *            the service name String.
     * @throws ServiceException 
     */
    public static void validateServiceName(String name) throws ServiceException {
        if (!validateRequiredField(name, nameRegEx)) {
            throw new ServiceException(
                    ErrorCode.SERVICE_INSTANCE_NAME_FORMAT_ERROR);
        }
    }

    /**
     * Validate the Service Instance address.
     *
     * It must be the ip address or hostname of the node.
     *
     * @param address
     *            the address.
     * @throws ServiceException 
     */
    public static void validateAddress(String address) throws ServiceException {
        if (address == null || address.isEmpty()) {
            throw new ServiceException(
                    ErrorCode.SERVICE_INSTANCE_ADDRESS_FORMAT_ERROR);
        }
    }

    /**
     * Validate the instance port.
     *
     * @param port
     *            the port number.
     * @throws ServiceException 
     */
    public static void validatePort(int port) throws ServiceException {
        if (port > 65535 || port < 1) {
            throw new ServiceException(
                    ErrorCode.SERVICE_INSTANCE_PORT_FORMAT_ERROR);
        }
  }

    /**
     * Validate the instance id.
     *
     * @param id
     *            the id String
     * @throws ServiceException
     */
    @Deprecated
    public static void validateServiceInstanceID(String id) throws ServiceException {
        if (!validateRequiredField(id, idRegEx)) {
            throw new ServiceException(
                    ErrorCode.SERVICE_INSTANCE_ID_FORMAT_ERROR);
        }
    }

    /**
     * Validate the ServiceInstance URI.
     *
     * @param uri
     *            the URI String.
     * @throws ServiceException
     */
    public static void validateURI(String uri) throws ServiceException {
        if (uri == null || uri.isEmpty() || !validateRequiredField(uri, urlRegEx) || !isValidBrace(uri)) {
            throw new ServiceException(ErrorCode.SERVICE_INSTANCE_URI_FORMAT_ERROR);
        }
    }
    

    /**
     * Validate the ServiceInstance Metadata.
     *
     * @param metadata
     *            the service instance metadata map.
     * @throws ServiceException
     */
    public static void validateMetadata(Map<String, String> metadata) throws ServiceException {
        for ( String key : metadata.keySet()){
            if (!validateRequiredField(key, metaKeyRegEx)){
               throw new ServiceException(
                    ErrorCode.SERVICE_INSTANCE_METAKEY_FORMAT_ERROR,
                    ErrorCode.SERVICE_INSTANCE_METAKEY_FORMAT_ERROR.getMessageTemplate(),key
               );
            }
        }

    }

    /**
     * Validate the ProvidedServiceInstance.
     *
     * @param serviceInstance
     *            the ServiceInstance.
     * @throws ServiceException 
     */
    public static void validateProvidedServiceInstance(
            ProvidedServiceInstance serviceInstance) throws ServiceException {
        
        if (serviceInstance == null) {
            throw new ServiceException(
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "service instance");
        }
               
        validateServiceName(serviceInstance.getServiceName());
        validateURI(serviceInstance.getUri());
        validateAddress(serviceInstance.getAddress());
        
        if (serviceInstance.getMetadata()!=null) { //allow metadata as null
            validateMetadata(serviceInstance.getMetadata());
        }
    }
    
    
    /**
     * Validate if the registration/Lookup manager is started.
     *
     * @param isStarted
     *            boolean to indicate the registration/lookup manager is started or not
     * @throws ServiceException SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED 
     *         if the registration/lookup manager is not started.
     */
    public static void validateManagerIsStarted(boolean isStarted) throws ServiceException {
        if (!isStarted) {
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED);
        }
    }

    /**
     * Validate the url of the Instance.
     *
     * @param url
     *            the URL String.
     * @return false if brace is invalid or URL is empty.
     */
    private static boolean isValidBrace(String url) {
        if (null == url || url.trim().length() == 0) {
            return false;
        }

        boolean isInsideVariable = false;
        StringTokenizer tokenizer = new StringTokenizer(url, "{}", true);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals("{")) {
                // { is not allowed inside of a variable specification
                if (isInsideVariable) {
                    return false;
                }
                isInsideVariable = true;
            } else if (token.equals("}")) {
                // } must be preceded by {;
                if (!isInsideVariable) {
                    return false;
                }
                isInsideVariable = false;
            }
        }
        return true;
    }
}
