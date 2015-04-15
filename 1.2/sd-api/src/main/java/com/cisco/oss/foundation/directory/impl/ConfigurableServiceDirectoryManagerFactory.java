package com.cisco.oss.foundation.directory.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.LookupManager;
import com.cisco.oss.foundation.directory.RegistrationManager;
import com.cisco.oss.foundation.directory.ServiceDirectoryManagerFactory;
import com.cisco.oss.foundation.directory.client.DirectoryServiceClient;
import com.cisco.oss.foundation.directory.client.DirectoryServiceClientProvider;
import com.cisco.oss.foundation.directory.client.DirectoryServiceDummyClient;
import com.cisco.oss.foundation.directory.client.DirectoryServiceInMemoryClient;
import com.cisco.oss.foundation.directory.client.DirectoryServiceRestfulClient;
import com.cisco.oss.foundation.directory.exception.ServiceException;
import com.cisco.oss.foundation.directory.lookup.CachedDirectoryLookupService;
import com.cisco.oss.foundation.directory.lookup.CachedLookupManagerImpl;
import com.cisco.oss.foundation.directory.lookup.DirectoryLookupService;
import com.cisco.oss.foundation.directory.lookup.LookupManagerImpl;
import com.cisco.oss.foundation.directory.registration.DirectoryRegistrationService;
import com.cisco.oss.foundation.directory.registration.HeartbeatDirectoryRegistrationService;
import com.cisco.oss.foundation.directory.registration.HeartbeatRegistrationManagerImpl;
import com.cisco.oss.foundation.directory.registration.RegistrationManagerImpl;

/**
 *
 */
public class ConfigurableServiceDirectoryManagerFactory implements ServiceDirectoryManagerFactory {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConfigurableServiceDirectoryManagerFactory.class);

    /*
     * ConfigurableServiceDirectoryManagerFactory Constructor by using SD Config
     * The constructor is protected by private, so that only
     * builder can call it
     */
    public ConfigurableServiceDirectoryManagerFactory(ServiceDirectoryConfig config) {
        this._config = config;
        if (_config.isCacheEnabled()) {
            this._lookUpService = new CachedDirectoryLookupService(getDirectoryServiceClient());
        } else {
            this._lookUpService = new DirectoryLookupService(getDirectoryServiceClient());
        }
        if (_config.isHeartBeatEnabled()) {
            this._registerService = new HeartbeatDirectoryRegistrationService(getDirectoryServiceClient());
        } else {
            this._registerService = new DirectoryRegistrationService(getDirectoryServiceClient());
        }

    }

    private final List<LookupManager> lookupManagerReferences = new ArrayList<>();
    private final List<RegistrationManager> RegistrationManagerReferences = new ArrayList<>();

    private final CloseListener managerCloseListener = new CloseListener() {
        @Override
        public void fireServiceClose(ServiceDirectoryService service) {
            service.stop();
        }

        @Override
        public void onManagerClose(AbstractServiceDirectoryManager manager) {
            if (manager instanceof LookupManager) {
                synchronized (lookupManagerReferences) {
                    if (lookupManagerReferences.contains(manager)) {
                        lookupManagerReferences.remove(manager);
                        manager.stop();
                    }
                    if (lookupManagerReferences.size() == 0) {
                        // when all lookup manager closed, fire service close
                        fireServiceClose(manager.getService());
                    }
                }
            } else if (manager instanceof RegistrationManager) {
                //TODO, handle Registration Mangers
            } else {
                throw new IllegalStateException("Unknown manager " + manager);
            }
        }

    };

    private final ServiceDirectoryConfig _config;
    private final DirectoryLookupService _lookUpService;
    private final DirectoryRegistrationService _registerService;

    // -----------------------
    // DirectoryServiceClient
    // -----------------------

    /**
     * restful (http) client
     */
    private static final DirectoryServiceClient _restfulClient = new DirectoryServiceRestfulClient();

    /**
     * dummy client
     */
    private static final DirectoryServiceClient _dummyClient = new DirectoryServiceDummyClient();

    /**
     * In-Memory Client
     */
    private static final DirectoryServiceClient _inMemoryClient = new DirectoryServiceInMemoryClient();

    /**
     * provided client
     */
    private static final AtomicReference<DirectoryServiceClientProvider> _clientProvider =
            new AtomicReference<>();

    public static void setClientProvider(DirectoryServiceClientProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("DirectoryServiceClientProvider can't be null");
        }
        _clientProvider.set(provider);
    }

    @Override
    public DirectoryServiceClient getDirectoryServiceClient() {
        DirectoryServiceClient client;
        switch (_config.getClientType()) {
            case RESTFUL:
                client = _restfulClient;
                break;
            case DUMMY:
                client = _dummyClient;
                break;
            case IN_MEMORY:
                client = _inMemoryClient;
                break;
            case PROVIDED:
                DirectoryServiceClientProvider provider = _clientProvider.get();
                if (provider != null) {
                    client = provider.getClient();
                } else {
                    throw new IllegalStateException("No DirectoryServiceClientProvider is set up for Client Type PROVIDED");
                }
                break;
            default:
                //don't support other client type now.
                throw new IllegalStateException("UNKNOWN Client Type " + _config.getClientType());
        }
        return client;
    }

    DirectoryLookupService getLookupService() {
        return this._lookUpService;
    }

    DirectoryRegistrationService getRegistrationService() {
        return this._registerService;
    }

    public LookupManager getLookupManager() throws ServiceException {
        if (_config.isCacheEnabled()) {
            //TODO, fix the force conversion
            CachedLookupManagerImpl cachedMgr = new CachedLookupManagerImpl((CachedDirectoryLookupService) getLookupService());
            cachedMgr.setCloseListener(managerCloseListener);
            lookupManagerReferences.add(cachedMgr);
            return cachedMgr;
        } else {
            LookupManagerImpl mgr = new LookupManagerImpl(getLookupService());
            mgr.setCloseListener(managerCloseListener);
            lookupManagerReferences.add(mgr);
            return mgr;
        }
    }

    public RegistrationManager getRegistrationManager() throws ServiceException {

        if (_config.isHeartBeatEnabled()) {
            //TODO, fix the conversion by extract interface
            return new HeartbeatRegistrationManagerImpl((HeartbeatDirectoryRegistrationService) getRegistrationService());
        } else {
            return new RegistrationManagerImpl(getRegistrationService());
        }
    }

    @Override
    public void start() {
        getLookupService().start();
        getRegistrationService().start();
    }

    @Override
    public void stop() {
        // Also need to closed all referenced lookupManagers
        // the referenced list need to be copied to a new ArrayList, because when close()
        // method is called, the fired listener will try to remove the one from the
        // reference list. so that a concurrentModification exception threw out.
        for (LookupManager lookup : new ArrayList<>(lookupManagerReferences)){
            lookup.close();
        }
        for (RegistrationManager register : new ArrayList<>(RegistrationManagerReferences))
        {
            register.close();
        }
        getLookupService().stop();
        getRegistrationService().stop();
    }
}
