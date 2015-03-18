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




package com.cisco.oss.foundation.directory.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_CREATED;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.core.type.TypeReference;

import com.cisco.oss.foundation.directory.Configurations;
import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationResult;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstanceHeartbeat;
import com.cisco.oss.foundation.directory.exception.DirectoryServerClientException;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceDirectoryError;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.utils.HttpResponse;
import com.cisco.oss.foundation.directory.utils.HttpUtils.HttpMethod;
import com.cisco.oss.foundation.directory.utils.JsonSerializer;

/**
 * This is the client object to invoke the remote service in ServiceDirectory Server Node.
 *
 *  It implements the HTTP transportation for ServiceDirectoryService,
 *  and hides the HTTPClient details from the upper application layer.
 *
 *
 */
public class DirectoryServiceClient{

    /**
     * The http client read timeout property.
     */
    public static final String SD_API_HTTPCLIENT_READ_TIMEOUT_PROPERTY = "httpclient.read.timeout";

    /**
     * The http client default read timeout value.
     */
    public static final int SD_API_HTTPCLIENT_READ_TIMEOUT_DEFAULT = 5;

    /**
     * The Service Directory server FQDN property name.
     */
    public static final String SD_API_SD_SERVER_FQDN_PROPERTY = "server.fqdn";

    /**
     * The default Service Directory server FQDN name.
     */
    public static final String SD_API_SD_SERVER_FQDN_DEFAULT = "vcsdirsvc";

    /**
     * The Service Directory server port property name.
     */
    public static final String SD_API_SD_SERVER_PORT_PROPERTY = "server.port";

    /**
     * The default Service Directory server port.
     */
    public static final int SD_API_SD_SERVER_PORT_DEFAULT = 2013;

    /**
     * The HTTP invoker to access remote ServiceDirectory node.
     */
    private final DirectoryInvoker invoker;

    private final JsonSerializer serializer;

    /**
     * Constructor.
     */
    public DirectoryServiceClient() {
        serializer = new JsonSerializer();
        String sdFQDN = Configurations.getString(SD_API_SD_SERVER_FQDN_PROPERTY, SD_API_SD_SERVER_FQDN_DEFAULT);
        int port = Configurations.getInt(SD_API_SD_SERVER_PORT_PROPERTY, SD_API_SD_SERVER_PORT_DEFAULT);
        String directoryAddresses = "http://" + sdFQDN + ":" + port;
        this.invoker = new DirectoryInvoker(directoryAddresses, serializer);
    }

