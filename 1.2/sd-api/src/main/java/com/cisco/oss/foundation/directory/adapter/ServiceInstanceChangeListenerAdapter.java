package com.cisco.oss.foundation.directory.adapter;


import com.cisco.oss.foundation.directory.ServiceInstanceChangeListener;
import com.cisco.oss.foundation.directory.entity.InstanceChange;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;

public class ServiceInstanceChangeListenerAdapter extends AbstractModelChangeAdapter<ServiceInstanceChangeListener> {

    public ServiceInstanceChangeListenerAdapter(ServiceInstanceChangeListener listener) {
        super(listener);
    }

    @Override
    public void onChange(InstanceChange.ChangeType type, InstanceChange<ModelServiceInstance> change) throws Exception {
        getAdapter().onChange(type, InstanceChange.toServiceInstanceChange(change));
    }
}



