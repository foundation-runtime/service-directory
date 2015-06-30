package com.cisco.oss.foundation.directory.impl;

/**
 * The listener is used as a broker/adapter.
 * Service, which is shared among those managers, can be stopped when all managers are closed.
 */
public interface CloseListener {

    /**
     * Stop the service
     * @param service
     *            the ServiceDirectoryService
     */
    void fireServiceClose(ServiceDirectoryService service);
    
    /**
     * Close the manager
     * @param manager
     *           the AbstractServiceDirectoryManger
     */
    void onManagerClose(AbstractServiceDirectoryManager manager);
}

