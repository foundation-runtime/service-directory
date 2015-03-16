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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.ServiceDirectoryThread;
import com.cisco.oss.foundation.directory.exception.ConnectTimeOutException;
import com.cisco.oss.foundation.directory.proto.Protocol;
import com.cisco.oss.foundation.directory.proto.ProtocolHeader;
import com.cisco.oss.foundation.directory.proto.Response;
import com.cisco.oss.foundation.directory.proto.ResponseHeader;

/**
 * The java nio implementation of the DirectorySocket.
 *
 *
 */
public class NIODirectorySocket extends DirectorySocket {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(NIODirectorySocket.class);

    /**
     * The nio Selector.
     */
    private Selector selector;

    /**
     * The nio SelectionKey.
     */
    private SelectionKey selectionKey;

    /**
     * The deamon NIOThread to send and receive.
     */
    private NIOThread nioThread;

    /**
     * The connect holder to block thread.
     */
    private Object connectHolder = new Object();

    /**
     * Indicate whether socket connect.
     */
    private volatile boolean isConnected = false;

    /**
     * Indicate socket is connecting.
     */
    private volatile boolean isConnecting = false;

    /**
     * Constructor.
     */
    public NIODirectorySocket() {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected() {
        return this.isConnected;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean connect(InetSocketAddress address) {
        nioThread = new NIOThread(address);
        nioThread.start();

        isConnecting = true;
        long timeToSleep = this.connectTimeOut;
        if(LOGGER.isTraceEnabled()){
            LOGGER.trace("NIO connect go to sleep - " + timeToSleep);
        }
        while(isConnecting && timeToSleep > 0){
            long now = System.currentTimeMillis();

            try {
                synchronized(connectHolder){
                    connectHolder.wait(timeToSleep);
                }
            } catch (InterruptedException e) {
                // do nothing.
            }
            timeToSleep = timeToSleep - (System.currentTimeMillis() - now);
            if(LOGGER.isTraceEnabled()){
                LOGGER.trace("NIO connect go to sleep - " + timeToSleep + ", isConnecting=" + isConnecting);
            }
        }



        if(! isConnected()){
            onConnectFailed();
            return false;
        }else{
            lenBuffer.clear();
            incomingBuffer = lenBuffer;
            return true;
        }

    }

    /**
     * Register a SocketChannel and connect the remote address.
     *
     * @param sock
     *         the SocketChannel.
     * @param addr
     *         the remote address.
     * @throws IOException
     *         the IOException.
     */
    void registerAndConnect(SocketChannel sock, InetSocketAddress addr)
            throws IOException {
        selectionKey = sock.register(selector, SelectionKey.OP_CONNECT);

        boolean immediateConnect = sock.connect(addr);
        if(LOGGER.isTraceEnabled()){
            LOGGER.trace("Connect to host=" + addr.getHostName() + ", hostString=" + addr.getHostString() + ", port=" + addr.getPort() + ", all=" + addr.getAddress() + ", local=" + sock.socket().getLocalSocketAddress());
        }
        if (immediateConnect) {
            onConnectSucceeded();
        }


    }

    /**
     * Create a SocketChannel.
     *
     * @return
     *         the SocketChannel.
     * @throws IOException
     *         the IOException.
     */
    SocketChannel createSock() throws IOException {
        SocketChannel sock;
        sock = SocketChannel.open();
        sock.configureBlocking(false);
        sock.socket().setSoLinger(false, -1);
        sock.socket().setTcpNoDelay(true);
        return sock;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SocketAddress getRemoteSocketAddress() {
        try {
            return ((SocketChannel) selectionKey.channel()).socket()
                    .getRemoteSocketAddress();
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SocketAddress getLocalSocketAddress() {
        try {
            return ((SocketChannel) selectionKey.channel()).socket()
                    .getLocalSocketAddress();
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanup() {
        if(nioThread != null){
            nioThread.toStop();
        }

        if (selectionKey != null) {

            SocketChannel sock = (SocketChannel) selectionKey.channel();
            selectionKey.cancel();
            try {
                sock.socket().shutdownInput();
            } catch (IOException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Ignoring exception during shutdown input", e);
                }
            }
            try {
                sock.socket().shutdownOutput();
            } catch (IOException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Ignoring exception during shutdown output",
                            e);
                }
            }
            try {
                sock.socket().close();
            } catch (IOException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Ignoring exception during socket close", e);
                }
            }
            try {
                sock.close();
            } catch (IOException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Ignoring exception during channel close", e);
                }
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("SendThread interrupted during sleep, ignoring");
            }
        }
        selectionKey = null;
    }

    /**
     * Close the socket.
     */
    @SuppressWarnings("unused")
    private void close() {
        try {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Doing client selector close");
            }
            selector.close();
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Closed client selector");
            }
        } catch (IOException e) {
            LOGGER.warn("Ignoring exception during selector close", e);
        }
    }

