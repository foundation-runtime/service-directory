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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.exception.ErrorCode;

/**
 * ServiceInstance util methods.
 *
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
     * Transfer a ModelServiceInstance to a ServiceInstance object.
     *
     * It will do the deep clone for the metadata.
     *
     * @param modelInstance
     *            the ModelServiceInstance object.
     * @return the ServiceInstance Object.
     */
    public static ServiceInstance transferFromModelServiceInstance(
            ModelServiceInstance modelInstance) {
        Map<String, String> meta = new HashMap<String, String>();
        if (modelInstance.getMetadata() != null) {
            for (Entry<String, String> en : modelInstance.getMetadata()
                    .entrySet()) {
                meta.put(en.getKey(), en.getValue());
            }
        }
        return new ServiceInstance(modelInstance.getServiceName(),
                modelInstance.getInstanceId(), modelInstance.getUri(),
                modelInstance.isMonitorEnabled(), modelInstance.getStatus(),
                modelInstance.getAddress(), modelInstance.getPort(), meta);
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
    public static boolean isMustFieldValid(String field, String reg) {
        if (field == null || field.length() == 0) {
            return false;
        }
        return Pattern.matches(reg, field);
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
    public static boolean isOptionalFieldValid(String field, String reg) {
        if (field == null || field.length() == 0) {
            return true;
        }
        return Pattern.matches(reg, field);
    }

    /**
     * Validate the instance service name.
     *
     * @param name
     *            the service name String.
     * @return OK if matched the name pattern.
     */
    public static ErrorCode isNameValid(String name) {
        if (!isMustFieldValid(name, nameRegEx)) {
            return ErrorCode.SERVICE_INSTANCE_NAME_FORMAT_ERROR;
        }
        return ErrorCode.OK;
    }

    /**
     * Validate the Service Instance address.
     *
     * It must be the ip address or hostname of the node.
     *
     * @param address
     *            the address.
     * @return OK if it is not null or an empty string.
     */
    public static ErrorCode isAddressValid(String address) {
        //if (!isMustFieldValid(address, ipRegEx)) {
    	 if (address == null || address.length() == 0) {
            return ErrorCode.SERVICE_INSTANCE_ADDRESS_FORMAT_ERROR;
        }
        return ErrorCode.OK;
    }

    /**
     * Validate the instance port.
     *
     * @param port
     *            the port number.
     * @return OK if in the port range.
     */
    public static ErrorCode isPortValid(int port) {
        if (port > 65535 || port < 1) {
            return ErrorCode.SERVICE_INSTANCE_PORT_FORMAT_ERROR;
        }
        return ErrorCode.OK;
    }

    /**
     * Validate the instance id.
     *
     * @param id
     *            the id String
     * @return OK if matched the id pattern.
     */
    public static ErrorCode isIdValid(String id) {
        if (!isMustFieldValid(id, idRegEx)) {
            return ErrorCode.SERVICE_INSTANCE_ID_FORMAT_ERROR;
        }
        return ErrorCode.OK;
    }

    /**
     * Validate the ServiceInstance URI.
     *
     * @param uri
     *            the URI String.
     * @return true if it is valid.
     */
    public static ErrorCode isUriValid(String uri) {
        if (uri == null || uri.isEmpty()) {
            return ErrorCode.SERVICE_INSTANCE_URI_FORMAT_ERROR;
        }
        if (!isMustFieldValid(uri, urlRegEx) || !isValidBrace(uri)) {
            return ErrorCode.SERVICE_INSTANCE_URI_FORMAT_ERROR;
        }
        return ErrorCode.OK;
    }

    /**
     * Validate the ProvidedServiceInstance.
     *
     * @param serviceInstance
     *            the ServiceInstance.
     * @return OK if all ServiceInstance fields are valid.
     */
    public static ErrorCode validateProvidedServiceInstance(
            ProvidedServiceInstance serviceInstance) {

        ErrorCode retstr = isNameValid(serviceInstance.getServiceName());
        if (retstr != ErrorCode.OK) {
            return retstr;
        }

        retstr = isUriValid(serviceInstance.getUri());
        if (retstr != ErrorCode.OK) {
            return retstr;
        }

        retstr = isPortValid(serviceInstance.getPort());
        if (retstr != ErrorCode.OK) {
            return retstr;
        }

		retstr = isAddressValid(serviceInstance.getAddress());
		if (retstr != ErrorCode.OK) {
			return retstr;
		}	
		 
		retstr = isIdValid(serviceInstance.getProviderId());
		if (retstr != ErrorCode.OK) {
			return retstr;

		}

        Map<String, String> metadata = serviceInstance.getMetadata();
        if (metadata != null && metadata.size() > 0 ) {
            Iterator<Entry<String, String>> itor = metadata.entrySet()
                    .iterator();
            while (itor.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) itor
                        .next();
                if (!isOptionalFieldValid(entry.getKey(), metaKeyRegEx)) {
                    return ErrorCode.SERVICE_INSTANCE_METAKEY_FORMAT_ERROR;
                }
            }
        }
        return ErrorCode.OK;
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
