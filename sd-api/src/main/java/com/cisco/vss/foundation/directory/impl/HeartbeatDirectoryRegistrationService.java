/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.vss.foundation.directory.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.vss.foundation.directory.Configurations;
import com.cisco.vss.foundation.directory.DirectoryServiceClientManager;
import com.cisco.vss.foundation.directory.ServiceInstanceHealth;
import com.cisco.vss.foundation.directory.entity.OperationResult;
import com.cisco.vss.foundation.directory.entity.OperationalStatus;
import com.cisco.vss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.vss.foundation.directory.entity.ServiceInstanceHeartbeat;
import com.cisco.vss.foundation.directory.exception.ServiceRuntimeException;
import com.cisco.vss.foundation.directory.lifecycle.Closable;

/**
 * The DirectoryRegistrationService with heartbeat and ServiceInstanceHealth callback checking.
 * 
 * The HeartbeatDirectoryRegistrationService will cache the registered ProvidedServiceInstance at the startup, 
 * and send heartbeats, and perform ServiceInstanceHealth callback for the monitorEnabled ProvidedServiceInstance.
 * 
 * @author zuxiang
 *
 */
public class HeartbeatDirectoryRegistrationService extends
		DirectoryRegistrationService implements Closable {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(HeartbeatDirectoryRegistrationService.class);

	/**
	 * The RegistrationManager health check executor kick off delay time
	 * property name in seconds.
	 */
	public static final String SD_API_REGISTRY_HEALTH_CHECK_DELAY_PROPERTY = "registry.health.check.delay";

	/**
	 * The default delay time of health check executor kick off.
	 */
	public static final int SD_API_REGISTRY_HEALTH_CHECK_DELAY_DEFAULT = 1;

	/**
	 * The RegistrationManager health check interval property name in seconds.
	 */
	public static final String SD_API_REGISTRY_HEALTH_CHECK_INTERVAL_PROPERTY = "registry.health.check.interval";

	/**
	 * The default health check interval value of RegistrationManager.
	 */
	public static final int SD_API_REGISTRY_HEALTH_CHECK_INTERVAL_DEFAULT = 5;

	/**
	 * The RegistrationManager heart beat executor kick off delay time property
	 * name in seconds.
	 */
	public static final String SD_API_HEARTBEAT_DELAY_PROPERTY = "heartbeat.delay";

	/**
	 * The default delay time of RegistrationManager heart beat executor kick
	 * off.
	 */
	public static final int SD_API_HEARTBEAT_DELAY_DEFAULT = 1;

	/**
	 * The RegistrationManager send ServiceInstance heart beat interval property
	 * name.
	 */
	public static final String SD_API_HEARTBEAT_INTERVAL_PROPERTY = "heartbeat.interval";

	/**
	 * The default interval value of RegistrationManager send ServiceInstance
	 * heart beat.
	 */
	public static final int SD_API_HEARTBEAT_INTERVAL_DEFAULT = 10;

	/**
	 * All ServiceInstanceHealth set collection.
	 */
	private Set<CachedProviderServiceInstance> instanceCache;

	/**
	 * ServiceInstanceHealth check ExecutorService.
	 */
	private ScheduledExecutorService healthJob;

	/**
	 * The heartbeat executor service.
	 */
	private ScheduledExecutorService heartbeatJob;

	/**
	 * Mark whether component is started?
	 */
	private boolean isStarted = false;

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

		if (isStarted) {
			return;
		}
		isStarted = true;

	}

	/**
	 * Stop the Component.
	 * 
	 * it is idempotent, it can be invoked in multiple times while in same
	 * state. But not thread safe.
	 */
	@Override
	public void stop() {
		if (isStarted) {
			isStarted = false;
			if (heartbeatJob != null) {
				heartbeatJob.shutdown();
			}
			if (healthJob != null) {
				healthJob.shutdown();
			}
		}

	}

	/**
	 * Constructor.
	 * 
	 * @param directoryServiceClientManager
	 * 		the DirectoryServiceClientManager to get DirectoryServiceClient.
	 */
	public HeartbeatDirectoryRegistrationService(
			DirectoryServiceClientManager directoryServiceClientManager) {
		super(directoryServiceClientManager);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerService(ProvidedServiceInstance serviceInstance) {
		super.registerService(serviceInstance);
		addCachedServiceInstance(serviceInstance, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerService(ProvidedServiceInstance serviceInstance,
			OperationalStatus status) {
		super.registerService(serviceInstance, status);
		serviceInstance.setStatus(status);
		addCachedServiceInstance(serviceInstance, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerService(ProvidedServiceInstance serviceInstance,
			OperationalStatus status, ServiceInstanceHealth registryHealth) {
		super.registerService(serviceInstance, status, registryHealth);
		serviceInstance.setStatus(status);
		addCachedServiceInstance(serviceInstance, registryHealth);
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateServiceOperationalStatus(String serviceName,
			String providerId, OperationalStatus status) {
		super.updateServiceOperationalStatus(serviceName, providerId, status);
		CachedProviderServiceInstance inst = getCachedServiceInstance(serviceName, providerId);
		if(inst != null){
			inst.setStatus(status);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateService(ProvidedServiceInstance serviceInstance) {
		super.updateService(serviceInstance);
		if(! serviceInstance.isMonitorEnabled()){
			this.editCachedServiceInstance(serviceInstance);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unregisterService(String serviceName, String providerId) {
		super.unregisterService(serviceName, providerId);
		this.deleteCachedServiceInstance(serviceName, providerId);
	}

	/**
	 * Add a ProvidedServiceInstance to the Cache.
	 * 
	 * Add the ProvidedServiceInstance to cache, if it already exits, remove it
	 * and add a new one.
	 * 
	 * It is thread safe.
	 * 
	 * @param instance
	 * 		the ProvidedServiceInstance.
	 * @param registryHealth
	 * 		the ServiceInstanceHealth callback.
	 */
	private void addCachedServiceInstance(ProvidedServiceInstance instance, ServiceInstanceHealth registryHealth) {
		try {
			write.lock();
			CachedProviderServiceInstance cachedinstance = getCachedServiceInstance(instance.getServiceName(),
					instance.getProviderId());
			if (cachedinstance != null) {
				getCacheServiceInstances().remove(cachedinstance);
			}
			
			CachedProviderServiceInstance cachedInstance = new CachedProviderServiceInstance(
					instance);
			LOGGER.info("add cached ProvidedServiceInstance, serviceName=" + instance.getServiceName()
					+ ", providerId=" + instance.getProviderId() + ", monitor=" + instance.isMonitorEnabled()
					+ ", status=" + instance.getStatus() + ", instance=" + cachedInstance.toString());
			cachedInstance.setServiceInstanceHealth(registryHealth);
			getCacheServiceInstances().add(cachedInstance);
		} finally {
			write.unlock();
		}
	}
	
	/**
	 * Edit the Cached ProvidedServiceInstance when updateService.
	 * 
	 * @param instance
	 * 		the ProvidedServiceInstance.
	 */
	private void editCachedServiceInstance(ProvidedServiceInstance instance) {
		try {
			write.lock();
			CachedProviderServiceInstance cachedinstance = getCachedServiceInstance(instance.getServiceName(),
					instance.getProviderId());
			LOGGER.info("update cached ProvidedServiceInstance, serviceName=" + instance.getServiceName()
					+ ", providerId=" + instance.getProviderId() + ", monitor=" + instance.isMonitorEnabled()
					+ ", status=" + instance.getStatus());
			if (cachedinstance != null) {
				cachedinstance.setMonitorEnabled(instance.isMonitorEnabled());
				cachedinstance.setStatus(instance.getStatus());
				LOGGER.info("instance=" + cachedinstance.toString());
			}
			
		} finally {
			write.unlock();
		}
	}

	/**
	 * Get the Cached ProvidedServiceInstance by serviceName and providerId.
	 * 
	 * It is thread safe.
	 * 
	 * @param serviceName
	 * 		the serviceName
	 * @param providerId
	 * 		the providerId
	 * @return
	 * 		the CachedProviderServiceInstance
	 */
	private CachedProviderServiceInstance getCachedServiceInstance(
			String serviceName, String providerId) {
		try {
			read.lock();
			for (CachedProviderServiceInstance instance : getCacheServiceInstances()) {
				if (instance.equalToProvidedServiceInstance(serviceName,
						providerId)) {
					return instance;
				}
			}
		} finally {
			read.unlock();
		}
		return null;
	}

	/**
	 * Delete the Cache ProvidedServiceInstance by serviceName and providerId.
	 * 
	 * Delete the Cache ProvidedServiceInstance, if it not exits, do nothing.
	 * It is thread safe.
	 * 
	 * @param serviceName
	 * 		the serviceName.
	 * @param providerId
	 * 		the providerId.
	 */
	private void deleteCachedServiceInstance(
			String serviceName, String providerId) {
		try {
			write.lock();
			LOGGER.info("delete cached ProvidedServiceInstance, serviceName=" + serviceName
					+ ", providerId=" + providerId);
			CachedProviderServiceInstance instance = getCachedServiceInstance(serviceName, providerId);
			if(instance != null){
				getCacheServiceInstances().remove(instance);
			}
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
	 * 		the CachedProviderServiceInstance Set.
	 */
	private Set<CachedProviderServiceInstance> getCacheServiceInstances() {
		if (instanceCache == null) {
			synchronized (this) {
				if (instanceCache == null) {
					instanceCache = new HashSet<CachedProviderServiceInstance>();
					initJobTasks();
				}
			}
		}
		return instanceCache;
	}

	/**
	 * initialize the Heartbeat task and Health Check task. It invoked in the
	 * getCacheServiceInstances method which is thread safe, no synchronized
	 * needed.
	 */
	private void initJobTasks() {
		healthJob = Executors
				.newSingleThreadScheduledExecutor(new ThreadFactory() {
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r);
						t.setName("SD API RegistryHealth Check");
						t.setDaemon(true);
						return t;
					}
				});

		heartbeatJob = Executors
				.newSingleThreadScheduledExecutor(new ThreadFactory() {
					public Thread newThread(Runnable r) {
						Thread t = new Thread(r);
						t.setName("SD API Heartbeat");
						t.setDaemon(true);
						return t;
					}
				});
		int rhDelay = Configurations.getInt(
				SD_API_REGISTRY_HEALTH_CHECK_DELAY_PROPERTY,
				SD_API_REGISTRY_HEALTH_CHECK_DELAY_DEFAULT);
		int rhInterval = Configurations.getInt(
				SD_API_REGISTRY_HEALTH_CHECK_INTERVAL_PROPERTY,
				SD_API_REGISTRY_HEALTH_CHECK_INTERVAL_DEFAULT);
		LOGGER.info("Start the SD API RegistryHealth Task scheduler, delay="
				+ rhDelay + ", interval=" + rhInterval);
		healthJob.scheduleAtFixedRate(new HealthCheckTask(), rhDelay,
				rhInterval, TimeUnit.SECONDS);

		int hbDelay = Configurations.getInt(SD_API_HEARTBEAT_DELAY_PROPERTY,
				SD_API_HEARTBEAT_DELAY_DEFAULT);
		int hbInterval = Configurations.getInt(
				SD_API_HEARTBEAT_INTERVAL_PROPERTY,
				SD_API_HEARTBEAT_INTERVAL_DEFAULT);
		LOGGER.info("Start the SD API Heartbeat Task scheduler, delay="
				+ hbDelay + ", interval=" + hbInterval);
		heartbeatJob.scheduleAtFixedRate(new HeartbeatTask(), hbDelay,
				hbInterval, TimeUnit.SECONDS);
	}

	/**
	 * The ServiceInstanceHealth checking task.
	 * 
	 * @author zuxiang
	 * 
	 */
	private class HealthCheckTask implements Runnable {

		@Override
		public void run() {
			read.lock();
			try {
				LOGGER.info("Kickoff the HealthCheckTask thread");
				
				for (CachedProviderServiceInstance ist : getCacheServiceInstances()) {
					if (ist.getServiceInstanceHealth() == null) {
						continue;
					}
					LOGGER.info("Check the Health for service=" + ist.getServiceName() + ", providerId=" + ist.getProviderId());
					boolean ret = ist.getServiceInstanceHealth().isHealthy();
					if (!ret && OperationalStatus.UP.equals(ist.getStatus())) {
						ist.setStatus(OperationalStatus.DOWN);
					} else if(ret && OperationalStatus.DOWN.equals(ist.getStatus())) {
						ist.setStatus(OperationalStatus.UP);
					}
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
	 * @author zuxiang
	 * 
	 */
	private class HeartbeatTask implements Runnable {

		@Override
		public void run() {
			read.lock();
			try {
				LOGGER.info("Kickoff the heartbeat thread");
				List<ServiceInstanceHeartbeat> serviceHBList = new ArrayList<ServiceInstanceHeartbeat>();
				for (CachedProviderServiceInstance cachedInstance : getCacheServiceInstances()) {
					LOGGER.info("Search for, " + cachedInstance.toString()	);
					if (cachedInstance.isMonitorEnabled()
							&& OperationalStatus.UP.equals(cachedInstance
									.getStatus())) {
						ServiceInstanceHeartbeat hb = new ServiceInstanceHeartbeat(
								cachedInstance.getServiceName(),
								cachedInstance.getProviderId());
						serviceHBList.add(hb);
					}
				}
				
				LOGGER.info("Send heartbeat for ServiceInstances, ServiceInstanceNumber="
						+ serviceHBList.size());
				if (serviceHBList.size() == 0) {
					return;
				}

				Map<String, ServiceInstanceHeartbeat> heartbeatMap = new HashMap<String, ServiceInstanceHeartbeat>();
				for (ServiceInstanceHeartbeat instance : serviceHBList) {
					String id = instance.getServiceName() + "-"
							+ instance.getProviderId();
					heartbeatMap.put(id, instance);
				}
				
				Map<String, OperationResult<String>> operateRsult = getServiceDirectoryClient()
						.sendHeartBeat(heartbeatMap);
				if (operateRsult != null) {
					for (Entry<String, OperationResult<String>> entry : operateRsult
							.entrySet()) {
						boolean result = entry.getValue().getResult();
						if (result == false) {
							ServiceInstanceHeartbeat instance = heartbeatMap
									.get(entry.getKey());
							LOGGER.error("Send heartbeat failed, serviceName="
									+ instance.getServiceName()
									+ ", providerId="
									+ instance.getProviderId()
									+ " - "
									+ entry.getValue().getError()
											.getErrorMessage());
						}
					}
				} else {
					LOGGER.error("Get no heartbeat responce from Directory Server");
				}
			} catch (Exception e) {
				LOGGER.error("Send heartbeat failed.", e);
			} finally{
				read.unlock();
			}
		}

	}

	/**
	 * The cached ProviderServiceInstance for the ServiceInstanceHealth and
	 * heartbeat.
	 * 
	 * @author zuxiang
	 * 
	 */
	private static class CachedProviderServiceInstance {
		/**
		 * The ServiceName of ProvidedServiceInstance.
		 */
		private final String serviceName;

		/**
		 * The providerId of ProvidedServiceInstance.
		 */
		private final String providerId;

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
		 * Constructor
		 * 
		 * @param serviceInstance
		 *            the ProvidedServiceInstance.
		 */
		public CachedProviderServiceInstance(
				ProvidedServiceInstance serviceInstance) {
			this.serviceName = serviceInstance.getServiceName();
			this.providerId = serviceInstance.getProviderId();
			this.monitorEnabled = serviceInstance.isMonitorEnabled();
			this.status = serviceInstance.getStatus();
		}

		/**
		 * Get the ServiceInstanceHealth.
		 * 
		 * @param healthCallback
		 *            the ServiceInstanceHealth.
		 */
		public void setServiceInstanceHealth(
				ServiceInstanceHealth healthCallback) {
			this.healthCallback = healthCallback;
		}

		/**
		 * Get the ServiceInstanceHealth.
		 * 
		 * @return the ServiceInstanceHealth callback instance.
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
		 * Get the providerId.
		 * 
		 * @return the providerId.
		 */
		public String getProviderId() {
			return providerId;
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
		 * check is monitor enable in Service Directory.
		 * 
		 * @return true if monitor enabled.
		 */
		public boolean isMonitorEnabled() {
			return monitorEnabled;
		}

		/**
		 * Set the monitor.
		 * 
		 * @param monitor
		 *            the monitor.
		 */
		public void setMonitorEnabled(boolean monitor) {
			this.monitorEnabled = monitor;
		}

		@Override
		public String toString() {
			return "serviceName=" + serviceName + ", providerId=" + providerId + ", status=" + status +", monitor=" + monitorEnabled;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj != null && obj instanceof CachedProviderServiceInstance) {
				CachedProviderServiceInstance instance = (CachedProviderServiceInstance) obj;
				return (instance.serviceName.equals(serviceName) && instance.providerId
						.equals(providerId));
			}
			return false;
		}

		@Override
		public int hashCode() {
			int result = providerId != null ? providerId.hashCode() : 0;
			result = 31 * result + serviceName != null ? serviceName.hashCode()
					: 0;
			return result;
		}

		/**
		 * check equal to the ProvidedServiceInstance.
		 * 
		 * @param serviceInstance
		 *            the ProvidedServiceInstance.
		 * @return true if the serviceName and instanceId is the same.
		 */
		public boolean equalToProvidedServiceInstance(String serviceName,
				String providerId) {
			return (this.serviceName.equals(serviceName) && this.providerId
					.equals(providerId));
		}
	}

}
