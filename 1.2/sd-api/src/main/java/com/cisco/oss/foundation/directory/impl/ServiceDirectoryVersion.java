package com.cisco.oss.foundation.directory.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Immutable Class to get the version of Service Directory. <p>
 * Version information is loaded from "version.txt" in classpath, the file
 * is created by Maven build.
 *
 * @since 1.2
 */
public final class ServiceDirectoryVersion {

    public static final Logger LOGGER = LoggerFactory.getLogger(ServiceDirectoryVersion.class);
    private static final String version;
    static {
        String ver = "Unknown";
        try {
            InputStream input = ServiceDirectoryVersion.class.getClassLoader()
                    .getResourceAsStream("version.txt");
            if (input != null) {
                Properties prop = new Properties();
                prop.load(input);
                input.close();

                if (prop.containsKey("version")) {
                    ver = prop.getProperty("version");
                }
            }
        } catch (IOException e) {
            LOGGER.error("Fail to get ServiceDirectory API version.", e);
        }
        version = ver;
    }
    public static String getVersion() {
        return version;
    }
}
