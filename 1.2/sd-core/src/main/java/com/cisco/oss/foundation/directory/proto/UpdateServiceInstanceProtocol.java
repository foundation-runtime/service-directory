package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;

public class UpdateServiceInstanceProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ProvidedServiceInstance providedServiceInstance;
	private boolean isOwned = false;
	private boolean restRequest = false;
	private boolean noSession = false;
	
	public UpdateServiceInstanceProtocol(){
		
	}
	
	public UpdateServiceInstanceProtocol(ProvidedServiceInstance providedServiceInstance){
		this.providedServiceInstance = providedServiceInstance;
	}
	
	public UpdateServiceInstanceProtocol(ProvidedServiceInstance providedServiceInstance, boolean restRequest, boolean isOwned, boolean noSession){
		this.providedServiceInstance = providedServiceInstance;
		this.restRequest = restRequest;
		this.isOwned = isOwned;
		this.noSession = noSession;
	}
	
	public boolean isNoSession() {
		return noSession;
	}

	public void setNoSession(boolean noSession) {
		this.noSession = noSession;
	}

	public boolean isOwned() {
		return isOwned;
	}

	public void setOwned(boolean isOwned) {
		this.isOwned = isOwned;
	}

	public boolean isRestRequest() {
		return restRequest;
	}

	public void setRestRequest(boolean restRequest) {
		this.restRequest = restRequest;
	}

	public ProvidedServiceInstance getProvidedServiceInstance() {
		return providedServiceInstance;
	}

	public void setProvidedServiceInstance(
			ProvidedServiceInstance providedServiceInstance) {
		this.providedServiceInstance = providedServiceInstance;
	}

	
}
