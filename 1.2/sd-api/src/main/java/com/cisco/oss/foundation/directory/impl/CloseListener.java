package com.cisco.oss.foundation.directory.impl;

/**
 * The listener is used as a broker/adapter when all manager are closed
 * we can stop service which is shared by those managers
 */
public interface CloseListener {

    void fireServiceClose(ServiceDirectoryService service);

    void onManagerClose(AbstractServiceDirectoryManager manager);
}

