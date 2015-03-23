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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.NotificationHandler;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceDirectoryError;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.exception.ServiceRuntimeException;
import com.cisco.oss.foundation.directory.lb.LoadBalancerManager;
import com.cisco.oss.foundation.directory.lb.ServiceInstanceLoadBalancer;
import com.cisco.oss.foundation.directory.lifecycle.Stoppable;
import com.cisco.oss.foundation.directory.query.QueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.ContainQueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.NotContainQueryCriterion;
import com.cisco.oss.foundation.directory.utils.ServiceInstanceUtils;
import static com.cisco.oss.foundation.directory.ServiceDirectory.getServiceDirectoryConfig;

/**
 * The default LookupManager implementation to access the remote Directory Server.
 *
 *
 */
public class LookupManagerImpl implements LookupManager, Stoppable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LookupManagerImpl.class);

    /**
     * The LookupManager cache enabled property.
     */
    public static final String SD_API_CACHE_ENABLED_PROPERTY = "com.cisco.oss.foundation.directory.cache.enabled";

    /**
     * The default cache enabled property value.
     */
    public static final boolean SD_API_CACHE_ENABLED_DEFAULT = true;

    /**
     * The remote ServiceDirectory node client.
     */
    private final DirectoryServiceClientManager directoryServiceClientManager ;

    /**
     * The loadbalancer map for Services.
     */
    private final LoadBalancerManager lbManager;

    /**
     * The LookupService.
     */
    private volatile DirectoryLookupService lookupService;

    /**
     * Mark component started or not
     */
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * Constructor.
     */
    public LookupManagerImpl(DirectoryServiceClientManager directoryServiceClientManager){
        this.directoryServiceClientManager = directoryServiceClientManager;
        this.lbManager = new LoadBalancerManager();
    }

    /**
     * Start the LookupManagerImpl.
     *
     * It is idempotent, and can be invoked multiple times.
     */
    @Override
    public void start(){
        isStarted.set(true);
    }

    /**
     * Stop the LookupManagerImpl
     *
     * It is thread safe.
     */
    @Override
    public void stop(){
        if (isStarted.compareAndSet(true, false)) {
           ((Stoppable) getLookupService()).stop();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceInstance lookupInstance(String serviceName)
            throws ServiceException {

        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateServiceName(serviceName);
        
        try{
            ServiceInstanceLoadBalancer lb = lbManager.getDefaultLoadBalancer();
            return lb.vote(lookupInstances(serviceName));
        } catch(ServiceRuntimeException e){
            throw new ServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceInstance> lookupInstances(String serviceName)
            throws ServiceException {
        
        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateServiceName(serviceName);
        
        try{
            List<ModelServiceInstance> modelSvc = getLookupService().getUPModelInstances(serviceName);
            if(modelSvc == null || modelSvc.isEmpty()){
                return Collections.<ServiceInstance>emptyList();
            }else{
                List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
                for(ModelServiceInstance modelInstance : modelSvc){
                    instances.add(ServiceInstanceUtils.toServiceInstance(modelInstance));
                }
                return instances;
            }
        } catch(ServiceRuntimeException e){
            throw new ServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceInstance queryInstanceByName(String serviceName, ServiceInstanceQuery query)
            throws ServiceException {
        
        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateServiceName(serviceName);
        if (query == null) {
            throw new ServiceException(new ServiceDirectoryError(
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    "ServiceInstanceQuery"));
        }
        
        try{
            ServiceInstanceLoadBalancer lb = lbManager.getDefaultLoadBalancer();
            return lb.vote(queryInstancesByName(serviceName,query));
        } catch(ServiceRuntimeException e){
            throw new ServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceInstance> queryInstancesByName(String serviceName, ServiceInstanceQuery query)
            throws ServiceException {
        
        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateServiceName(serviceName);
        if (query == null) {
            throw new ServiceException(new ServiceDirectoryError(
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    "ServiceInstanceQuery"));
        }

        try{
            List<ModelServiceInstance> modelSvc = getLookupService().getUPModelInstances(serviceName);
            if(modelSvc != null && ! modelSvc.isEmpty()){
                List<ModelServiceInstance> filteredInstances = ServiceInstanceQueryHelper.filter(query, modelSvc);
                List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
                for (ModelServiceInstance model : filteredInstances) {
                    instances.add(ServiceInstanceUtils.toServiceInstance(model));
                }
                return instances;
            }
            return Collections.<ServiceInstance>emptyList();
        } catch(ServiceRuntimeException e){
            throw new ServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceInstance queryInstanceByMetadataKey(ServiceInstanceQuery query)
            throws ServiceException {
        
        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        validateServiceInstanceMetadataQuery(query);

        try{
            ServiceInstanceLoadBalancer lb = lbManager.getDefaultLoadBalancer();
            return lb.vote(queryInstancesByMetadataKey(query));
        } catch(ServiceRuntimeException e){
            throw new ServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceInstance> queryInstancesByMetadataKey(ServiceInstanceQuery query)
            throws ServiceException {
        
        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        validateServiceInstanceMetadataQuery(query);
        
        try{
            List<ServiceInstance> instances = null;
            String keyName = null;
            if (query.getCriteria().size() > 0) {
                keyName = query.getCriteria().get(0).getMetadataKey();
            }
            if (keyName != null && !keyName.isEmpty()) {
                List<ModelServiceInstance> modelInstances = getLookupService()
                        .getUPModelInstancesByMetadataKey(keyName);
                List<ModelServiceInstance> filteredInstances = ServiceInstanceQueryHelper
                        .filter(query, modelInstances);

                instances = new ArrayList<ServiceInstance>();
                for (ModelServiceInstance model : filteredInstances) {
                    instances.add(ServiceInstanceUtils.toServiceInstance(model));
                }
                return instances;
            }
            return Collections.<ServiceInstance>emptyList();
        } catch(ServiceRuntimeException e){
            throw new ServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceInstance getInstance(String serviceName, String instanceId)
            throws ServiceException {

        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateServiceName(serviceName);
        ServiceInstanceUtils.validateServiceInstanceID(instanceId);

        try{
            ModelServiceInstance instance = getLookupService().getModelServiceInstance(serviceName, instanceId);
            if(instance != null){
                return ServiceInstanceUtils.toServiceInstance(instance);
            }
        } catch(ServiceRuntimeException e){
            ServiceDirectoryError reason = e.getServiceDirectoryError();
            if (reason != null && reason.getExceptionCode() != null
                    && reason.getExceptionCode() == ErrorCode.SERVICE_NOT_EXIST) {
                LOGGER.info(String.format(
                        "The service name %s not found at Service Directory",
                        serviceName));
            } else {
                throw new ServiceException(e);
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceInstance> getAllInstances(String serviceName)
            throws ServiceException {

        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateServiceName(serviceName);

        try{
            List<ModelServiceInstance> modelSvc = getLookupService().getModelInstances(serviceName);
            if(modelSvc == null || modelSvc.isEmpty()){
                return Collections.<ServiceInstance>emptyList();
            }else{
                List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
                for(ModelServiceInstance modelInstance : modelSvc){
                    instances.add(ServiceInstanceUtils.toServiceInstance(modelInstance));
                }
                return instances;
            }
        } catch(ServiceRuntimeException e){
            throw new ServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceInstance> getAllInstances(String serviceName,
            ServiceInstanceQuery query) throws ServiceException {

        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateServiceName(serviceName);
        if (query == null) {
            throw new ServiceException(new ServiceDirectoryError(
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    "ServiceInstanceQuery"));
        }

        try{
            List<ModelServiceInstance> modelSvc = getLookupService().getModelInstances(serviceName);
            if (modelSvc != null && !modelSvc.isEmpty()) {
                List<ModelServiceInstance> filteredInstances = ServiceInstanceQueryHelper
                        .filter(query, modelSvc);
                List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
                for (ModelServiceInstance model : filteredInstances) {
                    instances.add(ServiceInstanceUtils.toServiceInstance(model));
                }
                return instances;
            }
            return Collections.<ServiceInstance>emptyList();
        } catch(ServiceRuntimeException e){
            throw new ServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceInstance> getAllInstancesByMetadataKey(ServiceInstanceQuery query)
            throws ServiceException {
        
        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        validateServiceInstanceMetadataQuery(query);
        
        try{
            List<ServiceInstance> instances = null;
            String keyName = null;
            if (query.getCriteria().size() > 0) {
                keyName = query.getCriteria().get(0).getMetadataKey();
            }
            if (keyName != null && !keyName.isEmpty()) {
                List<ModelServiceInstance> modelInstances = getLookupService()
                        .getModelInstancesByMetadataKey(keyName);
                List<ModelServiceInstance> filteredInstances = ServiceInstanceQueryHelper
                        .filter(query, modelInstances);
                instances = new ArrayList<ServiceInstance>();
                for (ModelServiceInstance model : filteredInstances) {
                    instances.add(ServiceInstanceUtils.toServiceInstance(model));
                }
                return instances;
            }
            return Collections.<ServiceInstance>emptyList();
        } catch(ServiceRuntimeException e){
            throw new ServiceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceInstance> getAllInstances() throws ServiceException {
        
        ServiceInstanceUtils.validateManagerIsStarted(isStarted);

        List<ServiceInstance> instances = null;
        try {
            List<ModelServiceInstance> allInstances = getLookupService()
                    .getAllInstances();
            for (ModelServiceInstance serviceInstance : allInstances) {
                instances = new ArrayList<ServiceInstance>();
                instances.add(ServiceInstanceUtils
                        .toServiceInstance(serviceInstance));
            }
        } catch (ServiceRuntimeException e) {
            throw new ServiceException(e);
        }

        if (instances == null) {
            return Collections.<ServiceInstance>emptyList();
        }

        return instances;
    }


    /**
     * Add a NotificationHandler to the Service.
     *
     * This method can check the duplicated NotificationHandler for the serviceName, if the NotificationHandler
     * already exists for the serviceName, do nothing.
     *
     * Throws IllegalArgumentException if serviceName or handler is null.
     *
     * @param serviceName
     *         the service name.
     * @param handler
     *         the NotificationHandler for the service.
     */
    @Override
    public void addNotificationHandler(String serviceName, NotificationHandler handler) throws ServiceException {

        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateServiceName(serviceName);
        if (handler == null) {
            throw new ServiceException(new ServiceDirectoryError(
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    "NotificationHandler"));
        }

        try{
            ModelService service = getLookupService().getModelService(serviceName);
            if(service == null){
                ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_NOT_EXIST);
                throw new ServiceException(error);
            }
            getLookupService().addNotificationHandler(serviceName, handler);
        } catch(ServiceRuntimeException e){
            throw new ServiceException(e);
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
    @Override
    public void removeNotificationHandler(String serviceName, NotificationHandler handler) throws ServiceException {

        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateServiceName(serviceName);
        if (handler == null) {
            throw new ServiceException(new ServiceDirectoryError(
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    "NotificationHandler"));
        }

        try{
            getLookupService().removeNotificationHandler(serviceName, handler);;
        } catch(ServiceRuntimeException e){
            throw new ServiceException(e);
        }
    }

    /**
     * Get the DirectoryLookupService to do the lookup.
     *
     * It is thread safe.
     *
     * @return
     *         the LookupService.
     */
    private DirectoryLookupService getLookupService(){
        if(lookupService == null){
            synchronized(this){
                if(lookupService == null){
                    boolean cacheEnabled = getServiceDirectoryConfig().getBoolean(SD_API_CACHE_ENABLED_PROPERTY,
                            SD_API_CACHE_ENABLED_DEFAULT);
                    if(cacheEnabled){
                        CachedDirectoryLookupService service = new CachedDirectoryLookupService(directoryServiceClientManager);
                        service.start();
                        lookupService = service;
                        LOGGER.info("Created the CachedDirectoryLookupService in LookupManager");
                    } else {
                        lookupService = new DirectoryLookupService(directoryServiceClientManager);
                        LOGGER.info("Created the DirectoryLookupService in LookupManager");
                    }
                }
            }
        }
        return lookupService;
    }

    /**
     * Validate the ServiceInstanceQuery for the queryInstanceByMetadataKey, queryInstancesByMetadataKey
     * and getAllInstancesByMetadataKey method.
     *
     * For those methods, the ContainQueryCriterion and NotContainQueryCriterion are not supported.
     *
     * @param query
     *         the ServiceInstanceQuery to be validated.
     * @throws ServiceException
     *         when the ServiceInstanceQuery has ContainQueryCriterion or NotContainQueryCriterion.
     */
    private void validateServiceInstanceMetadataQuery(ServiceInstanceQuery query) throws ServiceException{

        if (query == null) {
            throw new ServiceException(new ServiceDirectoryError(
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    "service instance query"));
        }
        
        for(QueryCriterion criterion : query.getCriteria()){
            if(criterion instanceof ContainQueryCriterion || criterion instanceof NotContainQueryCriterion){
                ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.QUERY_CRITERION_ILLEGAL_IN_QUERY);
                throw new ServiceException(error);
            }
        }
    }


}
