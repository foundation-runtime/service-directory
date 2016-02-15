/**
 * Copyright 2014 Cisco Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cisco.oss.foundation.directory.exception;

/**
 * The Error Code enum list.
 *
 *
 */
public enum ErrorCode {
    /**
     * OK. no problems. Message: OK.
     */
    OK("200.1","OK."),

    // Service Instance format validate error.
    /**
     * Service Instance length too long when persisted to zookeeper. Message:
     * ServiceInstance length after serialization is too long, it should not
     * exceed {0}, current length is {1}.
     */
    SERVICE_INSTANCE_LENGTH_TOOLONG("400.1",
            "Serialized ServiceInstance size is too big, it should not exceed %s. Current size is %s."),

    /**
     * Service Instance name string format error. Message: ServiceInstance name
     * format is wrong. It must start with character or number, other parts can
     * contain character, number, '_', '-', ':' or '.'.
     */
    SERVICE_INSTANCE_NAME_FORMAT_ERROR("400.2",
            "Wrong format for the service name. The service name must start with a character or a number and contain only character, number, '_', '-', ':' or '.'."),

    /**
     * Service Instance uri format error. Message: ServiceInstance uri format is
     * wrong. It must be a valid URI.
     */
    SERVICE_INSTANCE_URI_FORMAT_ERROR("400.3",
            "Wrong format for the service uri. The service uri must be a valid URI."),

    /**
     * Service Instance instanceId format error. Message: ServiceInstance
     * instanceId format is wrong. It must start with character or number, other
     * parts can contain character, number, '_', '-' or '.'.
     */
    SERVICE_INSTANCE_ID_FORMAT_ERROR("400.4",
            "Wrong format for the service providerId. The service providerId must be of format 'ip-port'."),

    /**
     * Service Instance Address format error. Message: ServiceInstance address
     * format is wrong. It must start with character or number, other parts can
     * contain character, number, '_', '-' or '.'.
     */
    SERVICE_INSTANCE_ADDRESS_FORMAT_ERROR("400.5",
            "Wrong format for the service address. The service address must be a valid IP address or host name."),

    /**
     * Service Instance port format error. Message: ServiceInstance port format
     * is wrong. It must be integer which is not bigger than 65535 and less than
     * 1.
     */
    SERVICE_INSTANCE_PORT_FORMAT_ERROR("400.6",
            "Wrong format for the service port. The service port must be an unsigned integer less than 65536."),

    /**
     * Service Instance meta key format error. Message: ServiceInstance metadata
     * key name format is wrong. It must start with character or number, other
     * parts can contain character, number, '_', '-' or '.'.
     */
    SERVICE_INSTANCE_METAKEY_FORMAT_ERROR("400.7",
            "Wrong format for the metadata key [%s]. The service metadata key must start with a character or a number, and contain only character, number, '_', '-' or '.'."),

    /**
     * The ContainQueryCriterion and NotContainQueryCriterion are not supported
     * in queryInstancesByMetadataKey. Message: ServiceInstanceQuery has illegal
     * QueryCriteria. Querying Instances by MetadataKey doesn't support
     * ContainQueryCriterion and NotContainQueryCriterion.
     */
    QUERY_CRITERION_ILLEGAL_IN_QUERY("400.8",
            "ServiceInstanceQuery contains illegal QueryCriteria. ContainQueryCriterion and NotContainQueryCriterion are not supported in queryInstancesByKey."),
    
    /**
     * Service Directory argument {0} is null.
     */
    SERVICE_DIRECTORY_NULL_ARGUMENT_ERROR("400.9","The %s is null."),


    // Service Instance operation error.
    /**
     * Service Instance does not exist. Message: The Service Instance does not exist.
     */
    SERVICE_INSTANCE_DOES_NOT_EXIST("404.1","The Service Instance does not exist"),

    /**
     * Service does not exist. Message: The Service does not exist.
     */
    SERVICE_DOES_NOT_EXIST("404.2","The Service '%s' does not exist."),

    /**
     * Service Instance metadata key not exists. Message: The MetadataKey
     * doesn't exist.
     */
    METADATA_KEY_NOT_EXIST("404.3","The MetadataKey does not exist."),

    /**
     * Service Instance already exists. Message: The ServiceInstance already
     * exists.
     */
    SERVICE_INSTANCE_ALREADY_EXIST("409.1","The ServiceInstance already exists."),

    /**
     * Service already exists. Message: The Service already exists.
     */
    SERVICE_ALREADY_EXIST("409.2","The Service already exists."),

    /**
     * metadata key already exists. Message: The MetadataKey already exists.
     */
    METADATA_KEY_ALREADY_EXIST("409.3","The MetadataKey already exists."),

    /**
     * General error that can not be categorized. Message: General error: {0}
     */
    GENERAL_ERROR("500.1","General error: %s"),

