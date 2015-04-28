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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.NotificationHandler;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.impl.AbstractServiceDirectoryManager;
import com.cisco.oss.foundation.directory.impl.ServiceDirectoryService;
import com.cisco.oss.foundation.directory.impl.ServiceInstanceQueryHelper;
import com.cisco.oss.foundation.directory.lb.LoadBalancerManager;
import com.cisco.oss.foundation.directory.lb.ServiceInstanceLoadBalancer;
import com.cisco.oss.foundation.directory.query.QueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.ContainQueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery.NotContainQueryCriterion;
import com.cisco.oss.foundation.directory.utils.ServiceInstanceUtils;


/**
 * The LookupManager implementation.
 *
 *
 */
public class LookupManagerImpl extends AbstractServiceDirectoryManager implements LookupManager{

    public static final Logger LOGGER = LoggerFactory.getLogger(LookupManagerImpl.class);

    /**
     * The loadbalancer manager for Services.
     */
    private final LoadBalancerManager lbManager;

    /**
     * The LookupService.
     */
    private final DirectoryLookupService lookupService;


    public LookupManagerImpl(DirectoryLookupService lookupService){
        this.lbManager = new LoadBalancerManager();
        this.lookupService = lookupService;
        lookupService.start();
        this.start();
    }

    /**
     * Start the LookupManagerImpl.
     */
    @Override
    public void start(){
        super.start();
        LOGGER.info("Lookup Manager @{} is started", this);
    }

    /**
     * Stop the LookupManagerImpl
     *
     * It is thread safe.
     */
    @Override
    public void stop(){
        super.stop();
        LOGGER.info("Lookup Manager @{} is stopped", this);
    }

