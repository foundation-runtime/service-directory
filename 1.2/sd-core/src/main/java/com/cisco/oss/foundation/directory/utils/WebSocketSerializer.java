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

public class WebSocketSerializer {
	private static final ObjectMapper mapper = new ObjectMapper();
	
	public static ResponseSerializer getResponseSerializer(ResponseHeader header, Response response) throws JsonGenerationException, JsonMappingException, IOException{
		final String s = serializeResponse(header, response);
		return new ResponseSerializer(){

			@Override
			public String searializerAsString() {
				return s;
			}
			
		};
	}
	
	public static ProtocolSerializer getProtocolSerializer(ProtocolHeader header, Protocol protocol) throws JsonGenerationException, JsonMappingException, IOException{
		final String s = serializeProtocol(header, protocol);
		return new ProtocolSerializer(){

			@Override
			public String searializerAsString() {
				return s;
			}
			
		};
	}
	
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
	
	private static String toJsonStr(Object object) throws JsonGenerationException, JsonMappingException, IOException{
		return mapper.writeValueAsString(object);
	}
	
	public interface ResponseSerializer{
		public String searializerAsString();
	}
	
	public interface ResponseDeserializer{
		public ResponseHeader deserializerResponseHeader();
		public Response deserializerResponse();
	}
	
	public interface ProtocolSerializer{
		public String searializerAsString();
	}
	
	public interface ProtocolDeserializer{
		public ProtocolHeader deserializerProtocolHeader();
		public Protocol deserializerProtocol();
	}
	
	static class ResponsePair{
		ResponseHeader responseHeader;
		String response;
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
	
	static class ProtocolPair{
		ProtocolHeader protocolHeader;
		String protocol;
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
