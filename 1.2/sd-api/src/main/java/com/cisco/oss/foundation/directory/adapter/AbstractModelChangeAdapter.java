package com.cisco.oss.foundation.directory.adapter;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.impl.InstanceChangeListener;

public abstract class AbstractModelChangeAdapter<T> implements InstanceChangeListener<ModelServiceInstance> {
    private final T adapter;
    public AbstractModelChangeAdapter(T a){
        this.adapter=a;
    }
    public T getAdapter(){
        return adapter;
    }
}
