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




package com.cisco.oss.foundation.directory.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.NotificationHandler;
import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceInstanceHealth;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;
import com.cisco.oss.foundation.directory.exception.DirectoryServerClientException;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceDirectoryError;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.exception.ServiceRuntimeException;
import com.cisco.oss.foundation.directory.impl.ServiceInstanceQueryHelper;
import com.cisco.oss.foundation.directory.lifecycle.Stoppable;
import com.cisco.oss.foundation.directory.query.QueryCriterion;
import com.cisco.oss.foundation.directory.query.ServiceInstanceQuery;
import com.cisco.oss.foundation.directory.utils.ServiceInstanceUtils;

/**
 * The default Directory API RegistrationManager, LookupManager implementation for application testing purpose.
 *
 * It implements the RegistrationManager and LookupManager interfaces to operate the ServiceInstance
 * at client side and does not invoke a remote ServiceDirectory server node, all ServiceInstances
 * are stored in the local memory.
 *
 *
 *
 */
public class DefaultTestServiceDirectoryManager implements
        RegistrationManager, LookupManager, Stoppable {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(DefaultTestServiceDirectoryManager.class);

    /**
     * The position index for query instance by service name.
     */
    private Map<String, AtomicInteger> queryInstanceIdx;

    /**
     * The internal Cache for ProvidedService.
     */
    private ConcurrentHashMap<String, ProvidedService> cache;

    /**
     * The internal Cache for MetadataKey.
     */
    private ConcurrentHashMap<String, List<ProvidedServiceInstance>> metadataKeyCache;

    /**
     * Mark whether component is started or not.
     */
    protected AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * The Service NotificationHandler Map.
     */
    private Map<String, List<NotificationHandler>> notificationHandlers  = new HashMap<String, List<NotificationHandler>>();


    /**
     * Constructor
     */
    public DefaultTestServiceDirectoryManager(){
        cache = new ConcurrentHashMap<String, ProvidedService>();
        metadataKeyCache = new ConcurrentHashMap<String, List<ProvidedServiceInstance>>();
        this.queryInstanceIdx = new HashMap<String, AtomicInteger>();
    }

    /**
     * Start the DefaultTestServiceDirectoryManager.
     */
    @Override
    public void start() {
        isStarted.compareAndSet(false,true);
    }

    /**
     * Stop the DefaultTestServiceDirectoryManager.
     */
    @Override
    public void stop() {
        if(isStarted.compareAndSet(true,false)){
            cache.clear();
            metadataKeyCache.clear();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param serviceName
     *         the service name.
     * @return
     *         the ServiceInstance.
     * @throws ServiceException
     */
    @Override
    public ServiceInstance lookupInstance(String serviceName)
            throws ServiceException {
        List<ServiceInstance> list = this.lookupInstances(serviceName);
        if(list == null || list.size() == 0){
            return null;
        }
        int index = getNextQueryInstanceIndex(serviceName, list.size());
        return list.get(index);
    }

    /**
     * {@inheritDoc}
     *
     * @param serviceName
     *         the service name.
     * @return
     *         the ServiceInstance list.
     * @throws ServiceException
     */
    @Override
    public List<ServiceInstance> lookupInstances(String serviceName)
            throws ServiceException {

        ProvidedService service = null;

        if(cache.containsKey(serviceName)){
            service = cache.get(serviceName);
        }

        if(service == null || service.getServiceInstances().size() == 0){
            return Collections.emptyList();
        }else{
            List<ServiceInstance> list = new ArrayList<ServiceInstance>();
            for(ProvidedServiceInstance model : service.getServiceInstances()){
                if(model.getStatus() == OperationalStatus.UP){
                    list.add(new ServiceInstance(serviceName, model.getProviderId(), model.getUri(),
                            model.isMonitorEnabled(), model.getStatus(), model.getAddress(), model.getPort(), model.getMetadata()));
                }
            }
            return list;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param serviceName
     *         the service name.
     * @param query
     *         the ServiceInstanceQuery.
     * @return
     *         the ServiceInstance.
     * @throws ServiceException
     */
    @Override
    public ServiceInstance queryInstanceByName(String serviceName, ServiceInstanceQuery query)
            throws ServiceException {
        List<ServiceInstance> list = this.queryInstancesByName(serviceName, query);
        if(list == null || list.size() == 0){
            return null;
        }
        int index = getNextQueryInstanceIndex(serviceName, list.size());
        return list.get(index);
    }

    /**
     * {@inheritDoc}
     *
     * @param serviceName
     *         the service name.
     * @param query
     *         the ServiceInstanceQuery.
     * @return
     *         the ServiceInstance list match the ServiceInstanceQuery.
     * @throws ServiceException
     */
    @Override
    public List<ServiceInstance> queryInstancesByName(String serviceName, ServiceInstanceQuery query)
            throws ServiceException {
        List<ServiceInstance> instances = this.lookupInstances(serviceName);
        return ServiceInstanceQueryHelper.filterServiceInstance(query, instances);
    }

    /**
     * {@inheritDoc}
     *
     * @param serviceInstance
     *         the ProvidedServiceInstance.
     * @throws ServiceException
     */
    @Override
    public void registerService(ProvidedServiceInstance serviceInstance)
            throws ServiceException {

        ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);

        ProvidedServiceInstance cachedInstance = this.getProvidedServiceInstance(serviceInstance.getServiceName(), serviceInstance.getProviderId());
        if(cachedInstance != null){
            ServiceDirectoryError error = new ServiceDirectoryError(ErrorCode.SERVICE_INSTANCE_ALREADY_EXIST, serviceInstance.getServiceName());
            throw new ServiceException(error);
        }
        String serviceName = serviceInstance.getServiceName();

        ProvidedService service = this.createServiceIfNotExists(serviceName);

        ProvidedServiceInstance cacheServiceInstance = deepCloneProvidedServiceInstance(serviceInstance);
        service.getServiceInstances().add(cacheServiceInstance);
        addMetadataKeyMap(serviceInstance);

        onServiceInstanceAvailable(new ServiceInstance(cacheServiceInstance.getServiceName(), cacheServiceInstance.getProviderId(), cacheServiceInstance.getUri(),
                        cacheServiceInstance.isMonitorEnabled(), cacheServiceInstance.getStatus(), cacheServiceInstance.getAddress(), cacheServiceInstance.getPort(), cacheServiceInstance.getMetadata()));
        LOGGER.info("Registered Service, name={}.", serviceInstance.getServiceName());
    }

    /**
     * {@inheritDoc}
     *
     * @param serviceInstance
     *         the ProvidedServiceInstance.
     * @param registryHealth
     *         the ServiceInstanceHealth callback of the ServiceInstance.
     * @throws ServiceException
     */
    @Override
    public void registerService(ProvidedServiceInstance serviceInstance, ServiceInstanceHealth registryHealth)
            throws ServiceException {
        registerService(serviceInstance);
    }

    /**
     * {@inheritDoc}
     *
     * @param serviceName
     *         the service name.
     * @param providerId
     *         the providerId.
     * @param status
     *         the OperatonalStatus of the ProvidedServiceInstance.
     * @throws ServiceException
     */
    @Override
    public void updateServiceOperationalStatus(String serviceName,
            String providerId, OperationalStatus status)
            throws ServiceException {

        ServiceInstanceUtils.validateServiceName(serviceName);
        ServiceInstanceUtils.validateServiceInstanceID(providerId);

        ProvidedServiceInstance model = getProvidedServiceInstance(serviceName, providerId);
        if(model == null){
            LOGGER.error("Update Service Instance OperationalStatus failed - can not find the ServiceInstance.");
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.SERVICE_INSTANCE_NOT_EXIST,
                    serviceName);
            throw new DirectoryServerClientException(sde);
        }
        model.setStatus(status);

        onServiceInstanceChanged(new ServiceInstance(model.getServiceName(), model.getProviderId(), model.getUri(),
                model.isMonitorEnabled(), model.getStatus(),model.getAddress(), model.getPort(),  model.getMetadata()));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateServiceUri(String serviceName,
            String providerId, String uri) throws ServiceException {

        ServiceInstanceUtils.validateServiceName(serviceName);
        ServiceInstanceUtils.validateServiceInstanceID(providerId);
        ServiceInstanceUtils.validateURI(uri);

        ProvidedServiceInstance model = getProvidedServiceInstance(serviceName, providerId);
        if(model == null){
            LOGGER.error("Update Service Instance URI failed - can not find the ServiceInstance.");
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.SERVICE_INSTANCE_NOT_EXIST,
                    serviceName);
            throw new DirectoryServerClientException(sde);
        }
        model.setUri(uri);

        onServiceInstanceChanged(new ServiceInstance(model.getServiceName(), model.getProviderId(), model.getUri(),
                model.isMonitorEnabled(), model.getStatus(), model.getAddress(), model.getPort(), model.getMetadata()));
    }

    /**
     * {@inheritDoc}
     *
     * @param serviceInstance
     *         the ProvidedServiceInstance.
     * @throws ServiceException
     */
    @Override
    public void updateService(ProvidedServiceInstance serviceInstance)
            throws ServiceException {

        ServiceInstanceUtils.validateProvidedServiceInstance(serviceInstance);

        updateMetadataKeyMap(serviceInstance);
        try{
            updateInstance(serviceInstance);
        } catch(ServiceRuntimeException e){
            throw new ServiceException(e);
        }

    }

    /**
     * {@inheritDoc}
     *
     * @param serviceName
     *         the service name.
     * @param providerId
     *         the providerId
     * @throws ServiceException
     */
    @Override
    public void unregisterService(String serviceName, String providerId)
            throws ServiceException {
        ServiceInstanceUtils.validateServiceName(serviceName);
        ServiceInstanceUtils.validateServiceInstanceID(providerId);
        
        deleteMetadataKeyMap(serviceName, providerId);
        ProvidedServiceInstance model = getProvidedServiceInstance(serviceName, providerId);
        if(model == null){
            LOGGER.error("Unregister Service failed - can not find the ServiceInstance.");
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.SERVICE_INSTANCE_NOT_EXIST,
                    serviceName);
            throw new DirectoryServerClientException(sde);
        }

        getService(serviceName).getServiceInstances().remove(model);

        onServiceInstanceUnavailable(new ServiceInstance(model.getServiceName(), model.getProviderId(), model.getUri(),
                model.isMonitorEnabled(), model.getStatus(), model.getAddress(), model.getPort(), model.getMetadata()));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceInstance queryInstanceByMetadataKey(ServiceInstanceQuery query)
            throws ServiceException {
        List<QueryCriterion> criteria = query.getCriteria();
        if (criteria != null && criteria.size() > 0) {

            String key = criteria.get(0).getMetadataKey();
            List<ServiceInstance> list = this.queryInstancesByMetadataKey(query);
            if(list == null || list.size() == 0){
                return null;
            }
            int index = getNextQueryInstanceIndex(key, list.size());
            return list.get(index);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceInstance> queryInstancesByMetadataKey(
            ServiceInstanceQuery query) throws ServiceException {
        List<QueryCriterion> criteria = query.getCriteria();
        if (criteria != null && criteria.size() > 0) {

            String key = criteria.get(0).getMetadataKey();
            if (metadataKeyCache.containsKey(key)) {
                List<ProvidedServiceInstance> instances = metadataKeyCache
                        .get(key);
                List<ServiceInstance> list = new ArrayList<ServiceInstance>();
                for (ProvidedServiceInstance model : instances) {
                    if (model.getStatus() == OperationalStatus.UP) {
                        list.add(new ServiceInstance(model.getServiceName(),
                                model.getProviderId(), model.getUri(), model.isMonitorEnabled(),
                                model.getStatus(), model.getAddress(), model.getPort(), model.getMetadata()));
                    }
                }

                return ServiceInstanceQueryHelper.filterServiceInstance(query,
                        list);
            }
        }

        return Collections.emptyList();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ServiceInstance getInstance(String serviceName, String instanceId)
            throws ServiceException {
        ProvidedServiceInstance instance = getProvidedServiceInstance(serviceName, instanceId);
        return new ServiceInstance(serviceName, instance.getProviderId(), instance.getUri(),
                instance.isMonitorEnabled(), instance.getStatus(), instance.getAddress(), instance.getPort(), instance.getMetadata());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceInstance> getAllInstances(String serviceName)
            throws ServiceException {
        ProvidedService service = null;
        if(cache.containsKey(serviceName)){
            service = cache.get(serviceName);
        }

        List<ServiceInstance> list = new ArrayList<ServiceInstance>();
        for (ProvidedServiceInstance model : service.getServiceInstances()) {
            list.add(new ServiceInstance(serviceName, model.getProviderId(),
                    model.getUri(), model.isMonitorEnabled(),
                    model.getStatus(), model.getAddress(), model.getPort(),
                    model.getMetadata()));
        }
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceInstance> getAllInstances(String serviceName,
            ServiceInstanceQuery query) throws ServiceException {
        List<ServiceInstance> instances = this.getAllInstances(serviceName);
        return ServiceInstanceQueryHelper.filterServiceInstance(query, instances);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceInstance> getAllInstances() throws ServiceException {

        List<ServiceInstance> instances = null;
        for(ProvidedService service : cache.values()){
            if(instances == null){
                instances = new ArrayList<ServiceInstance>();
            }
            for(ProvidedServiceInstance model : service.getServiceInstances()){
                instances.add(new ServiceInstance(model.getServiceName(), model.getProviderId(), model.getUri(),
                        model.isMonitorEnabled(), model.getStatus(), model.getAddress(), model.getPort(), model.getMetadata()));
            }
        }
        if(instances == null ){
            return Collections.emptyList();
        }else{
            return instances;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ServiceInstance> getAllInstancesByMetadataKey(ServiceInstanceQuery query)
            throws ServiceException {
        List<QueryCriterion> criteria = query.getCriteria();
        if (criteria != null && criteria.size() > 0) {

            String key = criteria.get(0).getMetadataKey();
            if (metadataKeyCache.containsKey(key)) {
                List<ProvidedServiceInstance> instances = metadataKeyCache
                        .get(key);
                List<ServiceInstance> list = new ArrayList<ServiceInstance>();
                for (ProvidedServiceInstance model : instances) {
                    list.add(new ServiceInstance(model.getServiceName(),
                            model.getProviderId(), model.getUri(),
                            model.isMonitorEnabled(), model.getStatus(), model.getAddress(), model.getPort(), model.getMetadata()));
                }

                return ServiceInstanceQueryHelper.filterServiceInstance(query,
                        list);
            }
        }

        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addNotificationHandler(String serviceName,
            NotificationHandler handler) throws ServiceException {
        if(handler == null || serviceName == null || serviceName.isEmpty()){
            throw new IllegalArgumentException();
        }

        synchronized(notificationHandlers){
            if(! notificationHandlers.containsKey(serviceName)){
                notificationHandlers.put(serviceName, new ArrayList<NotificationHandler>());
            }

            notificationHandlers.get(serviceName).add(handler);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeNotificationHandler(String serviceName,
            NotificationHandler handler) throws ServiceException {
        if(handler == null || serviceName == null || serviceName.isEmpty()){
            throw new IllegalArgumentException();
        }

        synchronized(notificationHandlers){
            if(notificationHandlers.containsKey(serviceName)){
                List<NotificationHandler> list = notificationHandlers.get(serviceName);
                if(list.contains(handler)){
                    list.remove(handler);
                }
                if(list.size() == 0){
                    notificationHandlers.remove(serviceName);
                }
            }
        }
    }

    /**
     * Get the ServiceDirectoryCache.
     *
     * @return
     *         the ServiceDirectoryCache.
     */
    protected ConcurrentHashMap<String, ProvidedService> getServiceDirectoryCache(){
        return this.cache;
    }

    /**
     * On a ServiceInstance Unavailable.
     *
     * It will invoke the serviceInstanceUnavailable of the NotificationHandler.
     *
     * @param instance
     *         the ServiceInstance.
     */
    private void onServiceInstanceUnavailable(ServiceInstance instance){
        if(instance == null){
            return ;
        }
        String serviceName = instance.getServiceName();
        synchronized(notificationHandlers){
            if(notificationHandlers.containsKey(serviceName)){
                for(NotificationHandler h : notificationHandlers.get(serviceName)){
                    h.serviceInstanceUnavailable(instance);
                }
            }
        }
    }

    /**
     * On a ServiceInstance Unavailable.
     *
     * It will invoke the serviceInstanceChange of the NotificationHandler.
     *
     * @param instance
     *         the ServiceInstance.
     */
    private void onServiceInstanceChanged(ServiceInstance instance){
        String serviceName = instance.getServiceName();
        synchronized(notificationHandlers){
            if(notificationHandlers.containsKey(serviceName)){
                for(NotificationHandler h : notificationHandlers.get(serviceName)){
                    h.serviceInstanceChange(instance);
                }
            }
        }
    }

    /**
     * On a ServiceInstance Unavailable.
     *
     * It will invoke the serviceInstanceAvailable of the NotificationHandler.
     *
     * @param instance
     *         the ServiceInstance.
     */
    private void onServiceInstanceAvailable(ServiceInstance instance){
        String serviceName = instance.getServiceName();
        synchronized(notificationHandlers){
            if(notificationHandlers.containsKey(serviceName)){
                for(NotificationHandler h : notificationHandlers.get(serviceName)){
                    h.serviceInstanceAvailable(instance);
                }
            }
        }
    }

    /**
     * Get the ProvidedServiceInstance from cache.
     *
     * @param serviceName
     *         the service name.
     * @param instanceId
     *         the instanceId.
     * @return
     *         the ProvidedServiceInstance.
     */
    private ProvidedServiceInstance getProvidedServiceInstance(String serviceName, String instanceId){
        if(this.cache.containsKey(serviceName)){
            List<ProvidedServiceInstance> instances = cache.get(serviceName).getServiceInstances();
            for(ProvidedServiceInstance ist : instances){
                if(ist.getProviderId().equals(instanceId)){
                    return ist;
                }
            }
        }
        return null;
    }

    /**
     * Validate the ProvidedServiceInstance.
     *
     * @param instance
     *         the ProvidedServiceInstance.
     */
    private void validateProvidedServiceInstance(ProvidedServiceInstance instance){
        if(instance.getUri() == null || instance.getUri().isEmpty()){
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.SERVICE_INSTANCE_URI_FORMAT_ERROR,
                    instance.getServiceName());
            throw new DirectoryServerClientException(sde);
        }
    }

    /**
     * Obtain a ProvidedService, and create a new one if not existed.
     *
     * @param serviceName
     *         the service name.
     * @return
     *         the ProvidedService.
     */
    private ProvidedService createServiceIfNotExists(String serviceName){
        if(cache.containsKey(serviceName)){
            return cache.get(serviceName);
        }else{
            ProvidedService service = new ProvidedService(serviceName);
            List<ProvidedServiceInstance> ins = new ArrayList<ProvidedServiceInstance>();
            service.setServiceInstances(ins);
            cache.put(serviceName, service);
            return service;
        }
    }

    /**
     * Get the ProvidedService with its instances.
     *
     * @param serviceName
     *         the service name.
     * @return
     *         the ProvidedService.
     */
    private ProvidedService getService(String serviceName){
        if(cache.containsKey(serviceName)){
            return cache.get(serviceName);
        }
        return null;
    }

    /**
     * Update the ServiceInstance.
     *
     * @param instance
     *         the ProvidedServiceInstance.
     * @throws ServiceException
     */
    private void updateInstance(ProvidedServiceInstance instance)
            throws ServiceException {


        validateProvidedServiceInstance(instance);

        String serviceName = instance.getServiceName();
        String instanceId = instance.getProviderId();

        ProvidedServiceInstance model = getProvidedServiceInstance(serviceName, instanceId);
        if(model == null){
            LOGGER.error("Update Service failed - cannot find the ServiceInstance");
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.SERVICE_INSTANCE_NOT_EXIST,
                    instance.getServiceName());
            throw new DirectoryServerClientException(sde);
        }

        model.setMonitorEnabled(instance.isMonitorEnabled());
        model.setStatus(instance.getStatus());
        model.setUri(instance.getUri());

        // Change the metadata when metadata is not null.
        if(instance.getMetadata() != null){
            Map<String, String> modelMetadata = new HashMap<String, String>();
            model.setMetadata(modelMetadata);

            for(Entry<String, String> entry : instance.getMetadata().entrySet()){
                modelMetadata.put(entry.getKey(), entry.getValue());
            }
        }

        onServiceInstanceChanged(new ServiceInstance(model.getServiceName(), model.getProviderId(), model.getUri(),
                model.isMonitorEnabled(), model.getStatus(), model.getAddress(), model.getPort(), model.getMetadata()));
    }

    /**
     * Update the ProvidedServiceInstance OperationalStatus.
     *
     * @param serviceName
     *         the service name.
     * @param instanceId
     *         the instanceId
     * @param status
     *         the OperationalStatus of the ServiceInstance.
     * @throws ServiceException
     */
    protected void updateInstanceStatus(String serviceName, String instanceId,
            OperationalStatus status) throws ServiceException {

        ProvidedServiceInstance model = getProvidedServiceInstance(serviceName, instanceId);
        if(model == null){
            LOGGER.error("Update Service OperationalStatus failed - cannot find the ServiceInstance");
            ServiceDirectoryError sde = new ServiceDirectoryError(
                    ErrorCode.SERVICE_INSTANCE_NOT_EXIST,
                    serviceName);
            throw new DirectoryServerClientException(sde);
        }

        model.setStatus(status);
    }

    /**
     * Add the ProvidedServiceInstance to metadata key map.
     *
     * @param serviceInstance
     *         the ProvidedServiceInstance.
     */
    private void addMetadataKeyMap(ProvidedServiceInstance serviceInstance){
        String serviceName = serviceInstance.getServiceName();
        String instanceId = serviceInstance.getProviderId();

        ProvidedServiceInstance cachedInstance = this.getProvidedServiceInstance(serviceName, instanceId);
        if(cachedInstance == null){
            return;
        }

        for(Entry<String, String> en : serviceInstance.getMetadata().entrySet()){
            String key = en.getKey();
            if(! this.metadataKeyCache.containsKey(key)){
                List<ProvidedServiceInstance> instances = new ArrayList<ProvidedServiceInstance>();
                this.metadataKeyCache.put(key, instances);
            }
            this.metadataKeyCache.get(key).add(cachedInstance);
        }
    }

    /**
     * Update the metadata key for the new ProvidedServiceInstance.
     *
     * It removes the metadata key map from the old ProvidedServiceInstance, and adds metadata key
     * to the new ProvidedServiceInstance.
     *
     * @param serviceInstance
     *         the ProvidedServiceInstance.
     */
    private void updateMetadataKeyMap(ProvidedServiceInstance serviceInstance){
        // Remove old mapping.
        deleteMetadataKeyMap(serviceInstance.getServiceName(), serviceInstance.getProviderId());
        addMetadataKeyMap(serviceInstance);
    }

    /**
     * Delete the metadatakey for the ProvidedServiceInstance.
     *
     * @param serviceName
     *         the serviceName of ProvidedServiceInstance.
     * @param providerId
     *         the providerId of ProvidedServiceInstance.
     */
    private void deleteMetadataKeyMap(String serviceName, String providerId){

        ProvidedServiceInstance cachedInstance = this.getProvidedServiceInstance(serviceName, providerId);
        if(cachedInstance == null){
            return ;
        }
        for(Entry<String, String> en : cachedInstance.getMetadata().entrySet()){
            String key = en.getKey();
            if(! this.metadataKeyCache.containsKey(key)){
                List<ProvidedServiceInstance> instances = new ArrayList<ProvidedServiceInstance>();
                this.metadataKeyCache.put(key, instances);
            }
            this.metadataKeyCache.get(key).remove(cachedInstance);
        }
    }

    /**
     * Get the next index of ServiceInstance List in the queryList.
     *
     * @param serviceName    the name of the service.
     * @param max            The max index in the ServiceInstance List, it is size()-1.
     * @return
     *         the next ServiceInstance index in query.
     */
    private int getNextQueryInstanceIndex(String serviceName, int max){
        if(max == 0){
            return 0;
        }

        if(this.queryInstanceIdx.containsKey(serviceName)){
            AtomicInteger pre = this.queryInstanceIdx.get(serviceName);
            int theIndex = pre.incrementAndGet();
            return theIndex % max;
        }else{
            this.queryInstanceIdx.put(serviceName, new AtomicInteger(0));
            return 0;
        }
    }

    /**
     * Deep clone a ProvidedServiceInstance.
     *
     * It can clone all object reference and the Collections.
     *
     * @param instance
     *         the ProvidedServiceInstance.
     * @return
     *         the new ProvidedServiceInstance cloned.
     */
    private ProvidedServiceInstance deepCloneProvidedServiceInstance(ProvidedServiceInstance instance){
        ProvidedServiceInstance newInstance = new ProvidedServiceInstance(
                instance.getServiceName(), instance.getAddress(), instance.getPort());
        newInstance.setStatus(instance.getStatus());
        newInstance.setMonitorEnabled(instance.isMonitorEnabled());
        newInstance.setUri(instance.getUri());

        Map<String, String> meta = new HashMap<String, String>();
        for(Entry<String, String> entry : instance.getMetadata().entrySet()){
            meta.put(entry.getKey(), entry.getValue());
        }
        newInstance.setMetadata(meta);
        return newInstance;

    }

    /**
     * The Service Object in Service Directory test framework.
     *
     * It is the logic Service Object in Service Directory. The ProvidedServiceInstances with the same service
     * name belong to one ProvidedService.
     * It is only used in the test framework.
     *
     *
     */
    public static class ProvidedService {

        /**
         * The name.
         */
        private String name;

        /**
         * The ServiceInstances of the Service.
         */
        private List<ProvidedServiceInstance> serviceInstances;

        /**
         * Constructor.
         */
        public ProvidedService(){

        }

        /**
         * Constructor.
         *
         * @param name
         *         the name.
         */
        public ProvidedService(String name){
            this.name = name;
        }

        /**
         * Get service name.
         *
         * @return
         *         the service name.
         */
        public String getName() {
            return name;
        }

        /**
         * Set service name.
         *
         * @param name
         *         the service name.
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Get the ProvidedServiceInstance list.
         *
         * @return
         *         the ProviidedServiceInstance list.
         */
        public List<ProvidedServiceInstance> getServiceInstances() {
            return serviceInstances;
        }

        /**
         * Set the ProviidedServiceInstance list.
         * @param serviceInstances
         *         the ProviidedServiceInstance list.
         */
        public void setServiceInstances(List<ProvidedServiceInstance> serviceInstances) {
            this.serviceInstances = serviceInstances;
        }
    }
}
