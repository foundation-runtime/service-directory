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
package com.cisco.oss.foundation.directory.connect;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.concurrent.Future;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.proto.Protocol;
import com.cisco.oss.foundation.directory.proto.ProtocolHeader;
import com.cisco.oss.foundation.directory.utils.WebSocketSerializer;
import com.cisco.oss.foundation.directory.utils.WebSocketSerializer.ResponseDeserializer;

/**
 * The WebSocket implementation of the DirectorySocket.
 *
 * @author zuxiang
 *
 */
public class WSDirectorySocket extends DirectorySocket implements WebSocketListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(WSDirectorySocket.class);

    /**
     * The Websocket Session.
     */
    private Session session;

    /**
     * The WebSocketClient.
     */
    private WebSocketClient client;

    /**
     * The WebSocket started?
     */
    private boolean websocketStarted = false;

    /**
     * The WebSockte session Future.
     */
    private Future<Session> sessionFuture;

    /**
     * The remote Directory server URI.
     */
    private URI serverURI ;

    /**
     * Indicate whether connected.
     */
    private volatile boolean connected = false;

    /**
     * The session notify lock.
     */
    private Object sessionLock = new Object();

    /**
     * The deamon connect thread.
     */
    private Thread connectThread ;

    /**
     * The connect task.
     */
    private SocketDeamonTask task;

    /**
     * Indicate whether started.
     */
    private boolean isStarted = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected() {
        return connected;
    }

    /**
     * Connect to the remote socket.
     *
     * once fail or exception, it do the cleanup itself.
     */
    @Override
    synchronized public boolean connect(InetSocketAddress addr){
        long now = System.currentTimeMillis();
        if(isStarted){
            return true;
        }
        isStarted = true;

        String host = addr.getHostName();
        int port = addr.getPort();

        serverURI = URI.create("ws://" + host + ":" + port + "/ws/service/");

        if(LOGGER.isTraceEnabled()){
            LOGGER.trace("WebSocket connect to timeOut=" + connectTimeOut + " - " + serverURI.toString());
        }

        client = new WebSocketClient();

        doConnect();

        synchronized(sessionLock){
            long to = connectTimeOut - (System.currentTimeMillis() - now);
            while(! connected && to > 0){

                try {
                    sessionLock.wait(to);
                } catch (InterruptedException e) {
                    // do nothing.
                }
                to = connectTimeOut - (System.currentTimeMillis() - now);
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SocketAddress getRemoteSocketAddress() {
        if(session != null && session.isOpen()){
            return session.getRemoteAddress();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SocketAddress getLocalSocketAddress() {
        if(session != null && session.isOpen()){
            return session.getLocalAddress();
        }
        return null;
    }

    /**
     * Cleanup the DirectorySocket to original status to connect
     * to another Socket.
     */
    @Override
    synchronized public void cleanup() {
        LOGGER.info("Cleanup the DirectorySocket.");

        if(task != null){
            task.toStop();
            task = null;
        }

        if(client != null){
            try {
                client.stop();
            } catch (Exception e) {
                LOGGER.warn("Close WebSocketClient get exception.", e);
            }
            client = null;
        }

        try {
            if(session != null){
                session.close();
            }
        } catch (IOException e) {
            LOGGER.warn("Close the WebSocket Session get exception.", e);
        }

        if(sessionFuture != null){
            try {
            sessionFuture.cancel(true);
            } catch (Exception e) {
                // do nothing
            }
        }

        session = null;
        sessionFuture = null;
        websocketStarted = false;
        connected = false;
        isStarted = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPacket(ProtocolHeader header, Protocol p) throws IOException {
        if(! connected){
            throw new IOException("Websocket not connected.");
        }
        String s = WebSocketSerializer.getProtocolSerializer(header, p).serializerAsString();
        if(LOGGER.isTraceEnabled()){
            LOGGER.trace("Web socket send - " + s);
        }
        session.getRemote().sendString(s);


    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
        // We only use the text message.
        LOGGER.warn("No binary message...");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        LOGGER.info("Close the WebSocket, statusCode=" + statusCode + ", reason=" + reason);
        connected = false;
        clientConnection.onSocketError();
        if(this.session != null){
            try {
                this.session.close();
            } catch (IOException e) {
                LOGGER.warn("Close WebSocket session get exception.");
                if(LOGGER.isTraceEnabled()){
                    LOGGER.trace("Close WebSocket session get exception.", e);
                }
            }
            this.session = null;


            synchronized(sessionLock){
                sessionLock.notifyAll();
            }
//            doConnect();
//            cleanup();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onWebSocketConnect(Session session){
        this.session = session;
        if(LOGGER.isTraceEnabled()){
            LOGGER.trace("WebSocket client receive connection " + session.getRemoteAddress() + " - " +session.getLocalAddress());
        }

        synchronized(sessionLock){
            connected = true;
            sessionLock.notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onWebSocketError(Throwable error) {
        LOGGER.error("WebSocket client get error.");
        if(LOGGER.isTraceEnabled()){
            LOGGER.trace("WebSocket client get error.", error);
        }

        clientConnection.onSocketError();
        if(! connected){
            synchronized(sessionLock){
                sessionLock.notifyAll();
            }
        }
//        cleanup();
//        clientConnection.onSocketError();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onWebSocketText(String text) {
        if(LOGGER.isTraceEnabled()){
            LOGGER.trace("WebSocket client received - " + text);
        }
        ResponseDeserializer ds = null;
        try {
            ds = WebSocketSerializer.getResponseDeserializer(text);
            clientConnection.onReceivedPesponse(ds.deserializerResponseHeader(), ds.deserializerResponse());
        } catch (IOException e) {
            LOGGER.error("Parse response get exception", e);
        }

    }

    /**
     * Do the WebSocket connect.
     */
    private void doConnect(){
        task = new SocketDeamonTask();
        connectThread = new Thread(task);
        connectThread.setDaemon(true);
        connectThread.start();
    }

    /**
     * The Socket deamon task.
     *
     * @author zuxiang
     *
     */
    class SocketDeamonTask implements Runnable{
        /**
         * Indicate whether to stop.
         */
        private volatile boolean toStop = false;

        /**
         * to stop the deamon task.
         */
        public void toStop(){
            toStop = true;
        }

        @Override
        public void run() {
            while(! toStop ){
                try {
                    if(! websocketStarted){
                        client.start();
                        websocketStarted = true;
                    }
                    if(LOGGER.isTraceEnabled()){
                        LOGGER.trace("do the Websocket connect, connected=" + connected + " - " + Thread.currentThread().getId());
                    }

                    if(connected){
                        synchronized(sessionLock){
                            sessionLock.wait();
                        }
                    } else {
                        synchronized(sessionLock){
                            client.connect(WSDirectorySocket.this, serverURI);
                            sessionLock.wait();
                        }

                    }


                    Thread.sleep(100);
                } catch (Exception e) {
                    if(LOGGER.isTraceEnabled()){
                        LOGGER.trace("Websocket connect get exception.", e);
                    }
                }
            }
        }

    }

}
