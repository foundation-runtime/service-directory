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




package com.cisco.oss.foundation.directory.lookup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cisco.oss.foundation.directory.NotificationHandler;
import com.cisco.oss.foundation.directory.client.DirectoryServiceClient;
import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.utils.ServiceInstanceUtils;

/**
 * It is the Directory LookupService to perform the lookup functionality.
 *
 * It looks up ServiceInstance from Directory Server.
 *
 *
 */
public class DirectoryLookupService {

    /**
     * The DirectoryServiceClientManager to get the DirectoryServiceClient.
     */
    private final DirectoryServiceClient directoryServiceClient;

    /**
     * The Service NotificationHandler Map.
     */
    private Map<String, List<NotificationHandler>> notificationHandlers  = new HashMap<String, List<NotificationHandler>>();

    /**
     * Constructor.
     *
     * @param directoryServiceClient
     *         the DirectoryServiceClient.
     */
    public DirectoryLookupService(DirectoryServiceClient directoryServiceClient){
        this.directoryServiceClient = directoryServiceClient;
    }

    /**
     * Get the ModelService by service name.
     *
     * @param serviceName
     *         the Service name.
     * @return
     *         the ModelService.
     */
    protected ModelService getModelService(String serviceName){
        return getDirectoryServiceClient().lookupService(serviceName);
    }

    /**
     * Get ModelMetadataKey, which is an object holding a list of service instances that 
     * contain the key name in the service metadata.
     *
     * @param keyName
     *         the metadata key name.
     * @return
     *         the ModelMetadataKey.
     */
    protected ModelMetadataKey getModelMetadataKey(String keyName){
        return getDirectoryServiceClient().getMetadataKey(keyName);
    }

    /**
     * Get the ModelServiceInstance by serviceName and instanceId.
     *
     * @param serviceName
     *         the service name.
     * @param instanceId
     *         the instanceId.
     * @return
     *         the ModelServiceInstance.
     */
    public ModelServiceInstance getModelServiceInstance(String serviceName, String instanceId){
        ModelService service = getModelService(serviceName);
        if(service != null && service.getServiceInstances() != null ){
            for(ModelServiceInstance instance : service.getServiceInstances()){
                if(instance.getInstanceId().equals(instanceId)){
                    return instance;
                }
            }
        }
        return null;
    }

    /**
     * Get the ModelServiceInstance list that contains the metadata key.
     *
     * @param keyName
     *         the metadata key name.
     * @return
     *         the UP ModelServiceInstances that has the metadata key.
     */
    public List<ModelServiceInstance> getModelInstancesByMetadataKey(String keyName){
        ModelMetadataKey key = getModelMetadataKey(keyName);
        if(key == null || key.getServiceInstances().isEmpty()){
            return Collections.<ModelServiceInstance>emptyList();
        }else{
            return new ArrayList<ModelServiceInstance>(key.getServiceInstances());
        }
    }

    /**
     * Get the UP ModelServiceInstance list that contains the metadata key.
     *
     *
     * @param keyName
     *         the metadata key name.
     * @return
     *         the ModelServiceInstances that have the metadata key.
     */
    public List<ModelServiceInstance> getUPModelInstancesByMetadataKey(String keyName){
        List<ModelServiceInstance> list = new ArrayList<ModelServiceInstance>();
        for (ModelServiceInstance instance : getModelInstancesByMetadataKey(keyName)){
            if(instance.getStatus().equals(OperationalStatus.UP)){
                list.add(instance);
            }
        }
        return list;
    }

    /**
     * Get the ModelServiceInstance list of the Service.
     *
     * @param serviceName
     *         the service name.
     * @return
     *         the ModelServiceInstance list of the Service.
     */
    public List<ModelServiceInstance> getModelInstances(String serviceName){
        ModelService service = getModelService(serviceName);
        if(service == null || service.getServiceInstances().isEmpty()){
            return Collections.<ModelServiceInstance>emptyList();
        }else{
            return new ArrayList<ModelServiceInstance>(service.getServiceInstances());
        }
    }

    /**
     * Get All ModelServiceInstance on the Directory Server.
     *
     * @return
     *         the ModelServiceInstance List.
     */
    public List<ModelServiceInstance> getAllInstances(){
        return getDirectoryServiceClient().getAllInstances();
    }

