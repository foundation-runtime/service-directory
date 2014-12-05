/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.utils;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.cisco.oss.foundation.directory.proto.Protocol;
import com.cisco.oss.foundation.directory.proto.ProtocolHeader;
import com.cisco.oss.foundation.directory.proto.Response;
import com.cisco.oss.foundation.directory.proto.ResponseHeader;

/**
 * Serialize and deserialize utils for the Websocket JSON message.
 * 
 * @author zuxiang
 *
 */
public class WebSocketSerializer {
	/**
	 * The Jackson ObjectMapper.
	 */
	private static final ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Serialize the Response.
	 * 
	 * @param header
	 * 		the ResponseHeader.
	 * @param response
	 * 		the Response.
	 * @return
	 * 		the ResponseSerializer.
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static ResponseSerializer getResponseSerializer(ResponseHeader header, Response response) throws JsonGenerationException, JsonMappingException, IOException{
		final String s = serializeResponse(header, response);
		return new ResponseSerializer(){

			@Override
			public String searializerAsString() {
				return s;
			}
			
		};
	}
	
	/**
	 * Serialize the Protocol.
	 * 
	 * @param header
	 * 		the ProtocolHeader.
	 * @param protocol
	 * 		the Protocol.
	 * @return
	 * 		the ProtocolSerializer.
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static ProtocolSerializer getProtocolSerializer(ProtocolHeader header, Protocol protocol) throws JsonGenerationException, JsonMappingException, IOException{
		final String s = serializeProtocol(header, protocol);
		return new ProtocolSerializer(){

			@Override
			public String serializerAsString() {
				return s;
			}
			
		};
	}
	
	/**
	 * Deserialize the Protocol Json String.
	 * 
	 * @param jsonStr
	 * 		the Protocol Json String
	 * @return
	 * 		the ProtocolDeserializer.
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static ProtocolDeserializer getProtocolDeserializer(String jsonStr) throws JsonParseException, JsonMappingException, IOException{
		final ProtocolPair p = mapper.readValue(jsonStr, ProtocolPair.class);
		Protocol pp = null;
		if(p.getType() != null){
			pp = (Protocol) mapper.readValue(p.getProtocol(), p.getType());
		}
		final Protocol resp = pp;
		
		return new ProtocolDeserializer(){

			@Override
			public ProtocolHeader deserializerProtocolHeader() {
				return p.getProtocolHeader();
			}

			@Override
			public Protocol deserializerProtocol() {
				return resp;
			}
			
		};
	}
	
	/**
	 * Deserialize the Response Json String.
	 * 
	 * @param jsonStr
	 * 		The Response Json String.
	 * @return
	 * 		the ResponseDeserializer.
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static ResponseDeserializer getResponseDeserializer(String jsonStr) throws JsonParseException, JsonMappingException, IOException{
		final ResponsePair p = mapper.readValue(jsonStr, ResponsePair.class);
		Response rr = null;
		if(p.getType() != null){
			rr = (Response) mapper.readValue(p.getResponse(), p.getType());
		}
		final Response resp = rr;
		
		return new ResponseDeserializer(){

			@Override
			public ResponseHeader deserializerResponseHeader() {
				return p.getResponseHeader();
			}

			@Override
			public Response deserializerResponse() {
				return resp;
			}
			
		};
		
	}
	
	/**
	 * Serialize the Response.
	 * 
	 * @param header
	 * 		the ResponseHeader.
	 * @param response
	 * 		the Response.
	 * @return
	 * 		the JSON String.
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private static String serializeResponse(ResponseHeader header, Response response) throws JsonGenerationException, JsonMappingException, IOException{
		ResponsePair p = new ResponsePair();
		p.setResponseHeader(header);
		if(response == null){
			p.setType(null);
		}else{
			p.setType(response.getClass());
		}
		p.setResponse(toJsonStr(response));
		return toJsonStr(p);
	}
	
	/**
	 * Serialize the Protocol to JSON String.
	 * 
	 * @param header
	 * 		the ProtocolHeader.
	 * @param protocol
	 * 		the Protocol.
	 * @return
	 * 		the JSON String.
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private static String serializeProtocol(ProtocolHeader header, Protocol protocol) throws JsonGenerationException, JsonMappingException, IOException{
		ProtocolPair p = new ProtocolPair();
		p.setProtocolHeader(header);
		if(protocol == null){
			p.setType(null);
		} else {
			p.setType(protocol.getClass());
		}
		p.setProtocol(toJsonStr(protocol));
		return toJsonStr(p);
	}
	
	/**
	 * Transfer Object to JSON String.
	 * 
	 * @param object
	 * 		the target Object.
	 * @return
	 * 		JSON String.
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private static String toJsonStr(Object object) throws JsonGenerationException, JsonMappingException, IOException{
		return mapper.writeValueAsString(object);
	}
	
	/**
	 * The Response Serializer.
	 * 
	 * @author zuxiang
	 *
	 */
	public interface ResponseSerializer{
		/**
		 * serialize Response to JSON String.
		 * 
		 * @return
		 * 		the JSON String.
		 */
		public String searializerAsString();
	}
	
