/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.oss.foundation.directory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The ServiceDirectory Thread util to name and create Thread.
 *
 * @author zuxiang
 *
 */
public class ServiceDirectoryThread {

    /**
     * The thread prefix.
     */
    public final static String SD_THREAD_PREFIX = "SD_";

    /**
     * The current thread index.
     */
    private final static AtomicInteger index = new AtomicInteger(0);

    /**
     * THe defautl thread deamon flag.
     */
    private final static boolean DefaultThreadDeamon = false;

    /**
     * Get a Thread.
     *
     * @param runnable
     *         the runnable task.
     * @return
     *         the Thread.
     */
    public static Thread getThread(Runnable runnable){
        return doThread(runnable, null, DefaultThreadDeamon);
    }

    /**
     * Get a Thread.
     *
     * @param runnable
     *         the runnable task.
     * @param name
     *         the thread name.
     * @return
     *         the Thread.
     */
    public static Thread getThread(Runnable runnable, String name){
        return doThread(runnable, name, DefaultThreadDeamon);
    }

    /**
     * Get a Thread.
     *
     * @param runnable
     *         the runnable task.
     * @param name
     *         the thread name.
     * @param deamon
     *         the deamon flag.
     * @return
     *         the Thread.
     */
    public static Thread getThread(Runnable runnable, String name, boolean deamon){
        return doThread(runnable, name, deamon);
    }

    /**
     * Get the SD prefixed thread name.
     *
     * @param name
     *         the thread name.
     * @return
     *         the prefixed thread name.
     */
    public static String getThreadName(String name){
        if(name==null || name.isEmpty()){
            return SD_THREAD_PREFIX + nextIndex();
        }
        return SD_THREAD_PREFIX + name + "_" + nextIndex();
    }

    /**
     * Get next thread index.
     *
     * @return
     *         the thread index.
     */
    public static int nextIndex(){
        return index.incrementAndGet();
    }

    /**
     * Generate the Thread.
     *
     * @param runnable
     *         the runnable task.
     * @param name
     *         the thread name.
     * @param deamon
     *         the deamon flag.
     * @return
     *         the Thread.
     */
    private static Thread doThread(Runnable runnable, String name, boolean deamon){
        String realname = getThreadName(name);
        Thread t = new Thread(runnable);
        t.setName(realname);
        t.setDaemon(deamon);
        return t;
    }



}
