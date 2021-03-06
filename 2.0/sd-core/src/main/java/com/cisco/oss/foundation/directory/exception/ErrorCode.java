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
     * OK. no problems.
     * Message: OK.
     */
    OK("200.1"),

    // Service Instance format validate error.
    /**
     * Service Instance length too long when persistence to zookeeper.
     * Message: ServiceInstance length after serialization is too long, it should not exceed {0}, current length is {1}.
     */
    SERVICE_INSTANCE_LENGTH_TOOLONG("400.1"),

    /**
     * Service Instance name string format error.
     * Message: ServiceInstance name format is wrong. It must start with character or number, other parts can contain character, number, '_', '-', ':' or '.'.
     */
    SERVICE_INSTANCE_NAME_FORMAT_ERROR("400.2"),

    /**
     * Service Instance uri format error.
     * Message: ServiceInstance uri format is wrong. It must be a validate URI.
     */
    SERVICE_INSTANCE_URI_FORMAT_ERROR("400.3"),

    /**
     * Service Instance instanceId format error.
     * Message: ServiceInstance instanceId format is wrong. It must start with character or number, other parts can contain character, number, '_', '-' or '.'.
     */
    SERVICE_INSTANCE_ID_FORMAT_ERROR("400.4"),

    /**
     * Service Instance Address format error.
     * Message: ServiceInstance address format is wrong. It must start with character or number, other parts can contain character, number, '_', '-' or '.'.
     */
    SERVICE_INSTANCE_ADDRESS_FORMAT_ERROR("400.5"),

    /**
     * Service Instance port format error.
     * Message: ServiceInstance port format is wrong. It must be integer which is not bigger than 65535 and less than 1.
     */
    SERVICE_INSTANCE_PORT_FORMAT_ERROR("400.6"),

    /**
     * Service Instance meta key format error.
     * Message: ServiceInstance metadata key name format is wrong. It must start with character or number, other parts can contain character, number, '_', '-' or '.'.
     */
    SERVICE_INSTANCE_METAKEY_FORMAT_ERROR("400.7"),

    /**
     * The ContainQueryCriterion and NotContainQueryCriterion doesn't support
     * queryInstancesByKey.
     * Message: ServiceInstanceQuery has illegal QueryCriteria. Query Instances by MetadataKey doesn't support
     * ContainQueryCriterion and NotContainQueryCriterion.
     */
    QUERY_CRITERION_ILLEGAL_IN_QUERY("400.8"),

    /**
     * Directory Authentication failed when Create Connection or authentication.
     * Message: Directory Authentication failed.
     */
    AUTHENT_FAILED("400.9"),

    /**
     * The Client Session expired of the Directory request, or when reopen the exists session
     * Message: The Client Session expired.
     */
    SESSION_EXPIRED("400.10"),

    /**
     * The Connection to the Server lost.
     * Message: Connection to Directory Server loss.
     */
    CONNECTION_LOSS("400.11"),

    /**
     * Session password is invalid when reopen the session with exists sessionId.
     * Message: Session password is wrong.
     */
    SESSION_PASSWORD_INVALID("400.12"),

    /**
     * Authorization failed against the user ACL.
     * Message: Not authorized.
     */
    AUTHOR_FAILED("400.13"),

    /**
     * The client is closed.
     * Message: Client closed.
     */
    CLIENT_CLOSED("400.14"),

    /**
     * The protocol unknown in the Directory request.
     * Message: Unknown protocol of the Directory request.
     */
    UNEXPECTED_PROTOCOL("400.15"),

    /**
     * IP Format is wrong.
     * Message: IP format is wrong.
     */
    IP_FORMAT_ERROR("400.16"),

    /**
     * User already exists.
     * Message: User already exists.
     */
    USER_ALREADY_EXIST("400.17"),

    /**
     * User doesn't exist.
     * Message: User doesn't exist.
     */
    USER_NOT_EXIST("400.18"),

    /**
     * Session already exists.
     * Message: Session already exists.
     */
    SESSION_ALREADY_EXIST("400.19"),

    /**
     * Session desn't exist.
     * Message: Session doesn't exist.
     */
    SESSION_NOT_EXIST("400.20"),

    /**
     * Invalid Protocol.
     * Message: Invalid Protocol.
     */
    INVALID_PROTOCOL("400.21"),

    /**
     * SD API ping timeout.
     * Message: SD API Ping timeout.
     */
    PING_TIMEOUT("400.22"),

    // Service Instance operation error.
    /**
     * Service Instance not exists.
     * Message: The ServiceInstance doesn't exist.
     */
    SERVICE_INSTANCE_NOT_EXIST("404.1"),

    /**
     * Service not exists.
     * Message: The Service doesn't exits.
     */
    SERVICE_NOT_EXIST("404.2"),

    /**
     * Service Instance metadata key not exists.
     * Message: The MetadataKey doesn't exist.
     */
    METADATA_KEY_NOT_EXIST("404.3"),

    /**
     * Service Instance already exists.
     * Message: The ServiceInstance already exists.
     */
    SERVICE_INSTANCE_ALREADY_EXIST("409.1"),

    /**
     * Service already exists.
     * Message: The Service already exists.
     */
    SERVICE_ALREADY_EXIST("409.2"),

    /**
     * metadata key already exists.
     * Message: The MetadataKey already exists.
     */
    METADATA_KEY_ALREADY_EXIST("409.3"),

    /**
     * Update the Object locked by other thread.
     * Message: Update the Object locked by other thread.
     */
    CACHE_CONCURRENT_ERROR("409.4"),

    /**
     * General error that can not category.
     * Message: General error: {0}
     */
    GENERAL_ERROR("500.1"),

    /**
     * Service is not empty when delete.
     * Message: Cannot delete the Service is not empty.
     */
    SERVICE_NOT_EMPTY_IN_DELETE("500.2"),

    /**
     * Service Instance is not empty when delete.
     * Message: Cannot delete the ServiceInstance is not empty.
     */
    SERVICE_INSTANCE_NOT_EMPTY_IN_DELETE("500.3"),

    /**
     * Metadata key is not empty when delete.
     * Message: Cannot delete the MetadatKey is not empty.
     */
    METADATA_KEY_NOT_EMPTY_IN_DELETE("500.4"),

    /**
     * Update the monitorEnabled field error in ProvidedServiceInstance.
     * Message: Cannot update the monitorEnabled fields in ProvidedServiceInstance.
     */
    UPDATE_MONITOR_ENABLED_ERROR("500.7"),

    /**
     * The Directory API doesn't own the updated moitorEnabled ProvidedServiceInstance.
     * Message: The Directory API doesn't own the updated monitorEnabled ProvidedServiceInstance.
     */
    ILLEGAL_SERVICE_INSTANCE_OWNER_ERROR("500.8"),

    /**
     * Invoke the ServiceDirectory while it shutdown.
     * Message: The ServiceDirectory already shutdown.
     */
    SERVICE_DIRECTORY_IS_SHUTDOWN("500.10"),

    /**
     * ServiceInstanceHealth only for the monitorEnabled ProvidedServiceInstance.
     * Message: Cannot register the monitor disabled ProvidedServiceInstance with ServiceInstanceHealth.
     */
    SERVICE_INSTANCE_HEALTH_ERROR("500.9"),

    /**
     * Invoke the RegistrationManager or LookupManager of the closed ServiceDirectoryManagerFactory.
     * Message: The ServiceDirectoryManagerFactory already closed.
     */
    SERVICE_DIRECTORY_MANAGER_FACTORY_CLOSED("500.11"),



    // Directory Service Client error in Directory API.
    /**
     * The request submitted the the DirectoryServiceClient interrupted.
     * Message: The directory request interrupted.
     */
    REQUEST_INTERUPTED("500.12"),

    /**
     * The remote directory server get error. The directory server client of
     * directory api get a unrecognized error in HTTP Client.
     * Message: The remote Directory Server error: {0}
     */
    REMOTE_DIRECTORY_SERVER_ERROR("500.5"),

    /**
     * HTTP Client error in directory server client of directory api.
     * Message: The HttpClient error: {0}
     */
    HTTP_CLIENT_ERROR("500.6");

    /**
     * The exception code string.
     */
    private String code;

    /**
     * Constructor.
     *
     * @param code
     *            the exception code.
     */
    private ErrorCode(String code) {
        this.code = code;
    }

    /**
     * Get the locale specified error message String.
     *
     * @param code
     *            the Exception code string.
     * @return the error message String.
     */
    public static String getMessage(String code) {
        for (ErrorCode s : ErrorCode.values()) {
            if (s.getCode() == code) {
                return ErrorCodeConfig.getStringProperty(s.getCode());
            }
        }
        return null;
    }

    /**
     * Get the locale specified error message String.
     *
     * @return the locale specified error message String.
     */
    public String getMessage() {
        return ErrorCodeConfig.getStringProperty(this.getCode());
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
