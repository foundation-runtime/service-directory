/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.ServiceDirectory;
import com.cisco.oss.foundation.directory.async.Callback.AttachSessionCallback;
import com.cisco.oss.foundation.directory.async.Callback.GetServiceCallback;
import com.cisco.oss.foundation.directory.async.Callback.ProtocolCallback;
import com.cisco.oss.foundation.directory.async.Callback.QueryServiceCallback;
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
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.entity.ProvidedServiceInstance;
import com.cisco.oss.foundation.directory.entity.ServiceInstanceToken;
import com.cisco.oss.foundation.directory.entity.User;
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
import com.cisco.oss.foundation.directory.proto.GetServiceProtocol;
import com.cisco.oss.foundation.directory.proto.GetServiceResponse;
import com.cisco.oss.foundation.directory.proto.GetUserProtocol;
import com.cisco.oss.foundation.directory.proto.GetUserResponse;
import com.cisco.oss.foundation.directory.proto.ProtocolHeader;
import com.cisco.oss.foundation.directory.proto.ProtocolType;
import com.cisco.oss.foundation.directory.proto.QueryServiceProtocol;
import com.cisco.oss.foundation.directory.proto.QueryServiceProtocol.QueryCommand;
import com.cisco.oss.foundation.directory.proto.QueryServiceResponse;
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
import com.cisco.oss.foundation.directory.query.StringCommand;
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
	
	/**
	 * The DirectoryServers.
	 */
	private DirectoryServers directoryServers = null;
	
	/**
	 * The DirectoryConnection in the DirectoryServiceClient.
	 */
	private DirectoryConnection connection = null;
	
	/**
	 * The WatcherManager in the DirectoryConnection.
	 */
	WatcherManager watcherManager = new WatcherManager();
	
	/**
	 * Constructor.
	 * 
	 * @param servers
	 * 		the DirectoryServers.
	 * @param userName
	 * 		the user name.
	 * @param password
	 * 		the user password.
	 */
	public DirectoryServiceClient(List<String> servers, String userName, String password) {
		this(servers, userName, password, null);
		
	}
	
	/**
	 * Keep it default for unit test.
	 * 
	 * @param servers
	 * 		the DirectoryServers.
	 * @param userName
	 * 		the user name.
	 * @param password
	 * 		the password.
	 * @param socket
	 * 		the DirectorySocket.
	 */
	DirectoryServiceClient(List<String> servers, String userName, String password, DirectorySocket socket) {
		if(socket == null){
			socket  = getDirectorySocket();
		}
		directoryServers = new DirectoryServers(servers);
		
		connection = new DirectoryConnection(this.directoryServers.getNextDirectoryServer(), watcherManager, socket, userName, password);
		connection.start();
		connection.blockUtilConnected();
		
	}
	
	/**
	 * Set the Directory Server list.
	 * 
	 * @param servers
	 * 		the Directory Server list.
	 */
	public void setDirectoryServers(List<String> servers){
		if(servers == null || servers.size() == 0){
			return;
		}
		directoryServers = new DirectoryServers(servers);
		connection.setDirectoryServers(directoryServers.getNextDirectoryServer());
	}
	
	/**
	 * Get the ConnectionStatus.
	 * 
	 * @return
	 * 		the ConnectionStatus.
	 */
	public ConnectionStatus getStatus(){
		return connection.getStatus();
	}
	
	/**
	 * Set the user for the DirectoryServiceClient.
	 * 
	 * It will ask the DirectoryConnection to do authentication again.
	 * 
	 * @param userName
	 * 		the user name.
	 * @param password
	 * 		the password.
	 */
	public void setUser(String userName, String password){
		if(userName != null && ! userName.isEmpty()){
			connection.setDirectoryUser(userName, password);
		}
	}
	
	/**
	 * Create a User.
	 * 
	 * @param user
	 * 		the User.
	 * @param password
	 * 		the user password.
	 */
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
	
	/**
	 * Set user password.
	 * 
	 * @param userName
	 * 		the user name.
	 * @param password
	 * 		the user password.
	 */
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
	
	/**
	 * Delete user by name.
	 * 
	 * @param userName
	 * 		the user name.
	 */
	public void deleteUser(String userName){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.DeleteUser);
		
		DeleteUserProtocol p = new DeleteUserProtocol(userName);
		
		connection.submitRequest(header, p, null);
	}
	
	/**
	 * Update user.
	 * 
	 * @param user
	 * 		the user.
	 */
	public void updateUser(User user){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.UpdateUser);
		
		UpdateUserProtocol p = new UpdateUserProtocol(user);
		
		connection.submitRequest(header, p, null);
	}
	
	/**
	 * Get all Users.
	 * 
	 * @return
	 * 		the user list.
	 */
	public List<User> getAllUser(){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.GetAllUser);
		
		Response resp = connection.submitRequest(header, null, null);
		return ((GetAllUserResponse) resp).getUsers();
	}
	
	/**
	 * Get user by name.
	 * 
	 * @param name
	 * 		the user name.
	 * @return
	 * 		the User.
	 */
	public User getUser(String name){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.GetUser);
		
		GetUserProtocol p = new GetUserProtocol(name);
		
		Response resp = connection.submitRequest(header, p, null);
		return ((GetUserResponse) resp).getUser();
	}
	
	/**
	 * Set ACL.
	 * 
	 * @param acl
	 * 		the ACL.
	 */
	public void setACL(ACL acl){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.SetACL);
		
		SetACLProtocol p = new SetACLProtocol(acl);
		
		connection.submitRequest(header, p, null);
	}
	
	/**
	 * Get the ACL.
	 * 
	 * @param scheme
	 * 		the AuthScheme.
	 * @param id
	 * 		the identity.
	 * @return
	 * 		the ACL.
	 */
	public ACL getACL(AuthScheme scheme, String id){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.GetACL);
		
		GetACLProtocol p = new GetACLProtocol(scheme, id);
		
		Response resp = connection.submitRequest(header, p, null);
		return ((GetACLResponse) resp).getAcl();
	}
	
	/**
	 * Register a ServiceInstance.
	 * 
	 * @param instance
	 * 		the ProvidedServiceInstance.
	 */
	public void registerServiceInstance(ProvidedServiceInstance instance){
		
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.RegisterServiceInstance);
		
		RegisterServiceInstanceProtocol p = new RegisterServiceInstanceProtocol(instance);
		
		connection.submitRequest(header, p, null);
		
	}
	
	/**
	 * Register ServiceInstance with Callback.
	 * 
	 * @param instance
	 * 		the ProvidedServiceInstance.
	 * @param cb
	 * 		the callback.
	 * @param context
	 * 		the context Object.
	 */
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
	
	/**
	 * Update ServiceInstance status.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param instanceId
	 * 		the instanceId.
	 * @param status
	 * 		the OeperationalStatus.
	 */
	public void updateServiceInstanceStatus(String serviceName, String instanceId, OperationalStatus status){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.UpdateServiceInstanceStatus);
		
		UpdateServiceInstanceStatusProtocol p = new UpdateServiceInstanceStatusProtocol(serviceName, instanceId, status);
		connection.submitRequest(header, p, null);
	}
	
	/**
	 * Update ServiceInstance status with Callback.
	 * 
	 * @param serviceName
	 * 		the servicename.
	 * @param instanceId
	 * 		the instanceId.
	 * @param status
	 * 		the OperationalStaus
	 * @param cb
	 * 		the Callback.
	 * @param context
	 * 		the context object.
	 */
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
	
	/**
	 * Update ServiceInstance internal status.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param instanceId
	 * 		the instanceId.
	 * @param status
	 * 		the OeperationalStatus.
	 */
	public void updateServiceInstanceInternalStatus(String serviceName, String instanceId, OperationalStatus status){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.UpdateServiceInstanceInternalStatus);
		
		UpdateServiceInstanceInternalStatusProtocol protocol = new UpdateServiceInstanceInternalStatusProtocol(serviceName, instanceId, status);
		connection.submitRequest(header, protocol, null);
	}
	
	/**
	 * Update ServiceInstance internal status with Callback.
	 * 
	 * @param serviceName
	 * 		the servicename.
	 * @param instanceId
	 * 		the instanceId.
	 * @param status
	 * 		the OperationalStaus
	 * @param cb
	 * 		the Callback.
	 * @param context
	 * 		the context object.
	 */
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
	
	/**
	 * Attach ServiceInstance to the Session.
	 * 
	 * When create a new session, it need to attach the session again.
	 * 
	 * @param instanceTokens
	 * 		the instance list.
	 * @param cb
	 * 		the callback.
	 * @param context
	 * 		the context object.
	 */
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
	
	/**
	 * Attach ServiceInstance to the Session.
	 * 
	 * @param instanceTokens
	 * 		the instance list.
	 */
	public void attachSession(List<ServiceInstanceToken> instanceTokens){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.AttachSession);
		
		String sessionId = connection.getSessionId();
		AttachSessionProtocol protocol = new AttachSessionProtocol(instanceTokens, sessionId);
		connection.submitRequest(header, protocol, null);
		
	}
	
	/**
	 * Update the ServiceInstance.
	 * 
	 * @param instance
	 * 		the ServiceInstance.
	 */
	public void updateServiceInstance(ProvidedServiceInstance instance){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.UpdateServiceInstance);
		
		UpdateServiceInstanceProtocol p = new UpdateServiceInstanceProtocol(instance);
		connection.submitRequest(header, p, null);
	}
	
	/**
	 * Update the ServiceInstance with callback.
	 * 
	 * @param instance
	 * 		the ServiceInstance.
	 * @param cb
	 * 		the Callback.
	 * @param context
	 * 		the context object.
	 */
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
	
	/**
	 * Update ServiceInstance URI.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param instanceId
	 * 		the instance id.
	 * @param uri
	 * 		the new URI.
	 */
	public void updateServiceInstanceUri(String serviceName, String instanceId, String uri){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.UpdateServiceInstanceUri);
		
		UpdateServiceInstanceUriProtocol p = new UpdateServiceInstanceUriProtocol(serviceName, instanceId, uri);
		connection.submitRequest(header, p, null);
	}
	
	/**
	 * Update ServiceInstance URI in Callback.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param instanceId
	 * 		the instanceId.
	 * @param uri
	 * 		the new URI.
	 * @param cb
	 * 		the Callback.
	 * @param context
	 * 		the object context.
	 */
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
	
	/**
	 * Unregister the ServiceInstance.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param instanceId
	 * 		the instanceId.
	 */
	public void unregisterServiceInstance(String serviceName, String instanceId){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.UnregisterServiceInstance);
		
		UnregisterServiceInstanceProtocol p = new UnregisterServiceInstanceProtocol(serviceName, instanceId);
		connection.submitRequest(header, p, null);
	}
	
	/**
	 * Unregister the ServiceInstance in Callback.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param instanceId
	 * 		the instanceId.
	 * @param cb
	 * 		the Callback.
	 * @param context
	 * 		the Callback context object.
	 */
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
	
	/**
	 * Get the Service.
	 * 
	 * @param serviceName
	 * 		the serviceName.
	 * @param watcher
	 * 		the Watcher.
	 * @return
	 * 		the ModelService.
	 */
	public ModelService getService(String serviceName, Watcher watcher){
		WatcherRegistration wcb = null;
        if (watcher != null) {
            wcb = new WatcherRegistration(serviceName, watcher);
        }
        
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.GetService);
		
		GetServiceProtocol p = new GetServiceProtocol(serviceName);
		p.setWatcher(watcher != null);
		GetServiceResponse resp ;
		resp = (GetServiceResponse) connection.submitRequest(header, p,  wcb);
		return resp.getService();
	}
	
	/**
	 * Get Service in Callback.
	 * 
	 * @param serviceName
	 * 		the serviceName.
	 * @param cb
	 * 		the Callback.
	 * @param context
	 * 		the callback context object.
	 */
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
	
	/**
	 * Asynchronized get Service.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param watcher
	 * 		the watcher.
	 * @return
	 * 		the ServiceDirectoryFuture
	 */
	public ServiceDirectoryFuture asyncGetService(String serviceName, Watcher watcher){
		WatcherRegistration wcb = null;
        if (watcher != null) {
            wcb = new WatcherRegistration(serviceName, watcher);
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
	
	/**
	 * Validate whether the watcher registered.
	 * 
	 * @param serviceName
	 * 		the serviceName.
	 * @param watcher
	 * 		the watcher.
	 * @return
	 * 		true for success.
	 */
	public boolean validateServiceWatcher(String serviceName, Watcher watcher){
		return watcherManager.validateWatcher(serviceName, watcher);
	}
	
	/**
	 * delete the watcher.
	 * 
	 * @param serviceName
	 * 		the service name.
	 * @param watcher
	 * 		the watcher.
	 */
	public void deleteServiceWatcher(String serviceName, Watcher watcher){
		watcherManager.deleteWatcher(serviceName, watcher);
	}
	
	/**
	 * Clean the watchers oh the service.
	 * 
	 * @param serviceName
	 * 		the service name.
	 */
	public void cleanServiceWatchers(String serviceName){
		watcherManager.cleanWatchers(serviceName);
	}
	
	/**
	 * Query Service.
	 * 
	 * it is the synchronized method.
	 * 
	 * @param commands
	 * 		the StringCommand list.
	 * @return
	 * 		the ModelServiceInstance list.
	 */
	public List<ModelServiceInstance> queryService(List<StringCommand> commands){
        
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.QueryService);
		
		List<QueryCommand> cs = new ArrayList<QueryCommand>();
		for(StringCommand c : commands){
			cs.add(c.getStringCommand());
		}
		
		QueryServiceProtocol p = new QueryServiceProtocol(cs);
		QueryServiceResponse resp ;
		resp = (QueryServiceResponse) connection.submitRequest(header, p,  null);
		return resp.getInstances();
	}
	
	/**
	 * Query service in Callback.
	 * 
	 * @param commands
	 * 		the StringCommand list.
	 * @param cb
	 * 		the Callback.
	 * @param context
	 * 		the Callback context object.
	 */
	public void queryService(List<StringCommand> commands, final QueryServiceCallback cb, Object context){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.QueryService);
		
		List<QueryCommand> cs = new ArrayList<QueryCommand>();
		for(StringCommand c : commands){
			cs.add(c.getStringCommand());
		}
		
		QueryServiceProtocol p = new QueryServiceProtocol(cs);
		
		ProtocolCallback pcb = new ProtocolCallback(){

			@Override
			public void call(boolean result, Response response,
					ErrorCode error, Object ctx) {
				List<ModelServiceInstance> rsp = null;
				if(response != null){
					rsp = ((QueryServiceResponse) response).getInstances();
				}
				cb.call(result, rsp, error, ctx);
			}
			
		};
		
		connection.submitCallbackRequest(header, p, pcb, context);
	}
	
	/**
	 * Asynchronized query service.
	 * 
	 * @param commands
	 * 		the StringCommand list.
	 * @return
	 * 		the ServiceDirectoryFuture.
	 */
	public ServiceDirectoryFuture asyncQueryService(List<StringCommand> commands){
		ProtocolHeader header = new ProtocolHeader();
		header.setType(ProtocolType.QueryService);
		
		List<QueryCommand> cs = new ArrayList<QueryCommand>();
		for(StringCommand c : commands){
			cs.add(c.getStringCommand());
		}
		
		QueryServiceProtocol p = new QueryServiceProtocol(cs);
		return connection.submitAsyncRequest(header, p, null);
	}
	
	/**
	 * Register the ServiceDirectoryListener.
	 * 
	 * @param listener
	 * 		the ServiceDirectoryListener.
	 */
	public void registerClientChangeListener(ServiceDirectoryListener listener){
		connection.registerClientChangeListener(listener);
    }
    
	/**
	 * Unregister the ServiceDirectoryListener.
	 * 
	 * @param listener
	 * 		the ServiceDirectoryListener.
	 */
    public void unregisterClientChangeListener(ServiceDirectoryListener listener){
    	connection.unregisterClientChangeListener(listener);
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
	
	/**
	 * Get the DirectorySoeckt.
	 * 
	 * @return
	 * 		the DirectorySocket.
	 */
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
	
	/**
	 * The WatcherRegistration for the Service.
	 * 
	 * @author zuxiang
	 *
	 */
	public static class WatcherRegistration {
		/**
		 * The service name.
		 */
        private String name;
        
        /**
         * The service Watcher.
         */
        private Watcher watcher;
        
        /**
         * The Constructor.
         * 
         * @param name
         * 		the service name.
         * @param watcher
         * 		the service Watcher.
         */
        public WatcherRegistration(String name, Watcher watcher)
        {
            this.setName(name);
            this.watcher = watcher;
        }

        /**
         * Get the Watcher.
         * 
         * @return
         * 		the Service Watcher.
         */
		public Watcher getWatcher(){
			return this.watcher;
		}

		/**
		 * Get ServiceName.
		 * @return
		 * 		the service name.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Set the Service name.
		 * @param name
		 * 		the service name.
		 */
		public void setName(String name) {
			this.name = name;
		}
    }

	
}