    /**
     * only enable the read for the SelectionKey.
     */
    private void enableReadOnly() {
//        int i = selectionKey.interestOps();
//        if ((i & SelectionKey.OP_READ) == 0) {
            selectionKey.interestOps(SelectionKey.OP_READ);
//        }
    }

    /**
     * Wake up the Selector.
     */
    @SuppressWarnings("unused")
    private void wakeupCnxn() {
        selector.wakeup();
    }

    /**
     * Read the buffer message length.
     *
     * @throws IOException
     *         the IOException.
     */
    protected void readLength() throws IOException {
        int len = incomingBuffer.getInt();
        if(LOGGER.isTraceEnabled()){
            LOGGER.trace("Read length in client - " + len);
        }
        if (len < 0 || len >= DirectoryConnection.packetLen) {
            throw new IOException("Packet len" + len + " is out of range!");
        }
        incomingBuffer = ByteBuffer.allocate(len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPacket(ProtocolHeader header, Protocol protocol) throws IOException {
        SocketChannel sock = (SocketChannel) selectionKey.channel();
        if (sock == null) {
            throw new IOException("Socket is null!");
        }

        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(header);
            oos.writeObject(protocol);

            StringBuilder sb = new StringBuilder("Create ByteBuffer, RequestHeader[");
            if(header != null){
                sb.append("xid=").append(header.getXid());
                sb.append(", Type=").append(header.getType());
            }else{
                sb.append("NULL");
            }
            sb.append("], Request[");
            if(protocol != null){
                sb.append(protocol.getClass().getName());
            } else {
                sb.append("NULL");
            }
            sb.append("]");
            if(LOGGER.isTraceEnabled()){
                LOGGER.trace(sb.toString());
            }

            byte[] bytes = baos.toByteArray();
            int l = bytes.length + 4;
            ByteBuffer bb = ByteBuffer.allocate(l);
            bb.putInt(bytes.length);
            bb.put(bytes);
            if(LOGGER.isTraceEnabled()){
                LOGGER.trace(".................Send packet, xid=" + header.getXid() + ", type=" + header.getType());
            }
            bb.flip();
            sock.write(bb);
        } catch (IOException e) {
            LOGGER.warn("Ignoring unexpected exception", e);
        } finally {
            try {
                if(baos != null){
                    baos.close();
                }
            } catch (IOException e) {
                // do nothing.
            }
        }

    }

    /**
     * Get the Selector.
     *
     * @return
     *         the nio Selector.
     */
    public Selector getSelector() {
        return selector;
    }

    /**
     * Do the Socket connect.
     *
     * @param addr
     *         the remote address.
     * @throws IOException
     *         the IOException.
     */
    private void doSocketConnect(InetSocketAddress addr) throws IOException{
        SocketChannel sock = createSock();
        registerAndConnect(sock, addr);
    }

    /**
     * On the socked connected.
     */
    private void onConnectSucceeded(){
        LOGGER.info("Accepted the connection.");

        isConnected = true;
        isConnecting = false;
        enableReadOnly();
        synchronized(connectHolder){
            connectHolder.notifyAll();
        }
    }

    /**
     * On the socket connect failed.
     */
    private void onConnectFailed(){
        LOGGER.info("Socket failed");
        isConnecting = false;
        isConnected = false;
        cleanup();
        synchronized(connectHolder){
            connectHolder.notifyAll();
        }
    }

    /**
     * The deamon NIO thread.
     *
     *
     */
    class NIOThread extends Thread {

        /**
         * The remote server.
         */
        private final InetSocketAddress server;

        /**
         * to stop the NIOThread.
         */
        private boolean toStop = false;

        /**
         * Constructor.
         *
         * @param server
         *         the server address.
         */
        public NIOThread(InetSocketAddress server){
            super(ServiceDirectoryThread.getThreadName("NIO_Connection_Socket"));
            this.server = server;
        }

        /**
         * to stop the NIOThread.
         */
        public void toStop(){
            this.toStop = true;
        }

        @Override
        public void run(){
            if(LOGGER.isTraceEnabled()){
                LOGGER.trace("NIO Thread do connection");
            }
            try {
                doSocketConnect(server);
            } catch (IOException e1) {
                LOGGER.error("Socket connect to server failed - " + server);
                onConnectFailed();
            }

            isConnecting = true;
            updateNow();
            updateLastSendAndHeard();

            while(! toStop){
                try{
                    int timeOut = connectTimeOut;
                    if(isConnecting){
                        timeOut = connectTimeOut - getIdleRecv();
                    }
                    if(LOGGER.isTraceEnabled()){
                        if(selectionKey != null){
                            LOGGER.trace("NIO Thread kick off, timeOut=" + timeOut + ", ops=" + selectionKey.interestOps());// + ", keySize=" + selected.size());}

                        } else {
                            LOGGER.trace("NIO Thread kick off, timeOut=" + timeOut );
                        }
                    }

                    selector.select(timeOut);
                    Set<SelectionKey> selected;
                    synchronized (this) {
                        selected = selector.selectedKeys();
                    }
                    if(LOGGER.isTraceEnabled()){
                        LOGGER.trace("NIO Thread kick off, size=" + selected.size() );
                    }


                    updateNow();
                    for (SelectionKey k : selected) {
                        SocketChannel sc = ((SocketChannel) k.channel());
                        if ((k.readyOps() & SelectionKey.OP_CONNECT) != 0) {
                            if (sc.finishConnect()) {
                                if(LOGGER.isTraceEnabled()){
                                    LOGGER.trace("The local socket is " + sc.socket().getLocalSocketAddress());
                                }
                                updateLastHeard();
                                onConnectSucceeded();
                            }
                        } else if ((k.readyOps() & (SelectionKey.OP_READ )) != 0) {
                             doIO();
                        }
                    }

                    if(isConnecting){
                        int to = (int) (System.currentTimeMillis() - getIdleRecv());
                        if(to < 0){
                            throw new ConnectTimeOutException();
                        }
                    }
                    selected.clear();

                } catch (Exception e) {
                    if(LOGGER.isTraceEnabled()){
                        LOGGER.trace("NIO Exception.", e);
                    }
                    if(! isConnected()){
                        if (e instanceof ConnectTimeOutException) {

                        }
                        onConnectFailed();
                    }else{

                    }


                }
            }

        }

        /**
         * Do the IO.
         *
         * @throws IOException
         */
        private void doIO() throws IOException {
            if(LOGGER.isTraceEnabled()){
                LOGGER.trace("doIO, readable=" + selectionKey.isReadable() + ", writable=" + selectionKey.isWritable());
            }
            SocketChannel sc = (SocketChannel) selectionKey.channel();
            if (selectionKey.isReadable()) {
                sc.read(incomingBuffer);
                if (!incomingBuffer.hasRemaining()) {
                    incomingBuffer.flip();
                    if (incomingBuffer == lenBuffer) {
                        recvCount++;
                        readLength();
                    } else {
                        readResponse();
                        lenBuffer.clear();
                        incomingBuffer = lenBuffer;
                        updateLastHeard();
                    }
                }
            }

        }

        /**
         * Read the response.
         *
         * @throws IOException
         *         the IOException.
         */
        private void readResponse() throws IOException{

            ByteArrayInputStream bbis = new ByteArrayInputStream(
                    incomingBuffer.array());
            ObjectInputStream ois = new ObjectInputStream(bbis);
            ResponseHeader header = null;
            Response resp = null;

            try {
                header = (ResponseHeader) ois.readObject();
                resp = (Response) ois.readObject();
            } catch (ClassNotFoundException e1) {
                LOGGER.error("Deserialize response failed - " + e1.getMessage());
                throw new IOException("Desierialize Response failed - " + e1.getMessage(), e1);

            }
            if(LOGGER.isTraceEnabled()){
                LOGGER.trace("......................Received response, xid=" + header.getXid() +", dxid=" + header.getDxid() + ", time=" + System.currentTimeMillis());
            }


            clientConnection.onReceivedPesponse(header, resp);
        }

    }



}
