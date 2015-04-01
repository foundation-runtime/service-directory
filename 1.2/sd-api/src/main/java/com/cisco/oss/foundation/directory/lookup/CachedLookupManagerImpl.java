package com.cisco.oss.foundation.directory.lookup;

/**
 * The CachedLookupManager implementation .
 */
public class CachedLookupManagerImpl extends LookupManagerImpl {

    public CachedLookupManagerImpl(CachedDirectoryLookupService service) {
        super(service);
        service.start();
    }

}
