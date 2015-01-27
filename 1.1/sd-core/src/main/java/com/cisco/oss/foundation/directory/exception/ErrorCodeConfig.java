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

import java.util.Locale;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The locale-specific ResourceBundle of the error message for the error code.
 *
 * @author zuxiang
 *
 */
public class ErrorCodeConfig {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ErrorCodeConfig.class);

    /**
     * the default error message string value.
     */
    private static final String DEFAULT_STRING_VALUE = "";

    /**
     * the error code list file.
     */
    private static final String ERRORCODE_FILE = "errorcode";

    /**
     * The local ResourceBundle.
     */
    private static ResourceBundle resourceBundle = null;

    /**
     * Get the String value by the property name. When the property name not
     * exits, get exception, it always return the default String value
     * DEFAULT_STRING_VALUE.
     *
     * @param name
     *            The property name
     * @return The String value
     */
    public static String getStringProperty(String name) {
        try {
            if (resourceBundle == null) {
                resourceBundle = ResourceBundle.getBundle(ERRORCODE_FILE,
                        Locale.getDefault(),
                        ErrorCodeConfig.class.getClassLoader());
            }
            String tagValue = resourceBundle.getString(name);
            if (tagValue == null || tagValue.isEmpty()) {
                return DEFAULT_STRING_VALUE;
            }
            return tagValue.trim();
        } catch (Exception ex) {
            LOGGER.error(
                    "Get exception in ErrorCodeConfig.getStringProperty, propertyName="
                            + name + ".", ex);
            return DEFAULT_STRING_VALUE;
        }
    }

}
