package com.cisco.oss.foundation.directory.impl;

import com.cisco.oss.foundation.directory.entity.InstanceChange;

/**
 * Interface for the service instance change callback
 * The change types supported: instance available, instance unavailable, status change, 
 * URL change and metadata change.  
 */
public interface InstanceChangeListener<T> {
    void onChange(InstanceChange.ChangeType type,InstanceChange<T> change) throws Exception;
}
