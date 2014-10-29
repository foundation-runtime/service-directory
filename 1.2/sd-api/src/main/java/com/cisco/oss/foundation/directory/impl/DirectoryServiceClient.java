package com.cisco.oss.foundation.directory.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.ServiceDirectory;
import com.cisco.oss.foundation.directory.async.Callback.AttachSessionCallback;
import com.cisco.oss.foundation.directory.async.Callback.GetMetadataCallback;
import com.cisco.oss.foundation.directory.async.Callback.GetServiceCallback;
import com.cisco.oss.foundation.directory.async.Callback.ProtocolCallback;
import com.cisco.oss.foundation.directory.async.Callback.RegistrationCallback;
import com.cisco.oss.foundation.directory.async.ServiceDirectoryFuture;
import com.cisco.oss.foundation.directory.async.Watcher;
import com.cisco.oss.foundation.directory.async.WatcherManager;
import com.cisco.oss.foundation.directory.connect.DirectoryConnection;
import com.cisco.oss.foundation.directory.connect.DirectoryServers;
import com.cisco.oss.foundation.directory.connect.DirectorySocket;
import com.cisco.oss.foundation.directory.connect.WSDirectorySocket;
import com.cisco.oss.foundation.directory.entity.ACL;
import com.cisco.oss.foundation.directory.entity.AuthScheme;
import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstanceToken;
import com.cisco.oss.foundation.directory.entity.User;
import com.cisco.oss.foundation.directory.entity.WatcherType;
import com.cisco.oss.foundation.directory.event.ConnectionStatus;
import com.cisco.oss.foundation.directory.event.ServiceDirectoryListener;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.proto.AttachSessionProtocol;
import com.cisco.oss.foundation.directory.proto.AttachSessionResponse;
import com.cisco.oss.foundation.directory.proto.AttachSessionResponse.ItemResult;
import com.cisco.oss.foundation.directory.proto.CreateUserProtocol;
import com.cisco.oss.foundation.directory.proto.DeleteUserProtocol;
import com.cisco.oss.foundation.directory.proto.GetACLProtocol;
import com.cisco.oss.foundation.directory.proto.GetACLResponse;
import com.cisco.oss.foundation.directory.proto.GetAllServicesResponse;
import com.cisco.oss.foundation.directory.proto.GetAllUserResponse;
import com.cisco.oss.foundation.directory.proto.GetMetadataProtocol;
import com.cisco.oss.foundation.directory.proto.GetMetadataResponse;
import com.cisco.oss.foundation.directory.proto.GetServiceProtocol;
import com.cisco.oss.foundation.directory.proto.GetServiceResponse;
import com.cisco.oss.foundation.directory.proto.GetUserProtocol;
import com.cisco.oss.foundation.directory.proto.GetUserResponse;
import com.cisco.oss.foundation.directory.proto.ProtocolHeader;
import com.cisco.oss.foundation.directory.proto.ProtocolType;
import com.cisco.oss.foundation.directory.proto.RegisterServiceInstanceProtocol;
import com.cisco.oss.foundation.directory.proto.Response;
import com.cisco.oss.foundation.directory.proto.SetACLProtocol;
import com.cisco.oss.foundation.directory.proto.SetUserPasswordProtocol;
import com.cisco.oss.foundation.directory.proto.UnregisterServiceInstanceProtocol;
import com.cisco.oss.foundation.directory.proto.UpdateServiceInstanceInternalStatusProtocol;
import com.cisco.oss.foundation.directory.proto.UpdateServiceInstanceProtocol;
import com.cisco.oss.foundation.directory.proto.UpdateServiceInstanceStatusProtocol;
import com.cisco.oss.foundation.directory.proto.UpdateServiceInstanceUriProtocol;
import com.cisco.oss.foundation.directory.proto.UpdateUserProtocol;
import com.cisco.oss.foundation.directory.utils.ObfuscatUtil;

public class DirectoryServiceClient {

	private final static Logger LOGGER = LoggerFactory.getLogger(DirectoryServiceClient.class);
	
	/**
	 * The Service Directory server FQDN property name.
	 */
	public static final String SD_API_SD_SERVER_FQDN_PROPERTY = "server.fqdn";
	
	/**
	 * The default Service Directory server FQDN name.
	 */
	public static final String SD_API_SD_SERVER_FQDN_DEFAULT = "vcsdirsvc";
	
	/**
	 * The Service Directory server port property name.
	 */
	public static final String SD_API_SD_SERVER_PORT_PROPERTY = "server.port";
	