    /**
     * Register a ServiceInstance.
     *
     * @param instance
     *         the ProvidedServiceInstance.
     * @throws ServiceException
     */
    public void registerInstance(ProvidedServiceInstance instance){
        String body = serialize(instance);
        HttpResponse result = invoker.invoke("/service/" + instance.getServiceName() + "/" + instance.getProviderId(), body,
                HttpMethod.POST);

        if (result.getHttpCode() != HTTP_CREATED) {
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR,
                    "HTTP Code is not OK, code=" + result.getHttpCode());
            throw new DirectoryServerClientException(sde);
        }
    }

    /**
     * Update a ServiceInstance.
     *
     * @param instance
     *         the ProvidedServiceInstance.
     */
    public void updateInstance(ProvidedServiceInstance instance){
        String body = serialize(instance);
        HttpResponse result = invoker.invoke("/service/" + instance.getServiceName() + "/" + instance.getProviderId(), body,
                HttpMethod.PUT);

        if (result.getHttpCode() != HTTP_CREATED) {
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR,
                    "HTTP Code is not OK, code=" + result.getHttpCode());
            throw new DirectoryServerClientException(sde);
        }
    }

    /**
     * Update the ServiceInstance OperationalStatus by serviceName and instanceId.
     *
     * @param serviceName
     *         the service name.
     * @param instanceId
     *         the instance id.
     * @param status
     *         the ServiceInstance OperationalStatus.
     * @param isOwned
     *         whether the DirectoryAPI owns this ServiceProvider.
     */
    public void updateInstanceStatus(String serviceName, String instanceId, OperationalStatus status, boolean isOwned){
        String uri = "/service/" + serviceName + "/" + instanceId + "/status";

        String body = null;
        try {
            body = "status=" + URLEncoder.encode(status.toString(), "UTF-8") + "&isOwned=" + isOwned;
        } catch (UnsupportedEncodingException e) {
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.SERVICE_INSTANCE_URI_FORMAT_ERROR);
            throw new DirectoryServerClientException(sde);
        }

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        HttpResponse result = invoker.invoke(uri, body,
                HttpMethod.PUT, headers);

        if (result.getHttpCode() != HTTP_OK) {
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR, "HTTP Code is not OK, code=" + result.getHttpCode());
            throw new DirectoryServerClientException(sde);
        }
    }

    /**
     * Update the ServiceInstance URI by serviceName and instanceId.
     *
     * @param serviceName
     *         the service name.
     * @param instanceId
     *         the instance id.
     * @param uri
     *         the ServiceInstance URI.
     * @param isOwned
     *         whether the DirectoryAPI owns this ServiceProvider.
     */
    public void updateInstanceUri(String serviceName, String instanceId, String uri, boolean isOwned){
        String serviceUri = "/service/" + serviceName + "/" + instanceId + "/uri" ;
        String body = null;
        try {
            body = "uri=" + URLEncoder.encode(uri, "UTF-8") + "&isOwned=" + isOwned;
        } catch (UnsupportedEncodingException e) {
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.SERVICE_INSTANCE_URI_FORMAT_ERROR);
            throw new DirectoryServerClientException(sde);
        }

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        HttpResponse result = invoker.invoke(serviceUri, body,
                HttpMethod.PUT, headers);

        if (result.getHttpCode() != HTTP_OK) {
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR, "HTTP Code is not OK, code=" + result.getHttpCode());
            throw new DirectoryServerClientException(sde);
        }
    }

    /**
     * Unregister a ServiceInstance.
     *
     * @param serviceName
     *         service name.
     * @param instanceId
     *         the instance id.
     * @param isOwned
     *         whether the DirectoryAPI owns this ServiceProvider.
     */
    public void unregisterInstance(String serviceName, String instanceId, boolean isOwned){
        String uri = "/service/" + serviceName + "/" + instanceId + "/" + isOwned;
        HttpResponse result = invoker.invoke(uri, null,
                HttpMethod.DELETE);

        if (result.getHttpCode() != HTTP_OK) {
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR, "HTTP Code is not OK, code=" + result.getHttpCode());
            throw new DirectoryServerClientException(sde);
        }
    }

    /**
     * Send ServiceInstance heartbeats.
     *
     * @param heartbeatMap
     *         the ServiceInstances heartbeat Map.
     */
    public Map<String, OperationResult<String>> sendHeartBeat(Map<String, ServiceInstanceHeartbeat> heartbeatMap){
        String body = serialize(heartbeatMap);
        HttpResponse result = invoker.invoke("/service/heartbeat", body,
                HttpMethod.PUT);

        if (result.getHttpCode() != HTTP_OK) {
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR, "HTTP Code is not OK, code=" + result.getHttpCode());
            throw new DirectoryServerClientException(sde);
        }

        Map<String, OperationResult<String>> operateRsult = deserialize(
                result.getRetBody(), new TypeReference<Map<String, OperationResult<String>>>(){});
        return operateRsult;

    }

    /**
     * Lookup a Service by serviceName.
     *
     * @param serviceName
     *         the service name.
     * @return
     *         the ModelService.
     */
    public ModelService lookupService(String serviceName){
        HttpResponse result = invoker.invoke("/service/" + serviceName , null, HttpMethod.GET);

        if (result.getHttpCode() != HTTP_OK) {
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR, "HTTP Code is not OK, code=" + result.getHttpCode());
            throw new DirectoryServerClientException(sde);
        }

        ModelService service = deserialize(result.getRetBody(), ModelService.class);
        return service;
    }

    /**
     * Get all service instances.
     *
     * @return
     *         the ModelServiceInstance list.
     */
    public List<ModelServiceInstance> getAllInstances(){
        HttpResponse result = invoker.invoke("/service" , null, HttpMethod.GET);

        if (result.getHttpCode() != HTTP_OK) {
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR, "HTTP Code is not OK, code=" + result.getHttpCode());
            throw new DirectoryServerClientException(sde);
        }

        List<ModelServiceInstance> allInstances = deserialize(result.getRetBody(), new TypeReference<List<ModelServiceInstance>>(){});
        return allInstances;
    }

    /**
     * Get the MetadataKey by key name.
     *
     * @param keyName
     *         the key name.
     * @return
     *         the ModelMetadataKey.
     */
    public ModelMetadataKey getMetadataKey(String keyName){
        HttpResponse result = invoker.invoke("/metadatakey/" + keyName , null, HttpMethod.GET);

        if (result.getHttpCode() != HTTP_OK) {
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR, "HTTP Code is not OK, code=" + result.getHttpCode());
            throw new DirectoryServerClientException(sde);
        }

        ModelMetadataKey key = deserialize(
                    result.getRetBody(), ModelMetadataKey.class);

        return key;
    }

    /**
     * Get the changed services for Services list.
     *
     * @param services
     *         the Service list.
     * @return
     *         the Service list that has modification.
     * @throws ServiceException
     */
    public Map<String, OperationResult<ModelService>> getServiceChanging(Map<String, ModelService> services){
        String body = serialize(services);
        HttpResponse result = invoker.invoke("/service/changing" , body, HttpMethod.POST);

        if (result.getHttpCode() != HTTP_OK) {
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR, "HTTP Code is not OK, code=" + result.getHttpCode());
            throw new DirectoryServerClientException(sde);
        }

        Map<String, OperationResult<ModelService>> newservices = deserialize(
                    result.getRetBody(), new TypeReference<Map<String, OperationResult<ModelService>>>(){});

        return newservices;
    }

    /**
     * Get the changed MetadataKey.
     *
     * @param keys
     *         the MetadataKey List.
     * @return
     *         the ModelMetadataKey that has been changed.
     */
    public Map<String, OperationResult<ModelMetadataKey>> getMetadataKeyChanging(Map<String, ModelMetadataKey> keys) {
        String body = serialize(keys);
        HttpResponse result = invoker.invoke("/metadatakey/changing" , body, HttpMethod.POST);

        if (result.getHttpCode() != HTTP_OK) {
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR, "HTTP Code is not OK, code=" + result.getHttpCode());
            throw new DirectoryServerClientException(sde);
        }


        Map<String, OperationResult<ModelMetadataKey>> newkeys = deserialize(
                    result.getRetBody(), new TypeReference<Map<String, OperationResult<ModelMetadataKey>>>(){});

        return newkeys;
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
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @SuppressWarnings("unchecked")
    private <T> T deserialize(String body, Class<T> clazz) {
        if(body == null || body.isEmpty()){
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR, "the message body is empty");
            throw new DirectoryServerClientException(sde);
        }

        try {
            return (T) serializer.deserialize(body.getBytes(), clazz);
        } catch (IOException e) {
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR, "unrecognized message, deserialize failed.");
            throw new DirectoryServerClientException(sde, e);
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
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    @SuppressWarnings("unchecked")
    private <T> T deserialize(String body, TypeReference<T> typeRef){
        if(body == null || body.isEmpty()){
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR, "the message body is empty");
            throw new DirectoryServerClientException(sde);
        }

        try {
            return (T) serializer.deserialize(body.getBytes(), typeRef);
        } catch (IOException e) {
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.REMOTE_DIRECTORY_SERVER_ERROR, "unrecognized message, deserialize failed.");
            throw new DirectoryServerClientException(sde, e);
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
    private String serialize(Object o) {
        String body = null;
            try {
                body = new String(serializer.serialize(o));
            } catch (IOException e) {
                ServiceDirectoryError sde = new ServiceDirectoryError(
                        ErrorCode.HTTP_CLIENT_ERROR, "serialize failed.");
                throw new DirectoryServerClientException(sde, e);
            }
        return body;
    }

    /**
     * Keep it default for unit test.
     * @return
     *         the DirectoryInvoker
     */
    DirectoryInvoker getDirectoryInvoker(){
        return invoker;
    }
}
