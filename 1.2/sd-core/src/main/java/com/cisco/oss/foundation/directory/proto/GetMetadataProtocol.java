package com.cisco.oss.foundation.directory.proto;


public class GetMetadataProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String keyName;
	
	private boolean watcher = false;
	
	public GetMetadataProtocol(){
		
	}
	
	public GetMetadataProtocol(String keyName){
		this.keyName = keyName;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public boolean isWatcher() {
		return watcher;
	}

	public void setWatcher(boolean watcher) {
		this.watcher = watcher;
	}
	
	

}