	/**
	 * The Service Directory client DirectorySocket implementation property name.
	 */
	public static final String SD_API_DIRECTORY_SOCKET_PROVIDER_PROPERTY = "directory.socket.provider";
	
	/**
	 * The default Service Directory server port. 
	 */
	public static final int SD_API_SD_SERVER_PORT_DEFAULT = 2013;
	
	private DirectoryServers directoryServers = null;
	
	private DirectoryConnection connection = null;
	
	WatcherManager watcherManager = new WatcherManager();
	
	public DirectoryServiceClient(List<String> servers, String userName, String password) {
		this(servers, userName, password, null);
		
	}
	
	/**
	 * Keep it default for unit test.
	 * 
	 * @param servers
	 * @param userName
	 * @param password
	 * @param socket
	 */
	DirectoryServiceClient(List<String> servers, String userName, String password, DirectorySocket socket) {
		if(socket == null){
			socket  = getDirectorySocket();
		}
		directoryServers = new DirectoryServers(servers);
		
		connection = new DirectoryConnection(this.directoryServers, watcherManager, socket, userName, password);
		connection.start();
		connection.blockUtilConnected();
		
	}
	
	public void setDirectoryServers(List<String> servers){
		if(servers == null || servers.size() == 0){
			return;
		}
		directoryServers = new DirectoryServers(servers);
		connection.setDirectoryServers(directoryServers);
	}
	
	public ConnectionStatus getStatus(){
		return connection.getStatus();
	}
	
	public void setUser(String userName, String password){
		if(userName != null && ! userName.isEmpty()){
			connection.setDirectoryUser(userName, password);
		}
	}
	
	public void createUser(User user, String password){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.CreateUser);
		byte[] secret = null;
		if(password != null && ! password.isEmpty()){
			secret = ObfuscatUtil.base64Encode(password.getBytes());
		}
		CreateUserProtocol p = new CreateUserProtocol(user, secret);
		
