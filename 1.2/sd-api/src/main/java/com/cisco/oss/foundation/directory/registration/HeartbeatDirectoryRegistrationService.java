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




package com.cisco.oss.foundation.directory.registration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.ServiceInstanceHealth;
import com.cisco.oss.foundation.directory.client.DirectoryServiceClient;
import com.cisco.oss.foundation.directory.entity.OperationResult;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstanceHeartbeat;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.lifecycle.Stoppable;

import static com.cisco.oss.foundation.directory.ServiceDirectory.getServiceDirectoryConfig;

/**
 * The DirectoryRegistrationService with heartbeat and ServiceInstanceHealth callback checking.
 *
 * The HeartbeatDirectoryRegistrationService will cache the registered ProvidedServiceInstance at the startup,
 * and send heartbeats, and perform ServiceInstanceHealth callback for the monitorEnabled ProvidedServiceInstance.
 *
 *
 */
public class HeartbeatDirectoryRegistrationService extends
        DirectoryRegistrationService implements Stoppable {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(HeartbeatDirectoryRegistrationService.class);

    /**
     * The RegistrationManager health check executor kickoff delay time property
     * name in seconds.
     */
    public static final String SD_API_REGISTRY_HEALTH_CHECK_DELAY_PROPERTY = "com.cisco.oss.foundation.directory.registry.health.check.delay";

    /**
     * The default delay time of health check executor kickoff.
     */
    public static final int SD_API_REGISTRY_HEALTH_CHECK_DELAY_DEFAULT = 1;

    /**
     * The RegistrationManager health check interval property name in seconds.
     */
    public static final String SD_API_REGISTRY_HEALTH_CHECK_INTERVAL_PROPERTY = "com.cisco.oss.foundation.directory.registry.health.check.interval";

    /**
     * The default health check interval value of RegistrationManager.
     */
    public static final int SD_API_REGISTRY_HEALTH_CHECK_INTERVAL_DEFAULT = 5;

    /**
     * The RegistrationManager heartbeat executor kickoff delay time property name in seconds.
     */
    public static final String SD_API_HEARTBEAT_DELAY_PROPERTY = "com.cisco.oss.foundation.directory.heartbeat.delay";

    /**
     * The default delay time of RegistrationManager heartbeat executor kickoff
     */
    public static final int SD_API_HEARTBEAT_DELAY_DEFAULT = 1;

    /**
     * The RegistrationManager send ServiceInstance heartbeat interval property name.
     */
    public static final String SD_API_HEARTBEAT_INTERVAL_PROPERTY = "com.cisco.oss.foundation.directory.heartbeat.interval";

    /**
     * The default interval value of RegistrationManager send ServiceInstance heartbeats.
     */
    public static final int SD_API_HEARTBEAT_INTERVAL_DEFAULT = 10;

    /**
     * The CachedProviderServiceInstance Set
     */
    private final HashMap<ServiceInstanceId, CachedProviderServiceInstance> instanceCache;

    /**
     * ServiceInstanceHealth check ExecutorService.
     */
    private final AtomicReference<ScheduledExecutorService> healthCheckService = new AtomicReference<>();

    /**
     * The heartbeat executor service.
     */
    private final AtomicReference<ScheduledExecutorService> heartbeatService = new AtomicReference<>();

    /**
     * Mark whether component is started?
     */
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * The Read and Write lock to protect the CachedProviderServiceInstance Set.
     */
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * The Read lock.
     */
    private final Lock read = readWriteLock.readLock();

    /**
     * The Write lock.
     */
    private final Lock write = readWriteLock.writeLock();

    /**
     * Start the component.
     */
    @Override
    public void start() {
        if(isStarted.compareAndSet(false,true)){
            scheduleTasks();
        }
    }

    /**
     * Stop the Component.
     *
     * it is idempotent, it can be invoked in multiple times.
     */
    @Override
    public void stop() {
        if(isStarted.compareAndSet(true,false)){
            ScheduledExecutorService healthCheck = healthCheckService.getAndSet(newHealthCheckService());
            healthCheck.shutdown();
            ScheduledExecutorService heartbeat = heartbeatService.getAndSet(newHeartbeatService());
            heartbeat.shutdown();
        }
    }

    /**
     * Constructor.
     *
     * @param directoryServiceClient
     *         the DirectoryServiceClientManager to get DirectoryServiceClient.
     */
    public HeartbeatDirectoryRegistrationService(
            DirectoryServiceClient directoryServiceClient) {
        super(directoryServiceClient);
        instanceCache = new HashMap<ServiceInstanceId, CachedProviderServiceInstance>();
        healthCheckService.set(newHealthCheckService());
        heartbeatService.set(newHeartbeatService());
     }

    private ScheduledExecutorService newHealthCheckService(){
        return Executors
                .newSingleThreadScheduledExecutor(new ThreadFactory() {
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setName("SD API RegistryHealth Check");
                        t.setDaemon(true);
                        return t;
                    }
                });
    }
    private ScheduledExecutorService newHeartbeatService(){
        return Executors
                .newSingleThreadScheduledExecutor(new ThreadFactory() {
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setName("SD API Heartbeat");
                        t.setDaemon(true);
                        return t;
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerService(ProvidedServiceInstance serviceInstance) {
        super.registerService(serviceInstance);
        if(serviceInstance.isMonitorEnabled()){
            registerCachedServiceInstance(serviceInstance, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerService(ProvidedServiceInstance serviceInstance, ServiceInstanceHealth registryHealth) {
        super.registerService(serviceInstance, registryHealth);
        registerCachedServiceInstance(serviceInstance, registryHealth);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateServiceUri(String serviceName, String providerAddress, String uri){
        boolean isOwned = false;
        CachedProviderServiceInstance inst = getCachedServiceInstance(serviceName, providerAddress);

        if(inst != null){
            isOwned = true;
        }

        this.getServiceDirectoryClient().updateInstanceUri(serviceName, providerAddress, uri, isOwned);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateServiceOperationalStatus(String serviceName,
            String providerAddress, OperationalStatus status) {
        boolean isOwned = false;
        CachedProviderServiceInstance inst = getCachedServiceInstance(serviceName, providerAddress);

        if(inst != null){
            isOwned = true;
            inst.setStatus(status);
        }

        this.getServiceDirectoryClient().updateInstanceStatus(serviceName, providerAddress, status, isOwned);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateService(ProvidedServiceInstance serviceInstance) {
        if(serviceInstance.isMonitorEnabled()){
            CachedProviderServiceInstance inst = getCachedServiceInstance(serviceInstance.getServiceName(),
                    serviceInstance.getAddress());

            if(inst == null){
                throw new ServiceException(ErrorCode.ILLEGAL_SERVICE_INSTANCE_OWNER_ERROR);
            }
            this.editCachedServiceInstance(serviceInstance);

        }
        super.updateService(serviceInstance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterService(String serviceName, String providerAddress) {
        boolean isOwned = false;
        CachedProviderServiceInstance inst = getCachedServiceInstance(serviceName, providerAddress);

        if(inst != null){
            isOwned = true;
            this.unregisterCachedServiceInstance(serviceName, providerAddress);
        }

        this.getServiceDirectoryClient().unregisterInstance(serviceName, providerAddress, isOwned);
    }

    /**
     * Register a ProvidedServiceInstance to the Cache.
     *
     * Add/update the ProvidedServiceInstance in the cache. 
     *
     * It is thread safe.
     *
     * @param instance
     *         the ProvidedServiceInstance.
     * @param registryHealth
     *         the ServiceInstanceHealth callback.
     */
    private void registerCachedServiceInstance(ProvidedServiceInstance instance, ServiceInstanceHealth registryHealth) {
        try {
            write.lock();
            ServiceInstanceId id = new ServiceInstanceId(instance.getServiceName(),
                    instance.getAddress());
            CachedProviderServiceInstance cachedInstance = getCacheServiceInstances().get(id);

            if(cachedInstance == null){
                cachedInstance = new CachedProviderServiceInstance(
                    instance);
                getCacheServiceInstances().put(id, cachedInstance);

            }
            
            LOGGER.debug("add cached ProvidedServiceInstance: {}.", cachedInstance);
            cachedInstance.setServiceInstanceHealth(registryHealth);
        } finally {
            write.unlock();
        }
    }

    /**
     * Edit the Cached ProvidedServiceInstance when updateService is called.
     *
     * It is thread safe. 
     *
     * @param instance
     *         the ProvidedServiceInstance.
     */
    private void editCachedServiceInstance(ProvidedServiceInstance instance) {
        try {
            write.lock();
            ServiceInstanceId id = new ServiceInstanceId(instance.getServiceName(),
                    instance.getAddress());
            CachedProviderServiceInstance cachedInstance = getCacheServiceInstances().get(id);
            if(cachedInstance != null){
                cachedInstance.setMonitorEnabled(instance.isMonitorEnabled());
                cachedInstance.setStatus(instance.getStatus());
            }
            
            LOGGER.debug("update cached ProvidedServiceInstance: {}.", cachedInstance);

        } finally {
            write.unlock();
        }
    }

    /**
     * Get the Cached ProvidedServiceInstance by serviceName and providerAddress.
     *
     * It is thread safe.
     *
     * @param serviceName
     *         the serviceName
     * @param providerAddress
     *         the providerAddress
     * @return
     *         the CachedProviderServiceInstance
     */
    private CachedProviderServiceInstance getCachedServiceInstance(
            String serviceName, String providerAddress) {
        
		ServiceInstanceId id = new ServiceInstanceId(serviceName, providerAddress);
		return getCacheServiceInstances().get(id);
       
    }

    /**
     * Delete the Cached ProvidedServiceInstance by serviceName and providerAddress.
     *
     * It is thread safe.
     *
     * @param serviceName
     *         the serviceName.
     * @param providerAddress
     *         the providerAddress.
     */
    private void unregisterCachedServiceInstance(
            String serviceName, String providerAddress) {
        try {
            write.lock();
            ServiceInstanceId id = new ServiceInstanceId(serviceName, providerAddress);
            getCacheServiceInstances().remove(id);
            LOGGER.debug(
                    "delete cached ProvidedServiceInstance, serviceName={}, providerAddress={}.",
                    serviceName, providerAddress);
        } finally {
            write.unlock();
        }
    }

    /**
     * Get the CachedProviderServiceInstance Set.
     *
     * It is lazy initialized and thread safe.
     *
     * @return
     *         the CachedProviderServiceInstance Set.
     */
    private HashMap<ServiceInstanceId, CachedProviderServiceInstance> getCacheServiceInstances() {
       return instanceCache;
    }

    /**
     * schedule the Heartbeat task and Health Check task.
     */
    private void scheduleTasks() {

        int rhDelay = getServiceDirectoryConfig().getInt(
                SD_API_REGISTRY_HEALTH_CHECK_DELAY_PROPERTY,
                SD_API_REGISTRY_HEALTH_CHECK_DELAY_DEFAULT);
        int rhInterval = getServiceDirectoryConfig().getInt(
                SD_API_REGISTRY_HEALTH_CHECK_INTERVAL_PROPERTY,
                SD_API_REGISTRY_HEALTH_CHECK_INTERVAL_DEFAULT);
        LOGGER.info(
                "Start the SD API RegistryHealth Task scheduler, delay={}, interval={}.",
                rhDelay, rhInterval);
        healthCheckService.get().scheduleAtFixedRate(new HealthCheckTask(), rhDelay,
                rhInterval, TimeUnit.SECONDS);

        int hbDelay = getServiceDirectoryConfig().getInt(SD_API_HEARTBEAT_DELAY_PROPERTY,
                SD_API_HEARTBEAT_DELAY_DEFAULT);
        int hbInterval = getServiceDirectoryConfig().getInt(
                SD_API_HEARTBEAT_INTERVAL_PROPERTY,
                SD_API_HEARTBEAT_INTERVAL_DEFAULT);
        LOGGER.info(
                "Start the SD API Heartbeat Task scheduler, delay={}, interval={}.",
                hbDelay, hbInterval);
        heartbeatService.get().scheduleAtFixedRate(new HeartbeatTask(), hbDelay,
                hbInterval, TimeUnit.SECONDS);
    }

    /**
     * The ServiceInstanceHealth checking task.
     *
     *
     */
    private class HealthCheckTask implements Runnable {

        @Override
        public void run() {
            read.lock();
            try {
                
                LOGGER.debug("Kick off the HealthCheckTask thread");

                for (CachedProviderServiceInstance ist : getCacheServiceInstances().values()) {
                    if (ist.getServiceInstanceHealth() == null) {
                        continue;
                    }
                   
                    LOGGER.debug(
                            "Check the Health for service={}, providerAddress={}.",
                            ist.getServiceName(), ist.getProviderAddress());

                    ist.isHealth = ist.getServiceInstanceHealth().isHealthy();
                }
            } catch (Exception e) {
                LOGGER.error("ServiceInstanceHealth callback check failed.", e);
            } finally{
                read.unlock();
            }
        }

    }

    /**
     * The heartbeat task.
     *
     *
     */
    private class HeartbeatTask implements Runnable {

        @Override
        public void run() {
        
            LOGGER.debug("Kick off the heartbeat thread");
            List<ServiceInstanceHeartbeat> serviceHBList = new ArrayList<ServiceInstanceHeartbeat>();
            
            read.lock();
            try {
                for (CachedProviderServiceInstance cachedInstance : getCacheServiceInstances().values()) {
                    LOGGER.debug("Service instance: {}.", cachedInstance);
                    if (cachedInstance.monitorEnabled && OperationalStatus.UP.equals(cachedInstance.status)
                            && cachedInstance.isHealth) {
                        ServiceInstanceHeartbeat hb = new ServiceInstanceHeartbeat(
                                cachedInstance.getServiceName(),
                                cachedInstance.getProviderAddress());
                        serviceHBList.add(hb);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to add services to serviceHBList.", e);
            } finally {
            	read.unlock();
            }
                
            if (serviceHBList.isEmpty()) {
                return;
            }
            
            LOGGER.debug(
                      "Send heartbeat for ServiceInstances, ServiceInstanceNumber={}.",
                      serviceHBList.size());
        
            try {
                Map<String, ServiceInstanceHeartbeat> heartbeatMap = new HashMap<String, ServiceInstanceHeartbeat>();
                for (ServiceInstanceHeartbeat instance : serviceHBList) {
                    String id = instance.getServiceName() + "-"
                            + instance.getProviderAddress();
                    heartbeatMap.put(id, instance);
                }

                Map<String, OperationResult<String>> operateResult = getServiceDirectoryClient()
                        .sendHeartBeat(heartbeatMap);
                if (operateResult != null) {
                    for (Entry<String, OperationResult<String>> entry : operateResult
                            .entrySet()) {
                        boolean result = entry.getValue().getResult();
                        if (!result) {
                            ServiceInstanceHeartbeat instance = heartbeatMap
                                    .get(entry.getKey());
                            LOGGER.error(
                                    "Send heartbeat failed, serviceName={}, providerAddress={}. {}.",
                                            instance.getServiceName(),
                                            instance.getProviderAddress(),
                                            entry.getValue().getError().getErrorMessage());
                  }
                    }
                } else {
                    LOGGER.error("No heartbeat response from Directory Server.");
                }
            } catch (Exception e) {
                LOGGER.error("Send heartbeat failed.", e);
            }
        }

    }

    /**
     * The cached ProviderServiceInstance for the ServiceInstanceHealth and heartbeat.
     */
    private static class CachedProviderServiceInstance {
        /**
         * The ServiceName of ProvidedServiceInstance.
         */
        private final String serviceName;

        /**
         * The providerAddress of ProvidedServiceInstance.
         */
        private final String providerAddress;

        /**
         * Whether the instance enabled Monitor in Service Directory.
         */
        private boolean monitorEnabled = true;

        /**
         * The instance OperationalStatus.
         */
        private OperationalStatus status;

        /**
         * The ServiceInstanceHealth callback of the ProvidedServiceInstance.
         */
        private ServiceInstanceHealth healthCallback;

        /**
         * Store the ServiceInstanceHealth callback result.
         * If the ServiceInstanceHealth is null, default to true;
         */
        private boolean isHealth = true;

        /**
         * Constructor
         *
         * @param serviceInstance
         *            the ProvidedServiceInstance.
         */
        public CachedProviderServiceInstance(
                ProvidedServiceInstance serviceInstance) {
            this.serviceName = serviceInstance.getServiceName();
            this.providerAddress = serviceInstance.getAddress();
            this.monitorEnabled = serviceInstance.isMonitorEnabled();
            this.status = serviceInstance.getStatus();
        }

        /**
         * Set the ServiceInstanceHealth.
         *
         * @param healthCallback
         *            the ServiceInstanceHealth.
         */
        public void setServiceInstanceHealth(
                ServiceInstanceHealth healthCallback) {
            this.healthCallback = healthCallback;
            this.isHealth = true;
        }

        /**
         * Get the ServiceInstanceHealth.
         *
         * @return the ServiceInstanceHealth callback.
         */
        public ServiceInstanceHealth getServiceInstanceHealth() {
            return healthCallback;
        }

        /**
         * Get the service name.
         *
         * @return the service name.
         */
        public String getServiceName() {
            return serviceName;
        }

        /**
         * Get the providerAddress.
         *
         * @return the providerAddress.
         */
        public String getProviderAddress() {
            return providerAddress;
        }

        /**
         * Get the OperationalStatus.
         *
         * @return the OperationalStatus.
         */
        public OperationalStatus getStatus() {
            return status;
        }

        /**
         * Set the OperationalStatus.
         *
         * @param status
         *            the OperationalStatus.
         */
        public void setStatus(OperationalStatus status) {
            this.status = status;
        }

        /**
         * check whether it is monitor-enabled service instance
         *
         * @return true if monitor enabled.
         */
        public boolean isMonitorEnabled() {
            return monitorEnabled;
        }

        /**
         * Set the service to be monitored.
         *
         * @param monitor
         *            true or false.
         */
        public void setMonitorEnabled(boolean monitor) {
            this.monitorEnabled = monitor;
        }

        @Override
        public String toString() {
            return "serviceName=" + serviceName + ", providerAddress=" + providerAddress + ", status=" + status +", monitor=" + monitorEnabled + ", isHealth=" + isHealth;
        }
    }

    private static class ServiceInstanceId{
        private String serviceName;
        private String providerAddress;

        public ServiceInstanceId(String serviceName, String providerAddress){
            this.serviceName = serviceName;
            this.providerAddress = providerAddress;
        }
        @Override
        public String toString() {
            return "serviceName=" + serviceName + ", providerAddress=" + providerAddress;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof ServiceInstanceId) {
                ServiceInstanceId instance = (ServiceInstanceId) obj;
                return (instance.serviceName.equals(serviceName) && instance.providerAddress
                        .equals(providerAddress));
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = providerAddress != null ? providerAddress.hashCode() : 0;
            result = 31 * result + serviceName != null ? serviceName.hashCode()
                    : 0;
            return result;
        }
    }
}
