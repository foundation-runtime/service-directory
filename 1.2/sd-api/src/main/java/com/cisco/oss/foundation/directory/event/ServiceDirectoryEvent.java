package com.cisco.oss.foundation.directory.event;


public class ServiceDirectoryEvent {
	public ServiceDirectoryEvent() {
	}

	public static class ClientStatusEvent extends ServiceDirectoryEvent{
		 
		private ConnectionStatus previousStatus;
		private ConnectionStatus currentStatus;
		
		public ClientStatusEvent(ConnectionStatus pre, ConnectionStatus current){
			super();
			this.previousStatus = pre;
			this.currentStatus = current;
		}
		
		public ConnectionStatus getPreviousStatus() {
			return previousStatus;
		}
		public void setPreviousStatus(ConnectionStatus previousStatus) {
			this.previousStatus = previousStatus;
		}
		public ConnectionStatus getCurrentStatus() {
			return currentStatus;
		}
		public void setCurrentStatus(ConnectionStatus currentStatus) {
			this.currentStatus = currentStatus;
		}
		
		
	}
	
	public static class ClientSessionEvent extends ServiceDirectoryEvent{
		
		private SessionEvent sessionEvent;
		
		public ClientSessionEvent(SessionEvent event){
			super();
			this.sessionEvent = event;
		}
				
		public SessionEvent getSessionEvent() {
			return sessionEvent;
		}

		public void setSessionEvent(SessionEvent sessionEvent) {
			this.sessionEvent = sessionEvent;
		}
		
		@Override
		public String toString(){
			StringBuilder sb = new StringBuilder("{sessionEvent=");
			sb.append(sessionEvent).append("}");
			return sb.toString();
		}

		public enum SessionEvent{
			CREATED,
			CLOSED,
			REOPEN,
		}
	}
}
