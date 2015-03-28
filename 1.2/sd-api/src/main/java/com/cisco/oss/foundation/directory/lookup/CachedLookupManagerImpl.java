package com.cisco.oss.foundation.directory.lookup;

import com.cisco.oss.foundation.directory.lookup.CachedDirectoryLookupService;
import com.cisco.oss.foundation.directory.lookup.LookupManagerImpl;

/**
 * Created by alex on 3/25/15.
 */
public class CachedLookupManagerImpl extends LookupManagerImpl {

    public CachedLookupManagerImpl(CachedDirectoryLookupService service) {
        super(service);
        service.start();
    }

}
