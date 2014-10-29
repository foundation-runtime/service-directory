package com.cisco.oss.foundation.directory.entity;


public class ACL {
	private int permission;
	
	public AuthScheme scheme;
	private String id;

	public ACL() {
	}

	public ACL(AuthScheme scheme, String id, int permission ) {
		this.permission = permission;
		this.scheme = scheme;
		this.id = id;
	}

	public int getPermission() {
		return permission;
	}

	public void setPermission(int permission) {
		this.permission = permission;
	}
	
	public AuthScheme getScheme() {
		return scheme;
	}

	public void setScheme(AuthScheme scheme) {
		this.scheme = scheme;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public String toString(){
		return "{scheme=" + scheme.name() + ",id=" + id + "}";
	}
	
}
