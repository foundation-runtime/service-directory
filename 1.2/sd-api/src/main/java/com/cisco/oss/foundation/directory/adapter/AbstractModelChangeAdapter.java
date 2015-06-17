package com.cisco.oss.foundation.directory.adapter;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.impl.InstanceChangeListener;
/**
 *  Abstract adapter to bridge the 1.2 service instance changes to the 1.1 notifications.
 */
public abstract class AbstractModelChangeAdapter<T> implements InstanceChangeListener<ModelServiceInstance> {
    private final T adapter;
   
    /**
     * Constructor.
     *
     * @param a
     *            the adapter object
     */
    public AbstractModelChangeAdapter(T a){
        this.adapter=a;
    }
   
    /**
     * Get the adapter object.
     *
     * @return the adapter.
     */
    public T getAdapter(){
        return adapter;
    }
}
