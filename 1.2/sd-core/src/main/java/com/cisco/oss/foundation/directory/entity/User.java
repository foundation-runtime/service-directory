package com.cisco.oss.foundation.directory.entity;

import java.util.List;

public class User {
	private String name;
	private List<Permission> permission;
	
	public User(){
		
	}
	
	public User(String name, List<Permission> permission){
		this.setName(name);
		this.permission = permission;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Permission> getPermission() {
		return permission;
	}

	public void setPermission(List<Permission> permission) {
		this.permission = permission;
	}
}