    /**
     * Get the UP ModelServiceInstance list of the Service.
     *
     * @param serviceName
     *         the service name.
     * @return
     *         the ModelServiceInstance list of the Service.
     */
    public List<ModelServiceInstance> getUPModelInstances(String serviceName){
        List<ModelServiceInstance> list = new ArrayList<ModelServiceInstance>();
        for(ModelServiceInstance instance : getModelInstances(serviceName)){
            if(instance.getStatus().equals(OperationalStatus.UP)) {
                list.add(instance);
            }
        }
        return list;
    }

    /**
     * Add a NotificationHandler to the Service.
     *
     * This method checks the duplicated NotificationHandler for the serviceName, if the NotificationHandler
     * already exists for the serviceName, do nothing.
     *
     * @param serviceName
     *         the service name.
     * @param handler
     *         the NotificationHandler for the service.
     */
    public void addNotificationHandler(String serviceName, NotificationHandler handler){

        ServiceInstanceUtils.validateServiceName(serviceName);
        if (handler == null) {
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "NotificationHandler");
        }
  
        synchronized(notificationHandlers){
            if(! notificationHandlers.containsKey(serviceName)){
                notificationHandlers.put(serviceName, new ArrayList<NotificationHandler>());
            }
            notificationHandlers.get(serviceName).add(handler);
        }
    }

    /**
     * Remove the NotificationHandler from the Service.
     *
     * @param serviceName
     *         the service name.
     * @param handler
     *         the NotificationHandler for the service.
     */
    public void removeNotificationHandler(String serviceName, NotificationHandler handler){

        ServiceInstanceUtils.validateServiceName(serviceName);
        if (handler == null) {
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "NotificationHandler");
        }
  
        synchronized(notificationHandlers){
            if(notificationHandlers.containsKey(serviceName)){
                List<NotificationHandler> list = notificationHandlers.get(serviceName);
                if(list.contains(handler)){
                    list.remove(handler);
                }
                if(list.isEmpty()){
                    notificationHandlers.remove(serviceName);
                }
            }
        }
    }

    /**
     * Get the DirectoryServiceClient.
     *
     * @return
     *         the DirectoryServiceClient
     */
    protected DirectoryServiceClient getDirectoryServiceClient(){
        return this.directoryServiceClient;
    }

    /**
     * Invoke the serviceInstanceUnavailable of the NotificationHandler.
     *
     * @param instance
     *         the ServiceInstance.
     */
    protected void onServiceInstanceUnavailable(ServiceInstance instance){
        if(instance == null){
            return ;
        }
        String serviceName = instance.getServiceName();
        List<NotificationHandler> handlerList = new ArrayList<NotificationHandler>();
        synchronized (notificationHandlers) {
            if (notificationHandlers.containsKey(serviceName)) {
                handlerList.addAll(notificationHandlers.get(serviceName));
            }
        }
        for(NotificationHandler h : handlerList) {
            h.serviceInstanceUnavailable(instance);
        }
    }

    /**
     * Invoke the serviceInstanceChange of the NotificationHandler.
     *
     * @param instance
     *         the ServiceInstance.
     */
    protected void onServiceInstanceChanged(ServiceInstance instance){
        String serviceName = instance.getServiceName();
        List<NotificationHandler> handlerList = new ArrayList<NotificationHandler>();
        synchronized (notificationHandlers) {
            if (notificationHandlers.containsKey(serviceName)) {
                handlerList.addAll(notificationHandlers.get(serviceName));
            }
        }
        for (NotificationHandler h : handlerList) {
            h.serviceInstanceChange(instance);
        }
    }

    /**
     * Invoke the serviceInstanceAvailable of the NotificationHandler.
     *
     * @param instance
     *         the ServiceInstance.
     */
    protected void onServiceInstanceAvailable(ServiceInstance instance){
        String serviceName = instance.getServiceName();
        List<NotificationHandler> handlerList = new ArrayList<NotificationHandler>();
        synchronized (notificationHandlers) {
            if (notificationHandlers.containsKey(serviceName)) {
                handlerList.addAll(notificationHandlers.get(serviceName));
            }
        }
        for (NotificationHandler h : handlerList) {
            h.serviceInstanceAvailable(instance);
        }
    }
}
