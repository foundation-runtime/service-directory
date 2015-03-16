/**
 * Copyright 2014 Cisco Systems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cisco.oss.foundation.directory.utils;

import java.util.ArrayList;
import java.util.List;

import com.cisco.oss.foundation.directory.entity.Permission;

/**
 * The permission tranfer utils.
 *
 *
 */
public class PermissionUtil {

    /**
     * Transfer the permission id to Permission list.
     *
     * @param id
     *         the permission id.
     * @return
     *         the Permission list.
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
     *         the Permission list.
     * @return
     *         the permission id.
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
     *         the Permission Array.
     * @return
     *         the permission id.
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
