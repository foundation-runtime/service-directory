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
import static com.cisco.oss.foundation.directory.utils.JsonSerializer.serialize;

/**
 * The client side cache for service instance. It also implements an InstanceChangeListener for the service changes.
 * The cache will be updated automatically when there is a server-side change.
 */
public class ModelServiceClientCache extends ClientCache<ModelService> implements InstanceChangeListener<ModelServiceInstance> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelServiceClientCache.class);
    private final String serviceName;

    /**
     * Constructor.
     *
     * @param data
     *            the ModelService data object
     */
    public ModelServiceClientCache(ModelService data) {
        super(data);
        this.serviceName = data.getName();
    }

    @Override
    public void onChange(InstanceChange.ChangeType type, final InstanceChange<ModelServiceInstance> change) throws Exception {

        if (type == InstanceChange.ChangeType.REMOVE) {
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
        } else if (type == InstanceChange.ChangeType.ADD) {
            List<ModelServiceInstance> all = getAllModelServiceInstance();
            if (all!=null){
                //need to check if the instance already exist, because the instance might be
                //already cached by a lookup before the create event.
                boolean exist = false;
                for (ModelServiceInstance instance : all){
                   if (instance.getInstanceId().equals(change.to.getInstanceId())){
                       exist = true;
                       LOGGER.debug("The service instance {} already exists in cache.", change.to);
                       break;
                   }
                }
                if (!exist) {
                    LOGGER.debug("Add newly created service instance {} to cache.", change.to);
                    all.add(ServiceInstanceUtils.copyModelInstFrom(change.to));
                }
            }
        } else {
            List<ModelServiceInstance> all = getAllModelServiceInstance();
            if (all!=null){
                for (ModelServiceInstance instance : all){
                    if (instance.getInstanceId().equals(change.to.getInstanceId())){
                        if (type == InstanceChange.ChangeType.STATUS){
                            LOGGER.debug("Cached service instance {} has change Status from {} to {}",instance,
                                    change.from.getStatus(),
                                    change.to.getStatus());
                            instance.setStatus(change.to.getStatus());
                        }else if (type == InstanceChange.ChangeType.URL){
                            LOGGER.debug("Cached service instance {} has change URL from {} to {}",instance,
                                    change.from.getUri(),
                                    change.to.getUri());
                            instance.setUri(change.to.getUri());
                        }else if (type == InstanceChange.ChangeType.META){
                            Map<String,String> map = new HashMap<>();
                            map.putAll(change.to.getMetadata());
                            LOGGER.debug("Cached service instance {} has change Metadata from {} to {}",instance,
                                    instance.getMetadata(),map);
                            instance.setMetadata(map);
                        }
                        instance.setModifiedTime(change.to.getModifiedTime()); //need to change modified time
                        break;
                    }
                }
            }

        }
        dumpCache();
    }

	/**
	 * Get the service name.
	 * 
	 * @return   the service name
	 *            
	 */  
    public String getServiceName() {
        return serviceName;
    }

	/**
	 * Get the list of ModelServiceInstances.
	 * 
	 * @return   the list of ModelServiceInstances
	 *            
	 */  
    public List<ModelServiceInstance> getAllModelServiceInstance() {
        return getData() == null ? null : getData().getServiceInstances();
    }

	/**
	 * Get the ModelService from cache.
	 * 
	 * @return   the ModelService
	 *            
	 */  
    public ModelService getCachedService(){
        return getData();
    }

    // Set this log to DEBUG to enable Service Cache dump in LookupManager.
    // It will dump the whole ServiceCache to log file when the Logger Changed first time,
    // and every time the Service Cache has new update.
    private static final Logger CacheDumpLogger = LoggerFactory.getLogger("com.cisco.oss.foundation.directory.cache.dump");

    /**
     * Dump the ServiceCache to CacheDumpLogger Logger.
     *
     * @return
     *         true if dump complete.
     */
    private void dumpCache(){
        if (CacheDumpLogger.isDebugEnabled()) {
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("LookupManager dumped Service Cache at: ").append(System.currentTimeMillis()).append("\n");
                sb.append(new String(serialize(getData()))).append("\n");
                CacheDumpLogger.debug(sb.toString());
            } catch (Exception e) {
                LOGGER.warn("Dump Service Cache failed. Set Logger {} to INFO to disable this message.",
                            CacheDumpLogger.getName());
                LOGGER.trace("Dump Service Cache failed. ", e);
            }
        }
    }



}
