package com.cisco.oss.foundation.directory.impl;

/**
 * Created by alex on 3/25/15.
 */
public class CachedLookupManagerImpl extends LookupManagerImpl {

    public CachedLookupManagerImpl(CachedDirectoryLookupService service) {
        super(service);
        service.start();
    }

}
