/**
 * This Package is created for support backward compatibility of higher version of SD server to SD 1.1 client
 *
 * The internal implementation is based that, we use JSON to serialize/deserialize Objects between client and server.
 *
 * client 1.1  -- JSON(string) -- http -- JSON(string) -- server 1.2
 *
 * The 1.2 server will try to parse/generate different JSON String according to the judgement the request from 1.1
 * or 1.2 client.
 *
 * NOTE: The classes in this package might contain bugs in their methods, but the code will remain unchanged and stick
 * to the specified version intentionally.
 *
 */
package com.cisco.oss.foundation.directory.entity.compatible;