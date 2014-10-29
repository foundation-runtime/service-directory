package com.cisco.oss.foundation.directory.proto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BatchProtocol<T> extends Protocol implements Iterable<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<T> subProtocols;
	
	public List<T> getSubProtocols(){
		return subProtocols;
	}
	
	public void addSubProtocol(T protocol){
		if(subProtocols == null){
			subProtocols = new ArrayList<T>();
		}
		subProtocols.add(protocol);
	}
	
	@Override
	public Iterator<T> iterator(){
		if(subProtocols == null){
			return Collections.emptyIterator();
		}
		return subProtocols.iterator();
	}

	public static class RegisterServiceInstanceBatchProtocol extends BatchProtocol<RegisterServiceInstanceProtocol>{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		
		
	}
	
	public static class UnregisterServiceInstanceBatchProtocol extends BatchProtocol<UnregisterServiceInstanceProtocol>{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}
	
}
