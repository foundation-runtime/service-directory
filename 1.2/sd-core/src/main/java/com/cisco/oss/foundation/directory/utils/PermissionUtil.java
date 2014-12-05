/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.utils;

import java.util.ArrayList;
import java.util.List;

import com.cisco.oss.foundation.directory.entity.Permission;

/**
 * The permission tranfer utils.
 * 
 * @author zuxiang
 *
 */
public class PermissionUtil {
	
	/**
	 * Transfer the permission id to Permission list.
	 * 
	 * @param id
	 * 		the permission id.
	 * @return
	 * 		the Permission list.
	 */
	public static List<Permission> id2Permissions(int id){
		List<Permission> list = new ArrayList<Permission>();
		if(id <= 0 || id > 31){
			list.add(Permission.NONE); 
		}else{
			if((id & Permission.READ.getId())!=0){
				list.add(Permission.READ);
			} 
			if((id & Permission.WRITE.getId())!=0){
				list.add(Permission.WRITE);
			} 
			if((id & Permission.CREATE.getId())!=0){
				list.add(Permission.CREATE);
			} 
			if((id & Permission.DELETE.getId())!=0){
				list.add(Permission.DELETE);
			} 
			if((id & Permission.ADMIN.getId())!=0){
				list.add(Permission.ADMIN);
			}
		}
		return list;
	}
	
	/**
	 * Transfer the Permission List to id.
	 * 
	 * @param permissions
	 * 		the Permission list.
	 * @return
	 * 		the permission id.
	 */
	public static int permissionList2Id(List<Permission> permissions){
		int id = 0;
		if(permissions != null && ! permissions.isEmpty()){
			for (Permission p : permissions) {
				id += p.getId();
			}
		}
		return id;
	}
	
	/**
	 * Transfer the Permission Array to id.
	 * 
	 * @param permissions
	 * 		the Permission Array.
	 * @return
	 * 		the permission id.
	 */
	public static int permissionArray2Id(Permission[] permissions){
		int id = 0;
		if(permissions != null && permissions.length != 0){
			for (Permission p : permissions) {
				id += p.getId();
			}
		}
		return id;
	}
}
