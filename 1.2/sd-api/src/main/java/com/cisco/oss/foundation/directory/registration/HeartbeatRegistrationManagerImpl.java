package com.cisco.oss.foundation.directory.registration;

/**
 * The heartbeat registration manager implementation.
 */
public class HeartbeatRegistrationManagerImpl extends RegistrationManagerImpl {
    public HeartbeatRegistrationManagerImpl(HeartbeatDirectoryRegistrationService service){
        super(service);
        service.start();
    }
}
