package com.cisco.oss.foundation.directory.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Role {

	READ(Permission.READ),
	WRITE(Permission.READ, Permission.WRITE),
	CREATE(Permission.READ, Permission.WRITE, Permission.CREATE),
	DELETE(Permission.READ, Permission.WRITE, Permission.DELETE),
	ADMIN(Permission.READ, Permission.WRITE, Permission.DELETE, Permission.CREATE, Permission.ADMIN),
	;
	
	private Permission[] permission;
	
	
	Role(Permission... permission){
		this.permission = permission;
	}
	
	public List<Permission> getPermission() {
		ArrayList<Permission> list = new ArrayList<Permission>();
		list.addAll(Arrays.asList(permission));
		return list;
	}
	
}
