package com.cisco.oss.foundation.directory;

import java.util.concurrent.atomic.AtomicInteger;

public class ServiceDirectoryThread {
	
	public final static String SD_THREAD_PREFIX = "SD_";
	
	private final static AtomicInteger index = new AtomicInteger(0);
	
	private final static boolean DefaultThreadDeamon = false;

	public static Thread getThread(Runnable runnable){
		return doThread(runnable, null, DefaultThreadDeamon);
	}
	
	public static Thread getThread(Runnable runnable, String name){
		return doThread(runnable, name, DefaultThreadDeamon);
	}
	
	public static Thread getThread(Runnable runnable, String name, boolean deamon){
		return doThread(runnable, name, deamon);
	}
	
	public static String getThreadName(String name){
		if(name==null || name.isEmpty()){
			return SD_THREAD_PREFIX + nextIndex(); 
		}
		return SD_THREAD_PREFIX + name + "_" + nextIndex();
	}
	
	public static int nextIndex(){
		return index.incrementAndGet();
	}
	
	private static Thread doThread(Runnable runnable, String name, boolean deamon){
		String realname = getThreadName(name);
		Thread t = new Thread(runnable);
		t.setName(realname);
		t.setDaemon(deamon);
		return t;
	}
	
	
	
}
