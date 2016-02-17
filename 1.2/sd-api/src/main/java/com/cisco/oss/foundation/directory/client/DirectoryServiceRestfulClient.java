/**
 * Copyright 2014 Cisco Systems, Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cisco.oss.foundation.directory.client;

import com.cisco.oss.foundation.directory.ServiceDirectory;
import com.cisco.oss.foundation.directory.entity.*;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceDirectoryError;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.utils.HttpResponse;
import com.cisco.oss.foundation.directory.utils.HttpUtils;
import com.cisco.oss.foundation.directory.utils.HttpUtils.HttpMethod;
import com.cisco.oss.foundation.directory.utils.JsonSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cisco.oss.foundation.directory.ServiceDirectory.getServiceDirectoryConfig;
import static java.net.HttpURLConnection.*;

/**
 * This is the client object to invoke the remote service in ServiceDirectory Server Node.
 *
 *  It implements the HTTP transportation for ServiceDirectoryService,
 *  and hides the HTTPClient details from the upper application layer.
 *
 *
 */
public class DirectoryServiceRestfulClient implements DirectoryServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryServiceRestfulClient.class);

    /**
     * The http client read timeout property.
     */
    public static final String SD_API_HTTPCLIENT_READ_TIMEOUT_PROPERTY = "com.cisco.oss.foundation.directory.httpclient.read.timeout";

    /**
     * The http client default read timeout value.
     */
    public static final int SD_API_HTTPCLIENT_READ_TIMEOUT_DEFAULT = 5;

    /**
     * The Service Directory server FQDN property name.
     */
    public static final String SD_API_SD_SERVER_FQDN_PROPERTY = "com.cisco.oss.foundation.directory.server.fqdn";

    /**
     * The default Service Directory server FQDN name.
     */
    public static final String SD_API_SD_SERVER_FQDN_DEFAULT = "vcsdirsvc";

    /**
     * The Service Directory server port property name.
     */
    public static final String SD_API_SD_SERVER_PORT_PROPERTY = "com.cisco.oss.foundation.directory.server.port";

    /**
     * The Service Directory server port property name.
     */
    public static final String SD_API_SD_SERVER_HTTPS_PORT_PROPERTY = "com.cisco.oss.foundation.directory.server.https.port";

    
    /**
     * The default Service Directory server port.
     */
    public static final int SD_API_SD_SERVER_PORT_DEFAULT = 2013;

    
    /**
     * The default Service Directory server HTTPS port.
     */
    public static final int SD_API_SD_SERVER_HTTPS_PORT_DEFAULT = 0;
    
    /**
     * The HTTP invoker to access remote ServiceDirectory node.
     */
    private DirectoryHttpInvoker invoker;


    /**
     * The property to favor my datacenter or not
     */
    public static final String SD_API_DC_AFFINITY_PROPERTY = "com.cisco.oss.foundation.directory.favor.mydatacenter";
    public static final Boolean SD_API_DC_AFFINITY_DEFAULT = false;

    /**
     * The property to set my datacenter name
     */
    public static final String SD_API_MY_DC_NAME_PROPERTY = "com.cisco.oss.foundation.directory.mydatacenter.name";
    public static final String SD_API_MY_DC_NAME_DEFAULT = "datacenter1";

    /**
     * The MataData Key for referencing the datacenter name
     */
    public static final String SD_API_MY_DC_META_KEY= "datacenter";

    private final boolean favorMyDC ;
    private final String myDC ;

    /**
     * The property to control if keep instance without being removed on the server side even the max living time is exceed
     * It's true by default to remove the instance.
     */
    public static final String SD_API_AUTO_REMOVE_INST_ON_SERVER_SIDE_PROPERTY = "com.cisco.oss.foundation.directory.auto.remove.instance";
    public static final boolean SD_API_AUTO_REMOVE_INST_ON_SERVER_SIDE_DEFAULT = true;
    public static final String SD_API_AUTO_REMOVE_INST_ON_SERVER_SIDE_META_KEY = "autoRemoved";
    public final boolean autoRemove ;

    /**
     * Constructor.
     */
    public DirectoryServiceRestfulClient() {
        this.invoker = new DirectoryHttpInvoker();
        favorMyDC = getServiceDirectoryConfig().getBoolean(SD_API_DC_AFFINITY_PROPERTY, SD_API_DC_AFFINITY_DEFAULT);
        myDC = getServiceDirectoryConfig().getString(SD_API_MY_DC_NAME_PROPERTY, SD_API_MY_DC_NAME_DEFAULT);
        autoRemove = getServiceDirectoryConfig().getBoolean(SD_API_AUTO_REMOVE_INST_ON_SERVER_SIDE_PROPERTY,
                SD_API_AUTO_REMOVE_INST_ON_SERVER_SIDE_DEFAULT);
        if (favorMyDC) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Datacenter affinity is set.");
            }
            if (myDC == null || myDC.isEmpty()) {
                LOGGER.warn("Datacenter affinity is set without name.");
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Datacenter affinity is not set.");
            }
        }

        if (!autoRemove){
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("auto remove instance on server side is false, try to disable auto-remove instance on server side.");
            }
        }else{
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("auto remove instance on server side is true, server side will keep auto-removing instance by default.");
            }
        }
    }


    @Override
    public void registerInstance(ProvidedServiceInstance instance) throws ServiceException{
        instance = handleInstanceForHiddenMataData(instance);
        registerInst(instance);
    }

    private ProvidedServiceInstance handleInstanceForHiddenMataData(ProvidedServiceInstance instance) throws ServiceException {
       // need to copy the instance, because it's dangerous to change the instance owned by caller in the background.
        ProvidedServiceInstance copiedInstance = deserialize(serialize(instance), ProvidedServiceInstance.class);
        // set or update metadata
        Map<String,String> metaData = copiedInstance.getMetadata();
        if (metaData == null) {
           metaData = new HashMap<>();
        }
        metaData = handleHiddenMetaData(metaData);
        copiedInstance.setMetadata(metaData);
        return copiedInstance;
    }

    private Map<String,String> handleHiddenMetaData(Map<String,String> metaData){
        if(metaData==null) throw new IllegalArgumentException("metaData should not be null");
        if (favorMyDC) {
            metaData = addKeyValueToMetaData(metaData,SD_API_MY_DC_META_KEY,myDC);
        }
        if (!autoRemove){
            metaData = addKeyValueToMetaData(metaData,SD_API_AUTO_REMOVE_INST_ON_SERVER_SIDE_META_KEY,
                    Boolean.FALSE.toString());
        }
        return metaData;
    }
    private Map<String,String> addKeyValueToMetaData(Map<String,String> metaData,String key,String value){
        if (metaData==null) throw new IllegalArgumentException("metaData map should not be null");
        if (StringUtils.isEmpty(key)) throw new IllegalArgumentException("metaData key should not be empty");
        if (StringUtils.isEmpty(value)) throw new IllegalArgumentException("metaData value should not be empty");
        Map<String,String> copied= new HashMap<>();
        copied.putAll(metaData);
        copied.put(key,value);
        return copied;
    }

    private void registerInst(ProvidedServiceInstance instance){
        String body = serialize(instance);

        HttpResponse result = invoker.invoke(toInstanceUri(instance.getServiceName(), instance.getAddress()), body,
                HttpMethod.POST, addHeader());

        if (result.getHttpCode() != HTTP_CREATED) {
            handleHttpError(result.getHttpCode());
        }
    }

    private void handleHttpError(int httpCode) throws ServiceException{
        javax.ws.rs.core.Response.Status status = javax.ws.rs.core.Response.Status.fromStatusCode(httpCode);
        if (status!=null){
             throw new ServiceException(ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR,
                    "HTTP Code is not OK, code=%s, reason=[%s]", httpCode, status.getReasonPhrase());
        }else{
            throw new ServiceException(ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR,
                    "Unknown HTTP Code, code=%s", httpCode);
        }
    }

    private String toInstanceUri(String serviceName, String providerAddress) {
        return "/service/" + serviceName + "/" + providerAddress;
    }

    @Deprecated
    @Override
    public void updateInstance(ProvidedServiceInstance instance) throws ServiceException{
        String body = serialize(instance);

        HttpResponse result = invoker.invoke(toInstanceUri(instance.getServiceName(), instance.getAddress()), body,
                HttpMethod.PUT, addHeader());

        if (result.getHttpCode() != HTTP_CREATED) {
            handleHttpError(result.getHttpCode());
        }
    }

    @Override
    public void updateInstanceStatus(String serviceName, String instanceAddress, OperationalStatus status, boolean isOwned) throws ServiceException{
        String uri = toInstanceUri(serviceName, instanceAddress) + "/status";

        String body = null;
        try {
            body = "status=" + URLEncoder.encode(status.toString(), "UTF-8") + "&isOwned=" + isOwned;
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("UTF-8 not supported. ", e);
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("api-version", ServiceDirectory.getAPIVersion());

        HttpResponse result = invoker.invoke(uri, body,
                HttpMethod.PUT, headers);

        if (result.getHttpCode() != HTTP_OK) {
            handleHttpError(result.getHttpCode());
        }
    }


    @Override
    public void updateInstanceUri(String serviceName, String instanceAddress, String uri, boolean isOwned) throws ServiceException{
        String serviceUri = toInstanceUri(serviceName, instanceAddress) + "/uri";
        String body = null;
        try {
            body = "uri=" + URLEncoder.encode(uri, "UTF-8") + "&isOwned=" + isOwned;
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("UTF-8 not supported. ", e);
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("api-version", ServiceDirectory.getAPIVersion());
        HttpResponse result = invoker.invoke(serviceUri, body,
                HttpMethod.PUT, headers);

        if (result.getHttpCode() != HTTP_OK) {
            handleHttpError(result.getHttpCode());
        }
    }

    @Override
    public void updateInstanceMetadata(String serviceName, String instanceAddress, Map<String, String> metadata, boolean isOwned) throws ServiceException{
        String serviceUri = toInstanceUri(serviceName, instanceAddress) + "/metadata";
        String body = null;
        try {
            metadata = handleHiddenMetaData(metadata);
            String meta = new ObjectMapper().writeValueAsString(metadata);

            body = "metadata=" + URLEncoder.encode(meta, "UTF-8") + "&isOwned=" + isOwned;
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
                LOGGER.error("Exception converting map to JSON: ", e);
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("api-version", ServiceDirectory.getAPIVersion());
        HttpResponse result = invoker.invoke(serviceUri, body,
                HttpMethod.PUT, headers);

        if (result.getHttpCode() != HTTP_OK) {
            handleHttpError(result.getHttpCode());
        }
    }



    @Override
    public void unregisterInstance(String serviceName, String instanceAddress, boolean isOwned) throws ServiceException{
        String uri = toInstanceUri(serviceName, instanceAddress) + "/" + isOwned;

        HttpResponse result = invoker.invoke(uri, null,
                HttpMethod.DELETE, addHeader());

        if (result.getHttpCode() != HTTP_OK) {
            handleHttpError(result.getHttpCode());
        }
    }


    @Override
    public Map<String, OperationResult<String>> sendHeartBeat(Map<String, ServiceInstanceHeartbeat> heartbeatMap) throws ServiceException{
        String body = serialize(heartbeatMap);

        HttpResponse result = invoker.invoke("/service/heartbeat", body,
                HttpMethod.PUT, addHeader());

        if (result.getHttpCode() != HTTP_OK) {
            handleHttpError(result.getHttpCode());
        }

        return deserialize(
                result.getRetBody(), new TypeReference<Map<String, OperationResult<String>>>() {
                });

    }


    @Override
    public ModelService lookupService(String serviceName) throws ServiceException{
        ModelService service = lookupService0(serviceName);
        if(favorMyDC){
            List<ModelServiceInstance> instances = service.getServiceInstances();
            if (instances!=null){
                service.setServiceInstances(filterServiceInstancesByDataCenter(instances,myDC));
            }
        }
        return service;
    }

    private List<ModelServiceInstance> filterServiceInstancesByDataCenter(List<ModelServiceInstance> instances, String dCName){
        List<ModelServiceInstance> inMyDC = new ArrayList<>();
        for (ModelServiceInstance ins : instances) {
            if (isInstanceInMyDC(ins)) {
                inMyDC.add(ins);
            }
        }
        return inMyDC;
    }
    private boolean isInstanceInMyDC(ModelServiceInstance instance){
        if (instance==null){
            throw new IllegalArgumentException("instance should not be null");
        }
        if (instance.getMetadata()!=null&&instance.getMetadata().containsKey(SD_API_MY_DC_META_KEY)){
            String dcName = instance.getMetadata().get(SD_API_MY_DC_META_KEY);
            if (myDC.equals(dcName)) {
                return true;
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("%s is be ignored because data-center metadata is [%s] not [%s]", instance, dcName, myDC));
                }
            }
        }
        return false;
    }
    private ModelService lookupService0(String serviceName) throws ServiceException{
        HttpResponse result = invoker.invoke("/service/" + serviceName, null, HttpMethod.GET, addHeader());

        if (result.getHttpCode() != HTTP_OK) {
            handleHttpError(result.getHttpCode());
        }

        return deserialize(result.getRetBody(), ModelService.class);
    }

    @Override
    public List<ModelServiceInstance> getAllInstances() throws ServiceException{
        HttpResponse result = invoker.invoke("/service", null, HttpMethod.GET, addHeader());

        if (result.getHttpCode() != HTTP_OK) {
            handleHttpError(result.getHttpCode());
        }

        return deserialize(result.getRetBody(), new TypeReference<List<ModelServiceInstance>>() {
        });
    }


    @Override
    public ModelMetadataKey getMetadataKey(String keyName) throws ServiceException{
        HttpResponse result = invoker.invoke("/metadatakey/" + keyName, null, HttpMethod.GET, addHeader());

        if (result.getHttpCode() != HTTP_OK) {
            handleHttpError(result.getHttpCode());
        }

        return deserialize(
                result.getRetBody(), ModelMetadataKey.class);
    }


    @Override
    public Map<String, OperationResult<ModelService>> getChangedServices(Map<String, ModelService> services) throws ServiceException{
        String body = serialize(services);

        HttpResponse result = invoker.invoke("/service/changing", body, HttpMethod.POST, addHeader());

        if (result.getHttpCode() != HTTP_OK) {
            handleHttpError(result.getHttpCode());
        }

        return deserialize(
                result.getRetBody(), new TypeReference<Map<String, OperationResult<ModelService>>>() {
                });
    }

    /**
     * Deserialize a JSON String to the target object.
     *
     * @param body
     *         the JSON String.
     * @param clazz
     *         the Object class name deserialized to.
     * @return
     *         the deserialized Object instance.
     * @throws ServiceException
     */
    <T> T deserialize(String body, Class<T> clazz) {
        if (body == null || body.isEmpty()) {
            throw new ServiceException(ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR,
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR.getMessageTemplate(),
                    "the message body is empty");
        }

        try {
            return JsonSerializer.deserialize(body.getBytes(), clazz);
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR, e,
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR.getMessageTemplate(),
                    "unrecognized message, deserialize failed.");
        }
    }

    /**
     * Deserialize a JSON String to a generic object.
     *
     * This method is used when the target object is generic.
     *
     * @param body
     *         the JSON String.
     * @param typeRef
     *         the generic type.
     * @return
     *         the deserialized object instance.
     * @throws ServiceException
     */
    <T> T deserialize(String body, TypeReference<T> typeRef) {
        if (body == null || body.isEmpty()) {
            throw new ServiceException(ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR,
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR
                            .getMessageTemplate(), "the message body is empty");

        }

        try {
            return JsonSerializer.deserialize(body.getBytes(), typeRef);
        } catch (Exception e) {
            throw new ServiceException(ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR, e,
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR.getMessageTemplate(),
                    "unrecognized message, deserialize failed.");
        }
    }

    /**
     * Serialize a object to JSON String.
     *
     * @param o
     *         the target object.
     * @return
     *         the JSON String.
     */
    String serialize(Object o) {
        String body;
        try {
            body = new String(JsonSerializer.serialize(o));
        } catch (IOException e) {
            throw new ServiceException(ErrorCode.HTTP_CLIENT_ERROR,
                    ErrorCode.HTTP_CLIENT_ERROR.getMessageTemplate(),
                    "serialize failed.");
        }
        return body;
    }

    /**
     * Keep it default for unit test.
     * @return
     *         the DirectoryInvoker
     */
    DirectoryHttpInvoker getDirectoryInvoker() {
        return invoker;
    }

    public void setInvoker(DirectoryHttpInvoker invoker) {
        this.invoker = invoker;
    }

    /**
     * It is the HTTP invoker to the ServiceDirectory Server Node.
     *
     * It wraps the complexity of HttpClient and exposes an easy method to invoke RESTful services.
     *
     *
     */
    public static class DirectoryHttpInvoker implements DirectoryInvoker {

        /* The remote ServiceDirectory node address array, in the format of http://<host>:<port> */
        public String directoryAddresses;

        /**
         * Constructor.
         *
         */
        public DirectoryHttpInvoker() {
            String sdFQDN = getServiceDirectoryConfig().getString(SD_API_SD_SERVER_FQDN_PROPERTY, SD_API_SD_SERVER_FQDN_DEFAULT);
            int port = getServiceDirectoryConfig().getInt(SD_API_SD_SERVER_PORT_PROPERTY, SD_API_SD_SERVER_PORT_DEFAULT);
            int https_port = getServiceDirectoryConfig().getInt(SD_API_SD_SERVER_HTTPS_PORT_PROPERTY, SD_API_SD_SERVER_HTTPS_PORT_DEFAULT);
            if (https_port == 0) {
               directoryAddresses = "http://" + sdFQDN + ":" + port;
            } else {
               directoryAddresses = "https://" + sdFQDN + ":" + https_port;
            }
            
        }


        /**
         * Invoke the HTTP RESTful Service.
         *
         * @param uri        The URI of the RESTful service.
         * @param payload    The HTTP body string.
         * @param method     The HTTP method.
         * @param headers    The HTTP headers.
         * @return
         *         the HttpResponse.
         */
        public HttpResponse invoke(String uri, String payload, HttpMethod method, Map<String, String> headers) {
            HttpResponse result = null;
            String url = directoryAddresses + uri;
            try {
                if (method == null || method == HttpMethod.GET) {
                    result = HttpUtils.getJson(url, headers);
                } else if (method == HttpMethod.POST) {
                    result = HttpUtils.postJson(url, payload, headers);
                } else if (method == HttpMethod.PUT) {
                    result = HttpUtils.put(url, payload, headers);
                } else if (method == HttpMethod.DELETE) {
                    result = HttpUtils.deleteJson(url, headers);
                }
            } catch (IOException e) {
                String errMsg = "Send HTTP Request to remote Directory Server '" + url + "' failed";
                throw new ServiceException(ErrorCode.HTTP_CLIENT_ERROR, e, ErrorCode.HTTP_CLIENT_ERROR.getMessageTemplate(),errMsg);
            } catch (ServiceException e) {
                throw new ServiceException(ErrorCode.HTTP_CLIENT_ERROR, e, ErrorCode.HTTP_CLIENT_ERROR.getMessageTemplate(), e.getMessage());
            }
            // HTTP_OK 200, HTTP_MULT_CHOICE 300
            if (result != null) {
                if (LOGGER.isTraceEnabled()){
                    LOGGER.trace(String.format("HTTP Response Tracing : URL=[%s],PAYLOAD=[%s],METHOD=[%s],HTTP_CODE=[%d],RESULT_BODY=[%s]",uri,payload,method,result.getHttpCode(),result.getRetBody()));
                }
                if (result.getHttpCode() < HTTP_OK || result.getHttpCode() >= HTTP_MULT_CHOICE) {
                    String errorBody = result.getRetBody();

                    if (errorBody == null || errorBody.isEmpty()) {
                        throw new ServiceException(ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR,
                                ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR.getMessageTemplate(),
                                "Error Message body is empty.");
                    }
                    ServiceDirectoryError sde;
                    try {
                        sde = JsonSerializer.deserialize(errorBody.getBytes(), ServiceDirectoryError.class);
                    } catch (IOException e) {
                        String errMsg = "Deserialize error body message failed";
                        throw new ServiceException(ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR, e, errMsg);
                    }

                    if (sde != null) {
                        throw new ServiceException(sde.getExceptionCode(), sde.getErrorMessage());
                    }
                }
                return result;
            }else{
                throw new ServiceException(ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR,"null response");
            }
        }
    }

    @Override
    public List<InstanceChange<ModelServiceInstance>> lookupChangesSince(String serviceName, long since) {
        HttpResponse result = invoker.invoke("/v1.2/service/changes/" + serviceName + "/" + since, null, HttpMethod.GET, addHeader());

        if (result.getHttpCode() != HTTP_OK) {
            handleHttpError(result.getHttpCode());
        }

        List<InstanceChange<ModelServiceInstance>> changes = deserialize(result.getRetBody(), new TypeReference<List<InstanceChange<ModelServiceInstance>>>() {
        });

        if (favorMyDC){
            List<InstanceChange<ModelServiceInstance>> changesInMyDC = new ArrayList<>();
            for(InstanceChange<ModelServiceInstance> change : changes){
                ModelServiceInstance checkInstance;
                if (change.from!=null){
                    checkInstance = change.from;
                }else if (change.to!=null){
                    checkInstance = change.to;
                }else{
                    throw new NullPointerException("Should not be both null");
                }
                if (isInstanceInMyDC(checkInstance)){
                   changesInMyDC.add(change);
                }
            }
            return changesInMyDC;
        }
        return changes;
    }
    
    private Map<String, String>addHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("api-version", ServiceDirectory.getAPIVersion());
        return headers;
    }



}

