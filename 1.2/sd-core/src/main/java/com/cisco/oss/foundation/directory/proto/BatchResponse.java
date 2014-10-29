package com.cisco.oss.foundation.directory.proto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class BatchResponse<T> extends Response implements Iterable<T>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<T> responses;
	
	public void addResponse(T resp){
		if(responses == null){
			responses = new ArrayList<T>();
		}
		responses.add(resp);
	}
	
	public List<T> getSubResponses(){
		return responses;
	}
	

	@Override
	public Iterator<T> iterator() {
		if(responses == null){
			return Collections.emptyIterator();
		}
		return responses.iterator();
	}
	
}