		connection.submitRequest(header, p, null);
	}
	
	public void setUserPassword(String userName, String password) {
		
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.SetUserPassword);
		byte[] secret = null;
		if(password != null && ! password.isEmpty()){
			secret = ObfuscatUtil.base64Encode(password.getBytes());
		}
		SetUserPasswordProtocol p = new SetUserPasswordProtocol(userName, secret);
		
		connection.submitRequest(header, p, null);
		
	}
	
	public void deleteUser(String userName){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.DeleteUser);
		
		DeleteUserProtocol p = new DeleteUserProtocol(userName);
		
		connection.submitRequest(header, p, null);
	}
	
	public void updateUser(User user){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.UpdateUser);
		
		UpdateUserProtocol p = new UpdateUserProtocol(user);
		
		connection.submitRequest(header, p, null);
	}
	
	public List<User> getAllUser(){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.GetAllUser);
		
		Response resp = connection.submitRequest(header, null, null);
		return ((GetAllUserResponse) resp).getUsers();
	}
	
	public User getUser(String name){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.GetUser);
		
		GetUserProtocol p = new GetUserProtocol(name);
		
		Response resp = connection.submitRequest(header, p, null);
		return ((GetUserResponse) resp).getUser();
	}
	
	public void setACL(ACL acl){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.SetACL);
		
		SetACLProtocol p = new SetACLProtocol(acl);
		
		connection.submitRequest(header, p, null);
	}
	
	public ACL getACL(AuthScheme scheme, String id){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.GetACL);
		
		GetACLProtocol p = new GetACLProtocol(scheme, id);
		
		Response resp = connection.submitRequest(header, p, null);
		return ((GetACLResponse) resp).getAcl();
	}
	
	public void registerServiceInstance(ProvidedServiceInstance instance){
		
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.RegisterServiceInstance);
		
		RegisterServiceInstanceProtocol p = new RegisterServiceInstanceProtocol(instance);
		
		connection.submitRequest(header, p, null);
		
	}
	
	public void registerServiceInstance(ProvidedServiceInstance instance, final RegistrationCallback cb, Object context){
		
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.RegisterServiceInstance);
		
		RegisterServiceInstanceProtocol p = new RegisterServiceInstanceProtocol(instance);
		
		ProtocolCallback pcb = new ProtocolCallback(){

			@Override
			public void call(boolean result, Response response,
					ErrorCode error, Object ctx) {
				cb.call(result, error, ctx);
			}
			
		};
		
		connection.submitCallbackRequest(header, p, pcb, context);
		
	}
	
	
	
	public void updateServiceInstanceStatus(String serviceName, String instanceId, OperationalStatus status){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.UpdateServiceInstanceStatus);
		
		UpdateServiceInstanceStatusProtocol p = new UpdateServiceInstanceStatusProtocol(serviceName, instanceId, status);
		connection.submitRequest(header, p, null);
	}
	
	public void updateServiceInstanceStatus(String serviceName, String instanceId, OperationalStatus status, final RegistrationCallback cb, Object context){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.UpdateServiceInstanceStatus);
		
		UpdateServiceInstanceStatusProtocol p = new UpdateServiceInstanceStatusProtocol(serviceName, instanceId, status);
		
		ProtocolCallback pcb = new ProtocolCallback(){

			@Override
			public void call(boolean result, Response response,
					ErrorCode error, Object ctx) {
				cb.call(result, error, ctx);
			}
			
		};
		
		connection.submitCallbackRequest(header, p, pcb, context);
	}
	
	public void updateServiceInstanceInternalStatus(String serviceName, String instanceId, OperationalStatus status){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.UpdateServiceInstanceInternalStatus);
		
		UpdateServiceInstanceInternalStatusProtocol protocol = new UpdateServiceInstanceInternalStatusProtocol(serviceName, instanceId, status);
		connection.submitRequest(header, protocol, null);
	}
	
	public void updateServiceInstanceInternalStatus(String serviceName, String instanceId, OperationalStatus status, final RegistrationCallback cb, Object context){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.UpdateServiceInstanceInternalStatus);
		
		UpdateServiceInstanceInternalStatusProtocol protocol = new UpdateServiceInstanceInternalStatusProtocol(serviceName, instanceId, status);
		
		ProtocolCallback pcb = new ProtocolCallback(){

			@Override
			public void call(boolean result, Response response,
					ErrorCode error, Object ctx) {
				cb.call(result, error, ctx);
			}
			
		};
		
		connection.submitCallbackRequest(header, protocol, pcb, context);
	}
	
	public void attachSession(List<ServiceInstanceToken> instanceTokens, final AttachSessionCallback cb, Object context){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.AttachSession);
		
		String sessionId = connection.getSessionId();
		AttachSessionProtocol protocol = new AttachSessionProtocol(instanceTokens, sessionId);
		ProtocolCallback pcb = new ProtocolCallback(){

			@Override
			public void call(boolean result, Response response,
					ErrorCode error, Object ctx) {
				Map<ServiceInstanceToken, ItemResult> items = ((AttachSessionResponse)response).getAttachingResult();
				cb.call(result, items, error, ctx);
			}
			
		};
		
		connection.submitCallbackRequest(header, protocol, pcb, context);
	}
	
	public Map<ServiceInstanceToken, ItemResult> attachSession(List<ServiceInstanceToken> instanceTokens){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.AttachSession);
		
		String sessionId = connection.getSessionId();
		AttachSessionProtocol protocol = new AttachSessionProtocol(instanceTokens, sessionId);
		AttachSessionResponse resp = (AttachSessionResponse) connection.submitRequest(header, protocol, null);
		return resp.getAttachingResult();
	}
	
	public void updateServiceInstance(ProvidedServiceInstance instance){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.UpdateServiceInstance);
		
		UpdateServiceInstanceProtocol p = new UpdateServiceInstanceProtocol(instance);
		connection.submitRequest(header, p, null);
	}
	
	
	public void updateServiceInstance(ProvidedServiceInstance instance, final RegistrationCallback cb, Object context){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.UpdateServiceInstance);
		
		UpdateServiceInstanceProtocol p = new UpdateServiceInstanceProtocol(instance);
		ProtocolCallback pcb = new ProtocolCallback(){

			@Override
			public void call(boolean result, Response response,
					ErrorCode error, Object ctx) {
				cb.call(result, error, ctx);
			}
			
		};
		connection.submitCallbackRequest(header, p, pcb, context);
	}
	
	public void updateServiceInstanceUri(String serviceName, String instanceId, String uri){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.UpdateServiceInstanceUri);
		
		UpdateServiceInstanceUriProtocol p = new UpdateServiceInstanceUriProtocol(serviceName, instanceId, uri);
		connection.submitRequest(header, p, null);
	}
	
	public void updateServiceInstanceUri(String serviceName, String instanceId, String uri, final RegistrationCallback cb, Object context){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.UpdateServiceInstanceUri);
		
		UpdateServiceInstanceUriProtocol p = new UpdateServiceInstanceUriProtocol(serviceName, instanceId, uri);
		ProtocolCallback pcb = new ProtocolCallback(){

			@Override
			public void call(boolean result, Response response,
					ErrorCode error, Object ctx) {
				cb.call(result, error, ctx);
			}
			
		};
		connection.submitCallbackRequest(header, p, pcb, context);
	}
	
