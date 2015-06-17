package com.cisco.oss.foundation.directory.cache;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Service Directory Client Cache
 */
public class ClientCache<T> {
 
    /**
     * Constructor.
     *
     * @param data
     *            the data object
     */
    public ClientCache(T data) {
        this.data = new AtomicReference<>();
        this.data.set(data);
    }

    private final AtomicReference<T> data;


	/**
	 * Get the client cache.
	 * 
	 * @returns   the data cache
	 *            
	 */  
    public T getData(){
        return data.get();
    }

	/**
	 * Set the client cache.
	 * 
	 * @param value
	 *            the value to set
	 */
    public void setData(T value){
        data.set(value);
    }

}
