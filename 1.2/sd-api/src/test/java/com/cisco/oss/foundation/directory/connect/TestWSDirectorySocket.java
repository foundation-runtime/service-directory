package com.cisco.oss.foundation.directory.connect;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.annotation.WebServlet;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ModelServiceInstance;
import com.cisco.oss.foundation.directory.entity.OperationalStatus;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.proto.GetServiceProtocol;
import com.cisco.oss.foundation.directory.proto.GetServiceResponse;
import com.cisco.oss.foundation.directory.proto.Protocol;
import com.cisco.oss.foundation.directory.proto.ProtocolHeader;
import com.cisco.oss.foundation.directory.proto.ProtocolType;
import com.cisco.oss.foundation.directory.proto.Response;
import com.cisco.oss.foundation.directory.proto.ResponseHeader;
import com.cisco.oss.foundation.directory.utils.WebSocketSerializer;
import com.cisco.oss.foundation.directory.utils.WebSocketSerializer.ProtocolDeserializer;

public class TestWSDirectorySocket {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(TestWSDirectorySocket.class);
	
	@Test
	public void testInitAndConnect() throws UnknownHostException{
		InetSocketAddress address = new InetSocketAddress(
				InetAddress.getLocalHost(), 8091);

		DirectoryConnection conn = new DirectoryConnection(){
			@Override
			public void onSocketError(){
		    }
		};
		
		WSDirectorySocket sk = new WSDirectorySocket();
		sk.setConnection(conn);
		sk.setConnectTimeOut(1000);
		
		Assert.assertNull(sk.getLocalSocketAddress());
		Assert.assertNull(sk.getRemoteSocketAddress());
		Assert.assertFalse(sk.isConnected());
		
		// When connect failed, it do cleanup.
		
			sk.connect(address);
		Assert.assertNull(sk.getLocalSocketAddress());
		Assert.assertNull(sk.getRemoteSocketAddress());
		Assert.assertFalse(sk.isConnected());
		
		// Test connect
		Server server = null;
		try {
			server = startWSServer(address, new DefaultWebSocketListener());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
//			sk.connect(address);
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Assert.assertNotNull(sk.getLocalSocketAddress());
		Assert.assertNotNull(sk.getRemoteSocketAddress());
		Assert.assertTrue(sk.isConnected());
		
		LOGGER.info("...............cleanup");
		// Test reconnect
		sk.cleanup();
		Assert.assertNull(sk.getLocalSocketAddress());
		Assert.assertNull(sk.getRemoteSocketAddress());
		Assert.assertFalse(sk.isConnected());
		
		LOGGER.info(".................connect");
		sk.connect(address);
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	
		Assert.assertNotNull(sk.getLocalSocketAddress());
		Assert.assertNotNull(sk.getRemoteSocketAddress());
		Assert.assertTrue(sk.isConnected());
		
		try {
			server.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sk.cleanup();
	}
	
	@Test
	public void testSendPacket(){
		InetSocketAddress address = null;
		try {
			address = new InetSocketAddress(
					InetAddress.getLocalHost(), 8091);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Server server = null;
		DefaultWebSocketListener wsSocket = new DefaultWebSocketListener();
		try {
			server = startWSServer(address, wsSocket);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		WSDirectorySocket sk = new WSDirectorySocket();
		CustomerDirectoryConnect conn = new CustomerDirectoryConnect();
		sk.setConnection(conn);
		sk.setConnectTimeOut(4000);
		
			sk.connect(address);
		
		final ProtocolHeader header = new ProtocolHeader(1, ProtocolType.GetService);
		final GetServiceProtocol p = new GetServiceProtocol("mocksvc");
		
		ModelService service = new ModelService("mocksvc", "34");
		List<ModelServiceInstance> serviceInstances = new ArrayList<ModelServiceInstance>();
		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put("dc", "usa");
		ModelServiceInstance instance = new ModelServiceInstance("mocksvc", "127.0.0.1-9080", "0", "http://new.test.com/t", 
				OperationalStatus.UP, metadata);
		serviceInstances.add(instance);
		service.setServiceInstances(serviceInstances);
		final ResponseHeader respHeader = new ResponseHeader(1, 2, ErrorCode.OK);
		final GetServiceResponse response = new GetServiceResponse(service);
		
		wsSocket.setResponse(respHeader, response);
		
		try {
			sk.sendPacket(header, p);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		wsSocket.doProtocolCompare(new ProtocolCompare(){

			@Override
			public boolean comare(ProtocolHeader header, Protocol protocol) {
				Assert.assertEquals(ProtocolType.GetService, header.getType());
				Assert.assertEquals(1, header.getXid());
				Assert.assertEquals("mocksvc", ((GetServiceProtocol) protocol).getServiceName());
				return true;
			}
			
		});
		
		conn.doResponseCompare(new ResponseCompare(){

			@Override
			public boolean compare(ResponseHeader header, Response response) {
				Assert.assertEquals(1, header.getXid());
				Assert.assertEquals(2, header.getDxid());
				Assert.assertEquals(ErrorCode.OK, header.getErr());
				ModelService svc = ((GetServiceResponse) response).getService();
				Assert.assertEquals("mocksvc", svc.getName());
				Assert.assertEquals(1, svc.getServiceInstances().size());
				Assert.assertEquals("usa", svc.getServiceInstances().get(0).getMetadata().get("dc"));
				return true;
			}
			
		});
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			sk.cleanup();
			server.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@WebServlet(name = "API websocket", urlPatterns = { "/ws/service" })
	public static class SocketServlet extends WebSocketServlet {
		DefaultWebSocketListener listener;

		public SocketServlet(DefaultWebSocketListener listener) {
			this.listener = listener;
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void configure(WebSocketServletFactory factory) {
			factory.getPolicy().setIdleTimeout(10000);
			factory.setCreator(new SocketCreator(listener));
		}

	}

	public static class SocketCreator implements WebSocketCreator {

		DefaultWebSocketListener listener;

		public SocketCreator(DefaultWebSocketListener listener) {
			this.listener = listener;
		}

		@Override
		public Object createWebSocket(UpgradeRequest req, UpgradeResponse resp) {
			return listener;
		}

	}

	private static Server startWSServer(InetSocketAddress address,
			DefaultWebSocketListener listener) throws Exception {
		Server server = new Server();
		int httport = address.getPort();
		String httphost = address.getHostName();

		HttpConfiguration httpConfig = new HttpConfiguration();
		ServerConnector http = new ServerConnector(server,
				new HttpConnectionFactory(httpConfig));

		http.setHost(httphost);

		http.setPort(httport);
		server.addConnector(http);
		ResteasyProviderFactory.setInstance(new ResteasyProviderFactory());
		ServletContextHandler context = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		server.setHandler(context);
		context.setContextPath("/");
		ServletHolder holderEvents = new ServletHolder("ws-events",
				new SocketServlet(listener));
		context.addServlet(holderEvents, "/ws/service/*");
		server.start();
		LOGGER.info("Binding to {}:{}", httphost, httport);
		return server;
	}

//	public static void main(String[] args) throws Exception {
//		InetSocketAddress address = new InetSocketAddress(
//				InetAddress.getLocalHost(), 8091);
//		startWSServer(address, new DefaultWebSocketListener());
//
//		WSDirectorySocket sk = new WSDirectorySocket();
//		DirectoryConnection conn = new DirectoryConnection() {
//			@Override
//			public void onSocketError() {
//
//			}
//
//			@Override
//			public void onReceivedPesponse(ResponseHeader header,
//					Response response) {
//
//			}
//		};
//		sk.setConnection(conn);
//		sk.setConnectTimeOut(4000);
//		sk.connect(address);
//		
//		ProtocolHeader header = new ProtocolHeader(1, ProtocolType.GetService);
//		GetServiceProtocol p = new GetServiceProtocol("mocksvc");
//		sk.sendPacket(header, p);
//	}

	public static class DefaultWebSocketListener implements WebSocketListener {

		private Session session;
		
		ProtocolHeader protocolHeader;
		Protocol protocol;
		
		ResponseHeader responseHeader;
		Response response;
		
		public synchronized void doProtocolCompare(ProtocolCompare protocolCompare){
			Assert.assertTrue(protocolCompare.comare(protocolHeader, protocol));
		}
		
		public void setResponse(ResponseHeader responseHeader, Response response){
			this.responseHeader = responseHeader;
			this.response = response;
		}
		@Override
		public void onWebSocketBinary(byte[] payload, int offset, int len) {
			LOGGER.info("WS Server socket receive binary message."); 
		}

		@Override
		public void onWebSocketClose(int statusCode, String reason) {
			LOGGER.info("WS Server socket close connection, statusCode={}, reason={}", statusCode, reason); 
		}

		@Override
		public void onWebSocketConnect(Session session) {
			LOGGER.info("WS Server socket setup connection - " + session.getRemoteAddress()); 
			this.session = session;
		}

		@Override
		public void onWebSocketError(Throwable cause) {
			LOGGER.info("WS Server socket error - " + cause.getMessage()); 

		}

		@Override
		public void onWebSocketText(String message) {
			LOGGER.info("WS Server socket receive message - {}", message); 
			
			try {
				synchronized(this){
					ProtocolDeserializer ds = WebSocketSerializer.getProtocolDeserializer(message);
					protocolHeader = ds.deserializerProtocolHeader();
					protocol = ds.deserializerProtocol();
				}
				LOGGER.info("protocol type={}, protocol={}", protocolHeader.getType(), protocol.getClass().getName());
				
				sendResponse(responseHeader, response);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
		}
		
		public void sendResponse(ResponseHeader header, Response response){
			
			try {
				String s = WebSocketSerializer.getResponseSerializer(header, response).searializerAsString();
				LOGGER.info("WebSocket server send reponse - " + s);
				session.getRemote().sendString(s); 
			} catch (JsonGenerationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}
	
	public class CustomerDirectoryConnect extends DirectoryConnection {
		private ResponseHeader header;
		private Response response;
		@Override
		public void onSocketError() {

		}

		@Override
		public void onReceivedPesponse(ResponseHeader header,
				Response response) {
			this.header = header;
			this.response = response;
		}
		
		public void doResponseCompare(ResponseCompare compare){
			Assert.assertTrue(compare.compare(header, response));
		}
	};
	public interface ProtocolCompare{
		public boolean comare(ProtocolHeader header, Protocol protocol);
	}
	
	public interface ResponseCompare{
		public boolean compare(ResponseHeader header, Response response);
	}
}
