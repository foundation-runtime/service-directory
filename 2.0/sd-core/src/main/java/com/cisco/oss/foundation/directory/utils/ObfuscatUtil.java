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
package com.cisco.oss.foundation.directory.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * ObfuscateUtil use to encrypt the password.
 *
 *
 */
public class ObfuscatUtil {

    /**
     * Encrypt the password in PBKDF2 algorithm.
     *
     * @param password
     *         the password to obfuscate.
     * @param salt
     *         the randomly sequence of bits of the user.
     * @return
     *         the obfuscated password byte array.
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static byte[] pBKDF2ObfuscatePassword(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        String algorithm = "PBKDF2WithHmacSHA1";
        int derivedKeyLength = 160;
        int iterations = 20000;
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations,
                derivedKeyLength);
        SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);

        return f.generateSecret(spec).getEncoded();
    }

    /**
     * Compute the the hash value for the String.
     *
     * @param passwd
     *         the password String
     * @return
     *         the Hash digest byte.
     * @throws NoSuchAlgorithmException
     *         the NoSuchAlgorithmException.
     */
    public static byte[] computeHash(String passwd) throws NoSuchAlgorithmException  {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-1");
        md.reset();
        md.update(passwd.getBytes());
        return md.digest();
    }

    /**
     * base64 encode.
     *
     * @param buffer
     *         the byte array.
     * @return
     *         the encoded byte array.
     */
    public static byte[] base64Encode(byte[] buffer) {
        return Base64.encodeBase64(buffer);
    }

    /**
     * base64 decode.
     *
     * @param base64Buffer
     *         the base64 encoded byte array.
     * @return
     *         the decoded byte array.
     */
    public static byte[] base64Decode(byte[] base64Buffer){
        return Base64.decodeBase64(base64Buffer);
    }

    /**
     * Generate a random salt.
     *
     * @return
     *         the random salt.
     * @throws NoSuchAlgorithmException
     *         the NoSuchAlgorithmException.
     */
    public static byte[] generateSalt() throws NoSuchAlgorithmException {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        return salt;
    }
}