//	public void updateServiceInstancesHeartbeat(List<ServiceInstanceHeartbeat> instances, long time){
//		ProtocolHeader header = new ProtocolHeader();
//		header.setType(ProtocolType.UpdateHeartbeat);
//		
//		UpdateHeartbeatProtocol p = new UpdateHeartbeatProtocol(instances, time);
//		connection.submitRequest(header, p, null);
//	}
//	
//	public void updateServiceInstancesHeartbeat(List<ServiceInstanceHeartbeat> instances, long time, final RegistrationCallback cb, Object context){
//		ProtocolHeader header = new ProtocolHeader();
//		header.setType(ProtocolType.UpdateHeartbeat);
//		
//		UpdateHeartbeatProtocol p = new UpdateHeartbeatProtocol(instances, time);
//		ProtocolCallback pcb = new ProtocolCallback(){
//
//			@Override
//			public void call(boolean result, Response response,
//					ErrorCode error, Object ctx) {
//				cb.call(result, error, ctx);
//			}
//			
//		};
//		connection.submitCallbackRequest(header, p, pcb, context);
//	}
	
	public void unregisterServiceInstance(String serviceName, String instanceId){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.UnregisterServiceInstance);
		
		UnregisterServiceInstanceProtocol p = new UnregisterServiceInstanceProtocol(serviceName, instanceId);
		connection.submitRequest(header, p, null);
	}
	
	public void unregisterServiceInstance(String serviceName, String instanceId, final RegistrationCallback cb, Object context){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.UnregisterServiceInstance);
		
		UnregisterServiceInstanceProtocol p = new UnregisterServiceInstanceProtocol(serviceName, instanceId);
		ProtocolCallback pcb = new ProtocolCallback(){

			@Override
			public void call(boolean result, Response response,
					ErrorCode error, Object ctx) {
				cb.call(result, error, ctx);
			}
			
		};
		connection.submitCallbackRequest(header, p, pcb, context);
	}
	
	public ModelService getService(String serviceName, Watcher watcher){
		WatcherRegistration wcb = null;
        if (watcher != null) {
            wcb = new WatcherRegistration(serviceName, watcher, WatcherType.SERVICE);
        }
        
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.GetService);
		
		GetServiceProtocol p = new GetServiceProtocol(serviceName);
		p.setWatcher(watcher != null);
		GetServiceResponse resp ;
		resp = (GetServiceResponse) connection.submitRequest(header, p,  wcb);
		return resp.getService();
	}
	
	public void getService(String serviceName, final GetServiceCallback cb, Object context){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.GetService);
		
		GetServiceProtocol p = new GetServiceProtocol(serviceName);
		p.setWatcher(false);
		
		ProtocolCallback pcb = new ProtocolCallback(){

			@Override
			public void call(boolean result, Response response,
					ErrorCode error, Object ctx) {
				ModelService rsp = null;
				if(response != null){
					rsp = ((GetServiceResponse) response).getService();
				}
				cb.call(result, rsp, error, ctx);
			}
			
		};
		
		connection.submitCallbackRequest(header, p,  pcb, context);
	}
	
	public ServiceDirectoryFuture asyncGetService(String serviceName, Watcher watcher){
		WatcherRegistration wcb = null;
        if (watcher != null) {
            wcb = new WatcherRegistration(serviceName, watcher, WatcherType.SERVICE);
        }
        
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.GetService);
		
		GetServiceProtocol p = new GetServiceProtocol(serviceName);
		p.setWatcher(false);
		return connection.submitAsyncRequest(header, p,  wcb);
	}
	
	/**
	 * Get All ModelServiceInstance in the ServiceDirectory.
	 * 
	 * @return
	 * 		the ModelServiceInstance List.
	 */
	public List<ModelServiceInstance> getAllInstances(){
        
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.GetAllServices);
		GetAllServicesResponse resp = (GetAllServicesResponse) connection.submitRequest(header, null,  null);
		return resp.getInstances();
	}
	
	public boolean validateServiceWatcher(String serviceName, Watcher watcher){
		return watcherManager.validateWatcher(serviceName, WatcherType.SERVICE, watcher);
	}
	
	public void deleteServiceWatcher(String serviceName, Watcher watcher){
		watcherManager.deleteWatcher(serviceName, WatcherType.SERVICE, watcher);
	}
	
	public void cleanServiceWatchers(String serviceName){
		watcherManager.cleanWatchers(serviceName, WatcherType.SERVICE);
	}
	
	public ModelMetadataKey getMetadata(String keyName, Watcher watcher){
		WatcherRegistration wcb = null;
        if (watcher != null) {
            wcb = new WatcherRegistration(keyName, watcher, WatcherType.METADATA);
        }
        
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.GetMetadata);
		
		GetMetadataProtocol p = new GetMetadataProtocol(keyName);
		p.setWatcher(watcher != null);
		GetMetadataResponse resp ;
		resp = (GetMetadataResponse) connection.submitRequest(header, p,  wcb);
		return resp.getMetadata();
	}
	
	public void getMetadata(String keyName, final GetMetadataCallback cb, Object context){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.GetMetadata);
		
		GetMetadataProtocol p = new GetMetadataProtocol(keyName);
		
		ProtocolCallback pcb = new ProtocolCallback(){

			@Override
			public void call(boolean result, Response response,
					ErrorCode error, Object ctx) {
				ModelMetadataKey rsp = null;
				if(response != null){
					rsp = ((GetMetadataResponse) response).getMetadata();
				}
				cb.call(result, rsp, error, ctx);
			}
			
		};
		
		connection.submitCallbackRequest(header, p, pcb, context);
	}
	
	public ServiceDirectoryFuture asyncGetMetadata(String keyName, Watcher watcher){
		WatcherRegistration wcb = null;
        if (watcher != null) {
            wcb = new WatcherRegistration(keyName, watcher, WatcherType.METADATA);
        }
        ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.GetMetadata);
		
		GetMetadataProtocol p = new GetMetadataProtocol(keyName);
		p.setWatcher(watcher != null);
		return connection.submitAsyncRequest(header, p, wcb);
	}
	
	public void registerClientChangeListener(ServiceDirectoryListener listener){
		connection.registerClientChangeListener(listener);
    }
    
    public void unregisterClientChangeListener(ServiceDirectoryListener listener){
    	connection.unregisterClientChangeListener(listener);
    }
	
	public boolean validateMetadataKeyWatcher(String keyName, Watcher watcher){
		return watcherManager.validateWatcher(keyName, WatcherType.METADATA, watcher);
	}
	public void deleteMetadataKeyWatcher(String keyName, Watcher watcher){
		watcherManager.deleteWatcher(keyName, WatcherType.METADATA, watcher);
	}
	
	public void cleanMetadataKeyWatchers(String keyName){
		watcherManager.cleanWatchers(keyName, WatcherType.METADATA);
	}
	
	/**
	 * Close the DirectoryServiceClient.
	 */
	public void close(){
		if(connection != null){
			try {
				connection.close();
			} catch (IOException e) {
				LOGGER.warn("Close the DirectoryConnection get exception - " + e.getMessage());
			}
			connection = null;
		}
		if(watcherManager != null){
			watcherManager.cleanup();
			watcherManager = null;
		}
	}
	
	private DirectorySocket getDirectorySocket(){
		DirectorySocket socket = null;
		if(ServiceDirectory.getServiceDirectoryConfig().containsProperty(SD_API_DIRECTORY_SOCKET_PROVIDER_PROPERTY)){
			String provider = ServiceDirectory.getServiceDirectoryConfig().getString(SD_API_DIRECTORY_SOCKET_PROVIDER_PROPERTY);
			try{
				Class<?> provideClass = Class.forName(provider);
				
				if (DirectorySocket.class
						.isAssignableFrom(provideClass)) {
					socket = (DirectorySocket) provideClass.newInstance();
				}
			}catch(Exception e){
				LOGGER.warn("fail to initialize the DirectorySocket provider - " + provider, e);
			}
		}
		if(socket == null){
			return new WSDirectorySocket();
		}else {
			return socket;
		}
	}
	public static class WatcherRegistration {
        private String name;
        private Watcher watcher;
        private WatcherType watcherType = WatcherType.SERVICE; 
        public WatcherRegistration(String name, Watcher watcher, WatcherType watcherType)
        {
            this.setName(name);
            this.watcher = watcher;
            this.watcherType = watcherType;
        }

		public Watcher getWatcher(){
			return this.watcher;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public WatcherType getWatcherType() {
			return watcherType;
		}
    }

	
}