    /**
     * Service is not empty when being deleted. Message: Cannot delete the Service that is
     * not empty.
     */
    SERVICE_NOT_EMPTY_IN_DELETE("500.2",
            "Cannot delete the Service since the Service ZNode has children."),

    /**
     * Service Instance is not empty when being deleted. Message: Cannot delete the
     * ServiceInstance that is not empty.
     */
    SERVICE_INSTANCE_NOT_EMPTY_IN_DELETE("500.3",
            "Cannot delete the ServiceInstance since the ServiceInstance ZNode has children."),

    /**
     * Metadata key is not empty when being deleted. Message: Cannot delete the
     * MetadatKey that is not empty.
     */
    METADATA_KEY_NOT_EMPTY_IN_DELETE("500.4",
            "Cannot delete the MetadataKey since the MetadataKey ZNode has children."),
    // Directory Service Client error in Directory API.
    /**
     * The remote directory server get error. The directory server client of
     * directory api get a unrecognized error in HTTP Client. Message: The
     * remote Directory Server error: {0}
     */
    REMOTE_DIRECTORY_SERVER_ERROR("500.5",
            "The remote Directory Server error: %s "),

    /**
     * HTTP Client error in directory server client of directory api. Message:
     * The HttpClient error: {0}
     */
    HTTP_CLIENT_ERROR("500.6","The HttpClient error: %s "),

    /**
     * Update the monitorEnabled field error in ProvidedServiceInstance.
     * Message: Cannot update the monitorEnabled fields in
     * ProvidedServiceInstance.
     */
    UPDATE_MONITOR_ENABLED_ERROR("500.7",
            "The monitorEnabled field in ProvidedServiceInstance is not allowed to be updated."),

    /**
     * The Directory API doesn't own the moitorEnabled
     * ProvidedServiceInstance and can not update it. Message: The Directory API doesn't
     * own the ProvidedServiceInstance.
     */
    ILLEGAL_SERVICE_INSTANCE_OWNER_ERROR("500.8",
            "The Directory API does not own the ProvidedServiceInstance."),

    /**
     * ServiceInstanceHealth only for the monitorEnabled
     * ProvidedServiceInstance. Message: Cannot register the monitor disabled
     * ProvidedServiceInstance with ServiceInstanceHealth.
     */
    SERVICE_INSTANCE_HEALTH_ERROR("500.9",
            "Can not register the monitor disabled ProvidedServiceInstance with ServiceInstanceHealth."),

    /**
     * Invoke the ServiceDirectory while it shutdown. Message: The
     * ServiceDirectory already shut down.
     */
    SERVICE_DIRECTORY_IS_SHUTDOWN("500.10",
            "The ServiceDirectory already shut down."),

    /**
     * Invoke the RegistrationManager or LookupManager of the closed
     * ServiceDirectoryManagerFactory. Message: The
     * ServiceDirectoryManagerFactory already closed.
     */
    SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED("500.11",
            "The ServiceDirectoryManagerFactory already closed."),


    /**
     * Error when try to close a manager
     */
    SERVICE_DIRECTORY_MANAGER_CLOSE_ERROR("500.12","Error when try to close %s."),

    /**
     * Error when unregister a not-exist NotificationHandler
     */
    NOTIFICATION_HANDLER_DOES_NOT_EXIST("500.13",
            "NotificationHandler not exist, It may has been removed or has not been registered before."),

    /**
     * Error when unregister a not-exist ServiceInstanceChangeListener
     */
    SERVICE_INSTANCE_LISTENER_DOES_NOT_EXIST("500.14",
            "ServiceInstanceChangeListener not exist, It may has been removed or has not been registered before."),
    
    /**
     * Error when creating a secure connection to the server
     */
    SERVICE_DIRECTORY_SSLRUNTIMEEXCEPTION("500.15",
            "Unable to create a secure connection to the server. %s");
 
    /**
     * The Error code
     */
    private final String code;

    /**
     * The Error Code Message template
     */
    private final String msgTemplate;

    /**
     * Constructor.
     *
     * @param code
     *            the exception code.
     */
    ErrorCode(String code, String template) {
        this.code = code;
        this.msgTemplate = template;
    }

    /**
     * Get the locale specified error message String.
     *
     * @param code
     *            the Exception code string.
     * @return the error message String.
     */
    public static String getMessageTemplate(String code) {
        for (ErrorCode s : ErrorCode.values()) {
            if (s.getCode().equals(code)) {
                return s.getMessageTemplate();
            }
        }
        throw new IllegalArgumentException("Unknown ErrorCode : "+ code);
    }

    public static ErrorCode toErrorCode(String code){
        for (ErrorCode s : ErrorCode.values()) {
            if (s.getCode().equals(code)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown ErrorCode : "+ code);
    }

    /**
     * Get the locale specified error message String.
     *
     * @return the locale specified error message String.
     */
    public String getMessageTemplate() {
        return msgTemplate;
    }

    /**
     * Get the exception code.
     *
     * @return the code.
     */
    public String getCode() {
        return code;
    }
}
