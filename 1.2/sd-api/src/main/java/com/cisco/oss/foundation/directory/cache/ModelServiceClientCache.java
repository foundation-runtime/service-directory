package com.cisco.oss.foundation.directory.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.entity.InstanceChange;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.impl.InstanceChangeListener;

/**
 * The client side cache for service instance. which work as a listener of service changes.
 * the content will be updated automatically when server side is changed
 */
public class ModelServiceClientCache extends ClientCache<ModelService> implements InstanceChangeListener<ModelServiceInstance> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelServiceClientCache.class);

    public ModelServiceClientCache(List<InstanceChangeListener<ModelServiceInstance>> changeListeners, ModelService data) {
        super(data);
        changeListeners.add(this);
    }

    @Override
    public void onChange(InstanceChange.ChangeType type, InstanceChange<ModelServiceInstance> change) throws Exception {

        if (type == InstanceChange.ChangeType.Remove) {
            ModelService service = getData();
            if (service != null) {
                List<ModelServiceInstance> removeList = new ArrayList<>();
                for (ModelServiceInstance mInst : service.getServiceInstances()) {
                    if (mInst.getAddress().equals(change.from.getAddress())) {
                        removeList.add(mInst);
                    }
                }
                for (ModelServiceInstance mInst : removeList) {
                    boolean removed = service.getServiceInstances().remove(mInst);
                    if (removed) {
                        LOGGER.debug("{} has been removed from cache", mInst);
                    }
                }
                if (service.getServiceInstances().isEmpty()) { //nothing
                    setDate(null);
                    LOGGER.debug("{} has been removed from cache", service);

                }
            }
        } else if (type == InstanceChange.ChangeType.Create) {
            getData().getServiceInstances().add(change.to);
        }
    }

    public String getServiceName() {
        return getData().getName();
    }

    public List<ModelServiceInstance> getAllModelServiceInstance() {
        return getData() == null ? null : getData().getServiceInstances();
    }

    public void removeFromChangeListenerMap(Map<String, List<InstanceChangeListener<ModelServiceInstance>>> changeListeners) {
        String serviceName = getServiceName();
        List<InstanceChangeListener<ModelServiceInstance>> lList = changeListeners.get(serviceName);
        if (lList != null) {
            boolean removed = lList.remove(this);
            if (removed) {
                LOGGER.debug("Change Listener for service {}  has been removed", serviceName);
            }
        }
    }

}