	/**
	 * The Response Deserializer.
	 * 
	 * @author zuxiang
	 *
	 */
	public interface ResponseDeserializer{
		/**
		 * Deserialize the ResponseHeader.
		 * 
		 * @return
		 * 		the ResponseHeader.
		 */
		public ResponseHeader deserializerResponseHeader();
		
		/**
		 * Deserialize the Response.
		 * 
		 * @return
		 * 		the Response.
		 */
		public Response deserializerResponse();
	}
	
	/**
	 * The Protocol Serializer.
	 * @author zuxiang
	 *
	 */
	public interface ProtocolSerializer{
		
		/**
		 * Serialize the Protocol to JSON String.
		 * 
		 * @return
		 * 		the JSON String.
		 */
		public String serializerAsString();
	}
	
	/**
	 * The Protocol deserializer.
	 * 
	 * @author zuxiang
	 *
	 */
	public interface ProtocolDeserializer{
		/**
		 * Deserialize the ProtocolHeader.
		 * 
		 * @return
		 * 		the ProtocolHeader.
		 */
		public ProtocolHeader deserializerProtocolHeader();
		
		/**
		 * Deserialze the Protocol.
		 * 
		 * @return
		 * 		the Protocol.
		 */
		public Protocol deserializerProtocol();
	}
	
	/**
	 * The Response Pair to store Response.
	 * 
	 * @author zuxiang
	 *
	 */
	static class ResponsePair{
		/**
		 * The ResponseHeader.
		 */
		ResponseHeader responseHeader;
		
		/**
		 * THe Response.
		 */
		String response;
		
		/**
		 * The Response class Type.
		 */
		Class<? extends Response> type;
		public ResponsePair(){
			
		}
		
		public void setType(Class<? extends Response> type){
			this.type = type;
		}
		
		public Class<? extends Response> getType(){
			return this.type;
		}
		
		public ResponseHeader getResponseHeader() {
			return responseHeader;
		}
		public void setResponseHeader(ResponseHeader responseHeader) {
			this.responseHeader = responseHeader;
		}
		public String getResponse() {
			return response;
		}
		public void setResponse(String response) {
			this.response = response;
		}
		
	}
	
	/**
	 * The Protocol Pair to store Protocol.
	 * 
	 * @author zuxiang
	 *
	 */
	static class ProtocolPair{
		/**
		 * The ProtocolType.
		 */
		ProtocolHeader protocolHeader;
		
		/**
		 * THe Protocol JSON String.
		 */
		String protocol;
		
		/**
		 * The Protocol Class Type.
		 */
		Class<? extends Protocol> type;
		
		public ProtocolPair(){
			
		}
		public ProtocolHeader getProtocolHeader() {
			return protocolHeader;
		}
		public void setProtocolHeader(ProtocolHeader protocolHeader) {
			this.protocolHeader = protocolHeader;
		}
		public String getProtocol() {
			return protocol;
		}
		public void setProtocol(String protocol) {
			this.protocol = protocol;
		}
		public Class<? extends Protocol> getType() {
			return type;
		}
		public void setType(Class<? extends Protocol> type) {
			this.type = type;
		}
		
		
	}
}
