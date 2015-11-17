package com.cisco.oss.foundation.directory.utils;

import java.io.File;

/**
 * The Utility for SEI CERT JAVA Secure Code Standard
 */
public final class SecureUtils {

    private SecureUtils() {
        // private constructor
    }

    public static void validSpecialCharacters(String str) {
        // Allow only alphanumeric and underscore chars in firstName
        // (modify if firstName may also include special characters)
        if (!str.matches("[\\w]*")) {
            // String does not match whitelisted characters
            throw new IllegalArgumentException(str);
        }
    }

    public static void validateNumber(String str) {
        try {
            Integer i = Integer.decode(str);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a number" + str);
        }
    }

    public static String validateSecureFilePath(String name) {
        // Check for path operation names.
        if (name == null || name.equals("") || name.equals(".") || name.equals("..")) {
            throw new IllegalArgumentException();
        }
        // Null bytes
        if (name.indexOf('\0') != -1) {
            throw new IllegalArgumentException();
        }

        // '/' works as a separator on Windows, even though it isn't the
        // platform separator character
        if ('/' != File.separatorChar && name.indexOf('/') != -1) {
            throw new IllegalArgumentException();
        }
        return name;
    }
}
