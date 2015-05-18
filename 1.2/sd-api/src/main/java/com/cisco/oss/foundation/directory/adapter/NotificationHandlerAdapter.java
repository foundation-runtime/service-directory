package com.cisco.oss.foundation.directory.adapter;

import java.util.Objects;

import com.cisco.oss.foundation.directory.NotificationHandler;
import com.cisco.oss.foundation.directory.entity.InstanceChange;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;

import static com.cisco.oss.foundation.directory.entity.OperationalStatus.UP;
import static com.cisco.oss.foundation.directory.utils.ServiceInstanceUtils.toServiceInstance;

/**
 *  Adapter, bridge the 1.2 changes to 1.1 notification.
 */
public class NotificationHandlerAdapter extends AbstractModelChangeAdapter<NotificationHandler> {

    public NotificationHandlerAdapter(NotificationHandler a) {
        super(a);
    }

    @Override
    public void onChange(InstanceChange.ChangeType type, InstanceChange<ModelServiceInstance> change) throws Exception {
        Objects.requireNonNull(type);
        Objects.requireNonNull(change);
        switch (type) {
            case Create:
                getAdapter().serviceInstanceChange(toServiceInstance(change.to));
                break;
            case Remove:
                getAdapter().serviceInstanceChange(toServiceInstance(change.from));
                break;
            case Status:
                Objects.requireNonNull(change.to);
                if (change.to.getStatus()== UP){
                    getAdapter().serviceInstanceAvailable(toServiceInstance(change.to));
                }else{
                    getAdapter().serviceInstanceUnavailable(toServiceInstance(change.to));
                }
                break;
            case URL:
                getAdapter().serviceInstanceChange(toServiceInstance(change.to));
                break;
            case META:
                getAdapter().serviceInstanceChange(toServiceInstance(change.to));
                break;
        }
    }
}
