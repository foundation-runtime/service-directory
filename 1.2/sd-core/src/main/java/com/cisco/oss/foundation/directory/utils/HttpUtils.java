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

import static java.net.HttpURLConnection.*;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.exception.ServiceException;


/**
 * Convenient Http Client util methods to invoke remote RESTful Service.
 *
 *
 */
public final class HttpUtils {
    
    public static enum HttpMethod {
        GET, PUT, POST, DELETE
    }

    /**
     * prevent to create instance
     */
    private HttpUtils() { }


    /**
     * Invoke REST Service using POST method.
     *
     * @param urlStr
     *            the REST service URL String
     * @param body
     *            the Http Body String.
     * @return the HttpResponse.
     * @throws IOException
     * @throws ServiceException
     */
    public static HttpResponse postJson(String urlStr, String body) throws IOException, ServiceException {

        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url
                .openConnection();
        if (urlConnection instanceof HttpsURLConnection) {
            setTLSConnection((HttpsURLConnection)urlConnection);
        }
        urlConnection.addRequestProperty("Accept", "application/json");

        urlConnection.setRequestMethod("POST");

        urlConnection.addRequestProperty("Content-Type", "application/json");
        urlConnection.addRequestProperty("Content-Length",
                Integer.toString(body.length()));
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.setUseCaches(false);

        OutputStream out = urlConnection.getOutputStream();
        out.write(body.getBytes());
        ByteStreams.copy(new ByteArrayInputStream(body.getBytes()), out);
        return getHttpResponse(urlConnection);
    }

    /**
     * Invoke REST Service using POST method.
     *
     * @param urlStr
     *            the REST service URL String
     * @param body
     *            the Http Body String.
     * @param headers
     *            the Http header Map.
     * @return the HttpResponse.
     * @throws IOException
     * @throws ServiceException
     */
    public static HttpResponse postJson(String urlStr, String body, Map<String, String> headers) throws IOException, ServiceException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url
                .openConnection();
        if (urlConnection instanceof HttpsURLConnection) {
            setTLSConnection((HttpsURLConnection)urlConnection);
        }
        
        urlConnection.addRequestProperty("Accept", "application/json");

        urlConnection.setRequestMethod("POST");

        addCustomHeaders(urlConnection, headers);

        urlConnection.addRequestProperty("Content-Type", "application/json");
        urlConnection.addRequestProperty("Content-Length",
                Integer.toString(body.length()));
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.setUseCaches(false);

