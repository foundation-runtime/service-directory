package com.cisco.oss.foundation.directory.utils;

import java.util.List;

import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.WatcherType;
import com.cisco.oss.foundation.directory.proto.ServiceInstanceOperate;

public class ServiceInstanceOperateUtils {

	public static void executeServiceInstanceOperate(List<ModelServiceInstance> instances, ServiceInstanceOperate operate){
		
		String serviceName = operate.getServiceName();
		String instanceId = operate.getInstanceId();
		ModelServiceInstance instance = operate.getServiceInstance();
		
		switch(operate.getType()){
		case Add:
			instances.add(instance);
			break;
		case Update:
			ModelServiceInstance find = null;
			for(ModelServiceInstance i : instances){
				if(serviceName.equals(i.getServiceName()) && instanceId.equals(i.getInstanceId())){
					find = i;
				}
			}
			if(find != null){
				instances.remove(find);
				instances.add(instance);
			}
			break;
		case Delete:
			find = null;
			for(ModelServiceInstance i : instances){
				if(serviceName.equals(i.getServiceName()) && instanceId.equals(i.getInstanceId())){
					find = i;
				}
			}
			if(find != null){
				instances.remove(find);
			}
			break;
		}
	}
	
	public final static String ServiceWatcherPathPrefix = "/__SERVICE_WATCHER";
	public final static String MetadataWatcherPathPrefix = "/__METADATA_WATCHER";
	public final static String SEP = "/";
	
	public static String getServiceWatcherPath(String serviceName){
		return ServiceWatcherPathPrefix + SEP + serviceName;
	}
	
	public static String getMetadataWatcherPath(String metaName){
		return MetadataWatcherPathPrefix + SEP + metaName;
	}
	
	public static String getPathByWatcherType(String name, WatcherType wactherType){
		if(wactherType.equals(WatcherType.SERVICE)){
			return getServiceWatcherPath(name);
		}else {
			return getMetadataWatcherPath(name);
		}
	}
}
