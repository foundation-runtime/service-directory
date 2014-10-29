package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.ACL;

public class SetACLProtocol extends Protocol {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
