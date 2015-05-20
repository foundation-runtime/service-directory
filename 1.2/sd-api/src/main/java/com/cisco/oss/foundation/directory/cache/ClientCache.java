package com.cisco.oss.foundation.directory.cache;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Service Directory Client Cache
 */
public class ClientCache<T> {

    public ClientCache(T data) {
        this.data = new AtomicReference<>();
        this.data.set(data);
    }

    private final AtomicReference<T> data;

    public T getData(){
        return data.get();
    }

    public void setData(T value){
        data.set(value);
    }

}
