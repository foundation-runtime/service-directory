/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

import java.util.List;

import com.cisco.oss.foundation.directory.entity.ACL;

/**
 * Get all ACL by AuthScheme Response.
 * 
 * @author zuxiang
 *
 */
public class GetAllACLResponse extends Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The ACL list.
	 */
	private List<ACL> acls;
	public List<ACL> getAcls() {
		return acls;
	}

	public void setAcls(List<ACL> acls) {
		this.acls = acls;
	}

	public GetAllACLResponse(){
		
	}
	
	public GetAllACLResponse(List<ACL> acls){
		this.acls = acls;
	}
}
