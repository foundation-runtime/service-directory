/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory.exception;

/**
 * It is the unchecked Exception used in Directory API.
 *
 * The LookupManager and RegistrationManager will capture this Exception and
 * convert it to the ServiceException to reminder high Application. So all
 * Exception that cannot handle in ServiceDirectory should extends this
 * exception, it will throw to the high Application.
 *
 * @author zuxiang
 *
 */
public class ServiceRuntimeException extends RuntimeException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The ServiceDirectoryError.
     */
    private ServiceDirectoryError ec;

    /**
     * Constructor.
     *
     * @param ec
     *            the ServiceDirectoryError.
     */
    public ServiceRuntimeException(ServiceDirectoryError ec) {
        super(ec.getErrorMessage());
        this.ec = ec;
    }

    /**
     * Constructor.
     *
     * @param ec
     *            the ServiceDirectoryError.
     * @param message
     *            the error message.
     */
    public ServiceRuntimeException(ServiceDirectoryError ec, String message) {
        super(message);
        this.ec = ec;
    }

    /**
     * Constructor.
     *
     * @param ec
     *            the ServiceDirectoryError.
     * @param ex
     *            the root Exception.
     */
    public ServiceRuntimeException(ServiceDirectoryError ec, Exception ex) {
        super(ec.getErrorMessage(), ex);
        this.ec = ec;
    }

    /**
     * Constructor.
     *
     * @param ec
     *            the ServiceDirectoryError.
     * @param message
     *            the error message.
     * @param ex
     *            the root Exception.
     */
    public ServiceRuntimeException(ServiceDirectoryError ec, String message,
            Exception ex) {
        super(message, ex);
        this.ec = ec;
    }

    /**
     * Get the ServiceDirectoryError.
     *
     * @return the ServiceDirectoryError.
     */
    public ServiceDirectoryError getServiceDirectoryError() {
        return ec;
    }
}
