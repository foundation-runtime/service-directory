/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.ACL;

/**
 * Set ACL protocol.
 * @author zuxiang
 *
 */
public class SetACLProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The ACL.
	 */
	private ACL acl;
	public ACL getAcl() {
		return acl;
	}

	public void setAcls(ACL acl) {
		this.acl = acl;
	}

	public SetACLProtocol(){
		
	}
	
	public SetACLProtocol(ACL acl){
		this.acl = acl;
	}
}