    @Override
    public ServiceDirectoryService getService() {
        return getLookupService();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceInstance lookupInstance(String serviceName)
            throws ServiceException {

        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateServiceName(serviceName);
        ServiceInstanceLoadBalancer lb = lbManager.getDefaultLoadBalancer();
        return lb.vote(lookupInstances(serviceName));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceInstance> lookupInstances(String serviceName)
            throws ServiceException {
        
        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateServiceName(serviceName);

        List<ModelServiceInstance> modelSvc = getLookupService().getUPModelInstances(serviceName);
        List<ServiceInstance> instances = new ArrayList<>();
        for (ModelServiceInstance modelInstance : modelSvc) {
            instances.add(ServiceInstanceUtils.toServiceInstance(modelInstance));
        }
        return instances;
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
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "ServiceInstanceQuery");
        }
        ServiceInstanceLoadBalancer lb = lbManager.getDefaultLoadBalancer();
        return lb.vote(queryInstancesByName(serviceName,query));
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
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "ServiceInstanceQuery");
        }
        List<ModelServiceInstance> modelSvc = getLookupService().getUPModelInstances(serviceName);
        List<ModelServiceInstance> filteredInstances = ServiceInstanceQueryHelper.filter(query, modelSvc);
        List<ServiceInstance> instances = new ArrayList<>();
        for (ModelServiceInstance model : filteredInstances) {
            instances.add(ServiceInstanceUtils.toServiceInstance(model));
        }
        return instances;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceInstance queryInstanceByMetadataKey(ServiceInstanceQuery query)
            throws ServiceException {
        
        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        validateServiceInstanceMetadataQuery(query);

        ServiceInstanceLoadBalancer lb = lbManager.getDefaultLoadBalancer();
        return lb.vote(queryInstancesByMetadataKey(query));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceInstance> queryInstancesByMetadataKey(ServiceInstanceQuery query)
            throws ServiceException {
        
        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        validateServiceInstanceMetadataQuery(query);

        List<ServiceInstance> instances;
        String keyName = null;
        if (query.getCriteria().size() > 0) {
            keyName = query.getCriteria().get(0).getMetadataKey();
        }
        if (keyName != null && !keyName.isEmpty()) {
            List<ModelServiceInstance> modelInstances = getLookupService()
                    .getUPModelInstancesByMetadataKey(keyName);
            List<ModelServiceInstance> filteredInstances = ServiceInstanceQueryHelper
                    .filter(query, modelInstances);

            instances = new ArrayList<>();
            for (ModelServiceInstance model : filteredInstances) {
                instances.add(ServiceInstanceUtils.toServiceInstance(model));
            }
            return instances;
        }
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public ServiceInstance getInstance(String serviceName, String instanceId)
            throws ServiceException {

        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        ServiceInstanceUtils.validateServiceName(serviceName);
        ServiceInstanceUtils.validateServiceInstanceID(instanceId);

        ModelServiceInstance instance = getLookupService().getModelServiceInstance(serviceName, instanceId);

        if (instance!=null) {
            return ServiceInstanceUtils.toServiceInstance(instance);
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

        List<ModelServiceInstance> modelSvc = getLookupService().getModelInstances(serviceName);
        List<ServiceInstance> instances = new ArrayList<>();
        for (ModelServiceInstance modelInstance : modelSvc) {
            instances.add(ServiceInstanceUtils.toServiceInstance(modelInstance));
        }
        return instances;
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
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                     ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "ServiceInstanceQuery");
        }

        List<ModelServiceInstance> modelSvc = getLookupService().getModelInstances(serviceName);
        List<ModelServiceInstance> filteredInstances = ServiceInstanceQueryHelper.filter(query, modelSvc);
        List<ServiceInstance> instances = new ArrayList<>();
        for (ModelServiceInstance model : filteredInstances) {
            instances.add(ServiceInstanceUtils.toServiceInstance(model));
        }
        return instances;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceInstance> getAllInstancesByMetadataKey(ServiceInstanceQuery query)
            throws ServiceException {
        
        ServiceInstanceUtils.validateManagerIsStarted(isStarted);
        validateServiceInstanceMetadataQuery(query);

        String keyName = null;
        if (query.getCriteria().size() > 0) {
            keyName = query.getCriteria().get(0).getMetadataKey();
        }
        if (keyName != null && !keyName.isEmpty()) {
            List<ModelServiceInstance> modelInstances = getLookupService()
                    .getModelInstancesByMetadataKey(keyName);
            List<ModelServiceInstance> filteredInstances = ServiceInstanceQueryHelper
                    .filter(query, modelInstances);
            List<ServiceInstance> instances = new ArrayList<>();
            for (ModelServiceInstance model : filteredInstances) {
                instances.add(ServiceInstanceUtils.toServiceInstance(model));
            }
            return instances;
        }
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceInstance> getAllInstances() throws ServiceException {
        
        ServiceInstanceUtils.validateManagerIsStarted(isStarted);

        List<ServiceInstance> instances = new ArrayList<ServiceInstance>();
        List<ModelServiceInstance> allInstances = getLookupService().getAllInstances();
        for (ModelServiceInstance serviceInstance : allInstances) {
            instances.add(ServiceInstanceUtils.toServiceInstance(serviceInstance));
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
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "NotificationHandler");
        }
        ModelService service = getLookupService().getModelService(serviceName);
        if (service == null) {
            throw new ServiceException(ErrorCode.SERVICE_NOT_EXIST,ErrorCode.SERVICE_NOT_EXIST.getMessageTemplate(),serviceName);
        }
        getLookupService().addNotificationHandler(serviceName, handler);

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
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "NotificationHandler");
        }
        
        getLookupService().removeNotificationHandler(serviceName, handler);
    }

    /**
     * Get the DirectoryLookupService to do the lookup.
     *
     * It is thread safe.
     *
     * @return
     *         the LookupService.
     */
    public DirectoryLookupService getLookupService(){

        return lookupService;
    }

    /**
     * Validate the metadata query for the queryInstanceByMetadataKey, queryInstancesByMetadataKey 
     * and getAllInstancesByMetadataKey methods. For those methods, the ContainQueryCriterion and 
     * NotContainQueryCriterion are not supported.
     *
     * @param query
     *         the ServiceInstanceQuery to be validated.
     * @throws ServiceException
     *         when the ServiceInstanceQuery has ContainQueryCriterion or NotContainQueryCriterion.
     */
    private void validateServiceInstanceMetadataQuery(ServiceInstanceQuery query) throws ServiceException{

        if (query == null) {
            throw new ServiceException(ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR,
                    ErrorCode.SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR.getMessageTemplate(),
                    "service instance query");
        }
        
        for(QueryCriterion criterion : query.getCriteria()){
            if(criterion instanceof ContainQueryCriterion || criterion instanceof NotContainQueryCriterion){
                throw new ServiceException(ErrorCode.QUERY_CRITERION_ILLEGAL_IN_QUERY);
            }
        }
    }

}