        OutputStream out = urlConnection.getOutputStream();
        out.write(body.getBytes());
        ByteStreams.copy(new ByteArrayInputStream(body.getBytes()), out);
        return getHttpResponse(urlConnection);
    }
    
    /**
     * Invoke REST Service using PUT method.
     *
     * @param urlStr
     *            the REST Service URL String.
     * @param body
     *            the Http Body String.
     * @return the HttpResponse.
     * @throws IOException
     * @throws ServiceException 
     */
    public static HttpResponse putJson(String urlStr, String body) throws IOException, ServiceException {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        return put(urlStr, body, headers);
    }

    /**
     * Invoke REST Service using PUT method.
     *
     * It accepts the JSON message.
     *
     * @param urlStr
     *            the REST Service URL String.
     * @param body
     *            the Http Body String.
     * @param headers
     *            the Http header Map.
     * @return the HttpResponse.
     * @throws IOException
     * @throws ServiceException
     */
    public static HttpResponse put(String urlStr, String body,
            Map<String, String> headers) throws IOException, ServiceException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url
                .openConnection();
        if (urlConnection instanceof HttpsURLConnection) {
            setTLSConnection((HttpsURLConnection)urlConnection);
        }
        
        urlConnection.addRequestProperty("Accept", "application/json");

        urlConnection.setRequestMethod("PUT");
        addCustomHeaders(urlConnection, headers);

        urlConnection.addRequestProperty("Content-Type", "application/json");
        if (body != null && body.length() > 0)
            urlConnection.addRequestProperty("Content-Length",
                    Integer.toString(body.length()));
        else
            urlConnection.addRequestProperty("Content-Length", "0");
        urlConnection.setDoOutput(true);

        OutputStream out = urlConnection.getOutputStream();
        if (body != null && body.length() > 0)
            ByteStreams.copy(new ByteArrayInputStream(body.getBytes()), out);

        return getHttpResponse(urlConnection);

    }

    /**
     * Invoke REST Service using GET method.
     *
     * @param urlStr
     *            the REST service URL String.
     * @return the HttpResponse.
     * @throws IOException
     * @throws ServiceException
     */
    public static HttpResponse getJson(String urlStr) throws IOException, ServiceException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url
                .openConnection();
        if (urlConnection instanceof HttpsURLConnection) {
            setTLSConnection((HttpsURLConnection)urlConnection);
        }
        urlConnection.addRequestProperty("Accept", "application/json");

        return getHttpResponse(urlConnection);
    }

    /**
     * Invoke REST Service using GET method.
     *
     * @param urlStr
     *            the REST service URL String.
     * @param headers
     *            the Http header Map.
     * @return the HttpResponse.
     * @throws IOException
     * @throws ServiceException
     */
    public static HttpResponse getJson(String urlStr, Map<String, String> headers) throws IOException, ServiceException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url
                .openConnection();
        if (urlConnection instanceof HttpsURLConnection) {
            setTLSConnection((HttpsURLConnection)urlConnection);
        }
        urlConnection.addRequestProperty("Accept", "application/json");
        addCustomHeaders(urlConnection, headers);

        return getHttpResponse(urlConnection);
    }
    /**
     * Invoke REST Service using DELETE method.
     *
     * @param urlStr
     *            the REST URL String.
     * @return the HttpResponse.
     * @throws IOException
     * @throws ServiceException
     */
    public static HttpResponse deleteJson(String urlStr) throws IOException, ServiceException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url
                .openConnection();
        if (urlConnection instanceof HttpsURLConnection) {
            setTLSConnection((HttpsURLConnection)urlConnection);
        }
        urlConnection.addRequestProperty("Accept", "application/json");

        urlConnection.setRequestMethod("DELETE");

        return getHttpResponse(urlConnection);
    }

    /**
     * Invoke REST Service using DELETE method.
     *
     * @param urlStr
     *            the REST URL String.    
     * @param headers
     *            the Http header Map.
     * @return the HttpResponse.
     * @throws IOException
     * @throws ServiceException
     */
    public static HttpResponse deleteJson(String urlStr, Map<String, String> headers) throws IOException, ServiceException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url
                .openConnection();
        if (urlConnection instanceof HttpsURLConnection) {
            setTLSConnection((HttpsURLConnection)urlConnection);
        }
        urlConnection.addRequestProperty("Accept", "application/json");
        addCustomHeaders(urlConnection, headers);

        urlConnection.setRequestMethod("DELETE");

        return getHttpResponse(urlConnection);
    }
    
    private static HttpsURLConnection setTLSConnection(
            HttpsURLConnection secureConn) throws ServiceException {
        try {
            // TODO enable the cert authentication later
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs,
                        String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs,
                        String authType) {
                }
            } };
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, trustAllCerts, null);
            secureConn.setSSLSocketFactory(ctx.getSocketFactory());

            secureConn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return secureConn;
        } catch (Throwable e) {
            throw new ServiceException(
                    ErrorCode.SERVICE_DIRECTORY_SSLRUNTIMEEXCEPTION,
                    ErrorCode.SERVICE_DIRECTORY_SSLRUNTIMEEXCEPTION
                            .getMessageTemplate(), e);
        }
    }
    
    private static HttpResponse getHttpResponse(HttpURLConnection urlConnection) throws IOException {
        BufferedReader in = null;
        try {
            int errorCode = urlConnection.getResponseCode();
            // HTTP_OK (200), HTTP_CREATED (201), HTTP_ACCEPTED (202)
            if (((errorCode == HTTP_OK) || (errorCode == HTTP_CREATED) || errorCode == HTTP_ACCEPTED) ) {
                in = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
            } else {
                InputStream error = urlConnection.getErrorStream();
                if (error != null) {
                    in = new BufferedReader(new InputStreamReader(error));
                }
            }

            String json = null;
            if (in != null) {
                json = CharStreams.toString(in);
            }
            return new HttpResponse(errorCode, json);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
    private static void addCustomHeaders(HttpURLConnection urlConnection,
            Map<String, String> headers) {

        if (headers != null) {
            for (Entry<String, String> entry : headers.entrySet()) {
                urlConnection.addRequestProperty(entry.getKey(),
                        entry.getValue());
            }
        }
    }

}
