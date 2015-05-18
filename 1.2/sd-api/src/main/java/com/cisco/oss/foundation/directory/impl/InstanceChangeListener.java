package com.cisco.oss.foundation.directory.impl;

import com.cisco.oss.foundation.directory.entity.InstanceChange;

/**
 *
 */
public interface InstanceChangeListener<T> {
    void onChange(InstanceChange.ChangeType type,InstanceChange<T> change) throws Exception;
}
