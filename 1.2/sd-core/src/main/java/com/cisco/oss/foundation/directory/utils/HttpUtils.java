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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;

/**
 * Convenient Http Client util methods to invoke remote RESTful Service.
 *
 * @author zuxiang
 *
 */
public class HttpUtils {
    public static enum HttpMethod {
        GET, PUT, POST, DELETE
    }

    private volatile static HttpUtils instance = null;

    /**
     * protect the singleton.
     */
    protected HttpUtils() {

    }

    /**
     * Get the HttpUtils singleton instance.
     *
     * @return HttpUtils instance.
     */
    public static HttpUtils getInstance() {
        if (instance == null) {
            synchronized (HttpUtils.class) {
                if (instance == null) {
                    instance = new HttpUtils();
                }
            }
        }
        return instance;
    }

    /**
     * Invoke REST Service using POST method.
     *
     * @param urlStr
     *            the REST service URL String
     * @param body
     *            the Http Body String.
     * @return the HttpResponse.
     * @throws IOException
     */
    public HttpResponse postJson(String urlStr, String body) throws IOException {

        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url
                .openConnection();
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
        BufferedReader in = null;
        try {
            int errorCode = urlConnection.getResponseCode();
            if ((errorCode <= 202) && (errorCode >= 200)) {
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

    /**
     * Invoke REST Service using PUT method.
     *
     * @param urlStr
     *            the REST Service URL String.
     * @param body
     *            the Http Body String.
     * @return the HttpResponse.
     * @throws IOException
     */
    public HttpResponse putJson(String urlStr, String body) throws IOException {
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
     */
    public HttpResponse put(String urlStr, String body,
            Map<String, String> headers) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url
                .openConnection();
        urlConnection.addRequestProperty("Accept", "application/json");

        urlConnection.setRequestMethod("PUT");
        if (headers != null) {
            for (Entry<String, String> entry : headers.entrySet()) {
                urlConnection.addRequestProperty(entry.getKey(),
                        entry.getValue());
            }
        }

        if (body != null && body.length() > 0)
            urlConnection.addRequestProperty("Content-Length",
                    Integer.toString(body.length()));
        else
            urlConnection.addRequestProperty("Content-Length", "0");
        urlConnection.setDoOutput(true);

        OutputStream out = urlConnection.getOutputStream();
        if (body != null && body.length() > 0)
            ByteStreams.copy(new ByteArrayInputStream(body.getBytes()), out);

        BufferedReader in = null;
        try {
            int errorCode = urlConnection.getResponseCode();
            if ((errorCode <= 202) && (errorCode >= 200)) {
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

    /**
     * Invoke REST Service using GET method.
     *
     * @param urlStr
     *            the REST service URL String.
     * @return the HttpResponse.
     * @throws IOException
     */
    public HttpResponse getJson(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url
                .openConnection();
        urlConnection.addRequestProperty("Accept", "application/json");

        BufferedReader in = null;
        try {
            int errorCode = urlConnection.getResponseCode();
            if ((errorCode <= 202) && (errorCode >= 200)) {
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

    /**
     * Invoke REST Service using DELETE method.
     *
     * @param urlStr
     *            the REST URL String.
     * @return the HttpResponse.
     * @throws IOException
     */
    public HttpResponse deleteJson(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection) url
                .openConnection();
        urlConnection.addRequestProperty("Accept", "application/json");

        urlConnection.setRequestMethod("DELETE");

        BufferedReader in = null;
        try {
            int errorCode = urlConnection.getResponseCode();
            if ((errorCode <= 202) && (errorCode >= 200)) {
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
}
