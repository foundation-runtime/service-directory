package com.cisco.oss.foundation.directory.proto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;

public class RegisterServiceInstanceProtocol extends Protocol{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ProvidedServiceInstance instance;
	private boolean restRequest = false;
	private boolean noSession = false;
	
	public RegisterServiceInstanceProtocol(){};
	
	public RegisterServiceInstanceProtocol(ProvidedServiceInstance instance){
		this.instance = instance;
	}
	
	public RegisterServiceInstanceProtocol(ProvidedServiceInstance instance, boolean restRequest, boolean noSession){
		this.instance = instance;
		this.restRequest = restRequest;
		this.noSession = noSession;
	}
	
	public boolean isNoSession() {
		return noSession;
	}

	public void setNoSession(boolean noSession) {
		this.noSession = noSession;
	}

	public boolean isRestRequest() {
		return restRequest;
	}

	public void setRestRequest(boolean restRequest) {
		this.restRequest = restRequest;
	}
	
	public void setProvidedServiceInstance(ProvidedServiceInstance instance){
		this.instance = instance;
	}
	
	public ProvidedServiceInstance getProvidedServiceInstance(){
		return this.instance;
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException{
		ois.defaultReadObject();
		instance = (ProvidedServiceInstance) ois.readObject();
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException{
		oos.defaultWriteObject();
		oos.writeObject(instance);
	}
}
