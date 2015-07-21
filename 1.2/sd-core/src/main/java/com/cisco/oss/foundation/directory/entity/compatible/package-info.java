/**
 * This Package is created for supporting backward compatibility of higher version of SD server to SD 1.1 client
 *
 * The internal implementation uses JSON to serialize/deserialize Objects between client and server.
 *
 * client 1.1  -- JSON(string) -- http -- JSON(string) -- server 1.2
 *
 * The 1.2 server will try to parse/generate different JSON String for the requests coming from 1.1
 * and 1.2 clients.
 *
 */
package com.cisco.oss.foundation.directory.entity.compatible;
