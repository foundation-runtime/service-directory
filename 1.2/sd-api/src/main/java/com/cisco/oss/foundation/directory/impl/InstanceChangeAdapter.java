package com.cisco.oss.foundation.directory.impl;

import java.util.Objects;

import com.cisco.oss.foundation.directory.NotificationHandler;
import com.cisco.oss.foundation.directory.ServiceInstanceChangeListener;
import com.cisco.oss.foundation.directory.entity.InstanceChange;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ServiceInstance;

import static com.cisco.oss.foundation.directory.entity.OperationalStatus.UP;

/**
 *  Adapter, bridge the 1.2 changes to 1.1 notification.
 */
public class InstanceChangeAdapter implements ServiceInstanceChangeListener{

    private final NotificationHandler handler;

    public InstanceChangeAdapter(NotificationHandler handler){
        this.handler = handler;
    }

    public NotificationHandler getHandler(){
        return this.handler;
    }

    @Override
    public void onChange(InstanceChange.ChangeType type, InstanceChange<ServiceInstance> change) throws Exception {
        Objects.requireNonNull(type);
        Objects.requireNonNull(change);
        switch (type) {
            case Create:
                handler.serviceInstanceChange(change.to);
                break;
            case Remove:
                handler.serviceInstanceChange(change.from);
                break;
            case Status:
                Objects.requireNonNull(change.to);
                if (change.to.getStatus()== UP){
                    handler.serviceInstanceAvailable(change.to);
                }else{
                    handler.serviceInstanceUnavailable(change.to);
                }
                break;
            case URL:
                handler.serviceInstanceChange(change.to);
                break;
            case META:
                handler.serviceInstanceChange(change.to);
                break;
        }
    }
}
