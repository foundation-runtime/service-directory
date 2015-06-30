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
 * This class allows ServiceDirectory to be re-initialized with different configuration
 */
public class ConfigurableServiceDirectoryManagerFactory implements ServiceDirectoryManagerFactory {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConfigurableServiceDirectoryManagerFactory.class);

    /*
     * ConfigurableServiceDirectoryManagerFactory Constructor by using SD Config
     * The constructor is protected by private, so that only
     * builder can call it
     */
    public ConfigurableServiceDirectoryManagerFactory(ServiceDirectoryConfig config) {
        this.config = config;
        client = decideClient();
        if (this.config.isCacheEnabled()) {
            this.lookUpService = new CachedDirectoryLookupService(getDirectoryServiceClient());
        } else {
            this.lookUpService = new DirectoryLookupService(getDirectoryServiceClient());
        }
        if (this.config.isHeartBeatEnabled()) {
            this.registerService = new HeartbeatDirectoryRegistrationService(getDirectoryServiceClient());
        } else {
            this.registerService = new DirectoryRegistrationService(getDirectoryServiceClient());
        }

    }

    private final List<LookupManager> lookupManagerReferences = new ArrayList<>();
    private final List<RegistrationManager> registrationManagerReferences = new ArrayList<>();

    private final CloseListener managerCloseListener = new CloseListener() {
        @Override
        public void fireServiceClose(ServiceDirectoryService service) {
            service.stop();
        }

        @Override
        public void onManagerClose(AbstractServiceDirectoryManager manager) {
            if (manager instanceof LookupManager) {
                synchronized (lookupManagerReferences) {
                    handleManager(lookupManagerReferences, manager);
                }
            } else if (manager instanceof RegistrationManager) {
                synchronized (registrationManagerReferences) {
                    handleManager(registrationManagerReferences, manager);
                }
            } else {
                throw new IllegalStateException("Unknown manager " + manager);
            }
        }

        private <T extends AutoCloseable> void handleManager(final List<T> mangerList, AbstractServiceDirectoryManager manager){
            if (mangerList.contains(manager)) {
                mangerList.remove(manager);
                manager.stop();
            }
            if (mangerList.size() == 0) {
                // when all manager closed, fire service close
                fireServiceClose(manager.getService());
            }

        }

    };

    private final DirectoryServiceClient client;
    private final ServiceDirectoryConfig config;
    private final DirectoryLookupService lookUpService;
    private final DirectoryRegistrationService registerService;

    // -----------------------
    // DirectoryServiceClient
    // -----------------------

    /**
     * restful (http) client. it's not state, so that we can keep it singleton
     */
    private static final DirectoryServiceClient restfulClient = new DirectoryServiceRestfulClient();

    /**
     * dummy client, it's not state, so that we can keep it singleton
     */
    private static final DirectoryServiceClient dummyClient = new DirectoryServiceDummyClient();


    /**
     * provided client
     */
    private static final AtomicReference<DirectoryServiceClientProvider> clientProvider =
            new AtomicReference<>();

    public static void setClientProvider(DirectoryServiceClientProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("DirectoryServiceClientProvider can't be null");
        }
        clientProvider.set(provider);
    }


    @Override
    public DirectoryServiceClient getDirectoryServiceClient() {
        return client;
    }

    /*
      use in constructor to decided which client should use by configurtion.
     */
    private DirectoryServiceClient decideClient(){
        DirectoryServiceClient client;
        switch (config.getClientType()) {
            case RESTFUL:
                client = restfulClient;
                break;
            case DUMMY:
                client = dummyClient;
                break;
            case IN_MEMORY:
                client = new DirectoryServiceInMemoryClient();
                break;
            case PROVIDED:
                DirectoryServiceClientProvider provider = clientProvider.get();
                if (provider != null) {
                    client = provider.getClient();
                } else {
                    throw new IllegalStateException("No DirectoryServiceClientProvider is set up for Client Type PROVIDED");
                }
                break;
            default:
                //don't support other client type now.
                throw new IllegalStateException("UNKNOWN Client Type " + config.getClientType());
        }
        return client;
    }

    DirectoryLookupService getLookupService() {
        return this.lookUpService;
    }

    DirectoryRegistrationService getRegistrationService() {
        return this.registerService;
    }

    public LookupManager getLookupManager() throws ServiceException {
        LookupManagerImpl lookupMgr;
        if (config.isCacheEnabled()) {
            lookupMgr = new CachedLookupManagerImpl((CachedDirectoryLookupService) getLookupService());
        } else {
            lookupMgr = new LookupManagerImpl(getLookupService());
        }
        lookupMgr.setCloseListener(managerCloseListener);
        lookupManagerReferences.add(lookupMgr);
        return lookupMgr;
    }

    public RegistrationManager getRegistrationManager() throws ServiceException {
        RegistrationManagerImpl regMgr;
        if (config.isHeartBeatEnabled()) {
            regMgr = new HeartbeatRegistrationManagerImpl((HeartbeatDirectoryRegistrationService) getRegistrationService());
        } else {
            regMgr =  new RegistrationManagerImpl(getRegistrationService());
        }
        regMgr.setCloseListener(managerCloseListener);
        registrationManagerReferences.add(regMgr);
        return regMgr;
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
        for (RegistrationManager register : new ArrayList<>(registrationManagerReferences))
        {
            register.close();
        }
        getLookupService().stop();
        getRegistrationService().stop();
    }
}
