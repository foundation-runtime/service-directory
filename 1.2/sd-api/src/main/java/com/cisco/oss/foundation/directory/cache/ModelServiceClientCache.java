package com.cisco.oss.foundation.directory.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.entity.InstanceChange;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.impl.InstanceChangeListener;
import com.cisco.oss.foundation.directory.utils.ServiceInstanceUtils;

/**
 * The client side cache for service instance. which work as a listener of service changes.
 * the content will be updated automatically when server side is changed
 */
public class ModelServiceClientCache extends ClientCache<ModelService> implements InstanceChangeListener<ModelServiceInstance> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelServiceClientCache.class);
    private final String serviceName;

    public ModelServiceClientCache(List<InstanceChangeListener<ModelServiceInstance>> changeListeners, ModelService data) {
        super(data);
        this.serviceName = data.getName();
        changeListeners.add(this);
    }

    @Override
    public void onChange(InstanceChange.ChangeType type, final InstanceChange<ModelServiceInstance> change) throws Exception {

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
                    setData(null);
                    LOGGER.debug("{} has been removed from cache", service);

                }
            }
        } else if (type == InstanceChange.ChangeType.Create) {
            List<ModelServiceInstance> all = getAllModelServiceInstance();
            if (all!=null){
                all.add(ServiceInstanceUtils.copyModelInstFrom(change.to));
            }
        } else {
            List<ModelServiceInstance> all = getAllModelServiceInstance();
            if (all!=null){
                for (ModelServiceInstance instance : all){
                    if (instance.getInstanceId().equals(change.to.getInstanceId())){
                        if (type == InstanceChange.ChangeType.Status){
                            instance.setStatus(change.to.getStatus());
                        }else if (type == InstanceChange.ChangeType.URL){
                            instance.setUri(change.to.getUri());
                        }else if (type == InstanceChange.ChangeType.META){
                            Map<String,String> map = new HashMap<>();
                            map.putAll(change.to.getMetadata());
                            instance.setMetadata(map);
                        }
                        break;
                    }
                }
            }

        }
    }

    public String getServiceName() {
        return serviceName;
    }

    public List<ModelServiceInstance> getAllModelServiceInstance() {
        return getData() == null ? null : getData().getServiceInstances();
    }

    public ModelService getCachedService(){
        return getData();
    }

    public void removeFromChangeListenerMap(Map<String, List<InstanceChangeListener<ModelServiceInstance>>> changeListeners) {
        List<InstanceChangeListener<ModelServiceInstance>> lList = changeListeners.get(serviceName);
        if (lList != null) {
            boolean removed = lList.remove(this);
            if (removed) {
                LOGGER.debug("Change Listener for Client Cache of service {} has been removed", serviceName);
            }
            else{
                LOGGER.debug("Change Listener for client cache of service {} might already has been removed");
            }
        }else{
            LOGGER.debug("No Change Listener find for service {} ");
        }
    }

}
