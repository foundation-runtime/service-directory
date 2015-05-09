package com.cisco.oss.foundation.directory;

import com.cisco.oss.foundation.directory.entity.InstanceChange;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;

import static com.cisco.oss.foundation.directory.entity.InstanceChange.*;

/**
 * Listener for Service Instance changes
 *
 * @see InstanceChange
 * @see InstanceChange.ChangeType
 */
public interface ServiceInstanceChangeListener {
   /**
    * called when service instance change is occurred
    *
    * @since 1.2
    * @param type URL, status, metadata
    * @param change the changes
    * @throws Exception
    *
    */
   void onChange(ChangeType type, InstanceChange<ServiceInstance> change) throws Exception;
}
