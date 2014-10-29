package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.ACL;

public class GetACLResponse extends Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ACL acl;
	public ACL getAcl() {
		return acl;
	}

	public void setAcl(ACL acl) {
		this.acl = acl;
	}

	public GetACLResponse(){
		
	}
	
	public GetACLResponse(ACL acl){
		this.acl = acl;
	}
}
