package com.cisco.oss.foundation.directory.adapter;


import com.cisco.oss.foundation.directory.ServiceInstanceChangeListener;
import com.cisco.oss.foundation.directory.entity.InstanceChange;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
/**
 *  Adapter to bridge the 1.2 service instance changes to the 1.1 notifications.
 */
public class ServiceInstanceChangeListenerAdapter extends AbstractModelChangeAdapter<ServiceInstanceChangeListener> {

	 /**
     * Constructor.
     *
     * @param listener
     *            the ServiceInstanceChangeListener object
     */
    public ServiceInstanceChangeListenerAdapter(ServiceInstanceChangeListener listener) {
        super(listener);
    }

    @Override
    public void onChange(InstanceChange.ChangeType type, InstanceChange<ModelServiceInstance> change) throws Exception {
        getAdapter().onChange(type, InstanceChange.toServiceInstanceChange(change));
    }
}



