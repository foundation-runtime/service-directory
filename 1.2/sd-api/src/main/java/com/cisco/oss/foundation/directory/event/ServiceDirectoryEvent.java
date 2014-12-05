/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.event;

/**
 * The ServiceDirectoryEvent in the SD API.
 * 
 * @author zuxiang
 *
 */
public class ServiceDirectoryEvent {
	
	/**
	 * The default constructor.
	 */
	public ServiceDirectoryEvent() {
	}

	/**
	 * The SD API client ConnectionStatus event.
	 * 
	 * @author zuxiang
	 *
	 */
	public static class ClientStatusEvent extends ServiceDirectoryEvent{
		 
		/**
		 * The previous ConnectionStatus.
		 */
		private ConnectionStatus previousStatus;
		
		/**
		 * The current ConnectionStatus.
		 */
		private ConnectionStatus currentStatus;
		
		/**
		 * The constructor.
		 * 
		 * @param pre
		 * 		the previous ConnectionStatus
		 * @param current
		 * 		the current ConnectionStatus.
		 */
		public ClientStatusEvent(ConnectionStatus pre, ConnectionStatus current){
			super();
			this.previousStatus = pre;
			this.currentStatus = current;
		}
		
		/**
		 * Get the previous ConnectionStatus.
		 * 
		 * @return
		 * 		the previous ConnectionStatus.
		 */
		public ConnectionStatus getPreviousStatus() {
			return previousStatus;
		}
		
		/**
		 * Set the previous ConnectionStatus.
		 * 
		 * @param previousStatus
		 * 		the previous ConnectionStatus.
		 */
		public void setPreviousStatus(ConnectionStatus previousStatus) {
			this.previousStatus = previousStatus;
		}
		
		/**
		 * Get the current ConnectionStatus.
		 * 
		 * @return
		 * 		the current ConnectionStatus.
		 */
		public ConnectionStatus getCurrentStatus() {
			return currentStatus;
		}
		
		/**
		 * Set the current ConnectionStatus.
		 * 
		 * @param currentStatus
		 * 		the current ConnectionStatus.
		 */
		public void setCurrentStatus(ConnectionStatus currentStatus) {
			this.currentStatus = currentStatus;
		}
	}
	
	/**
	 * The SD API client session event.
	 * 
	 * @author zuxiang
	 *
	 */
	public static class ClientSessionEvent extends ServiceDirectoryEvent{
		
		/**
		 * The session event.
		 */
		private SessionEvent sessionEvent;
		
		/**
		 * Constructor.
		 * 
		 * @param event
		 * 		the session event.
		 */
		public ClientSessionEvent(SessionEvent event){
			super();
			this.sessionEvent = event;
		}
			
		/**
		 * Get the session event.
		 * 
		 * @return
		 * 		the session event.
		 */
		public SessionEvent getSessionEvent() {
			return sessionEvent;
		}

		/**
		 * Set the session event.
		 * 
		 * @param sessionEvent
		 * 		the session event.
		 */
		public void setSessionEvent(SessionEvent sessionEvent) {
			this.sessionEvent = sessionEvent;
		}
		
		@Override
		public String toString(){
			StringBuilder sb = new StringBuilder("{sessionEvent=");
			sb.append(sessionEvent).append("}");
			return sb.toString();
		}

		/**
		 * The session event.
		 * 
		 * @author zuxiang
		 *
		 */
		public enum SessionEvent{
			/**
			 * The session created.
			 */
			CREATED,
			
			/**
			 * The session closed.
			 */
			CLOSED,
			
			/**
			 * The session reopenned.
			 */
			REOPEN,
		}
	}
}
