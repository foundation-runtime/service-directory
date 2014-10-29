package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.EventType;
import com.cisco.oss.foundation.directory.entity.ServerStatus;


public class ServerEvent extends Event{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ServerEvent(){
		super(EventType.Server);
	}
	
	public static class ServerStatusEvent extends ServerEvent{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private ServerStatus serverStatus;
		public ServerStatusEvent(){
			
		}
		
		public ServerStatusEvent(ServerStatus serverStatus){
			super();
			this.setServerStatus(serverStatus);
		}
		public ServerStatus getServerStatus() {
			return serverStatus;
		}
		public void setServerStatus(ServerStatus serverStatus) {
			this.serverStatus = serverStatus;
		}
	}
	
	public static class CloseSessionEvent extends ServerEvent {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private String message;

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
		
		public CloseSessionEvent(){
			
		}
		
		public CloseSessionEvent(String message){
			this.message = message;
		}
		
		
	}
	

}
