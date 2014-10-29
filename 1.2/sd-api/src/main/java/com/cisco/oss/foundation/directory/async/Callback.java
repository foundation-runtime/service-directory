package com.cisco.oss.foundation.directory.async;

import java.util.Map;

import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;
import com.cisco.oss.foundation.directory.entity.ModelService;
import com.cisco.oss.foundation.directory.entity.ServiceInstanceToken;
import com.cisco.oss.foundation.directory.exception.ErrorCode;
import com.cisco.oss.foundation.directory.proto.Response;
import com.cisco.oss.foundation.directory.proto.AttachSessionResponse.ItemResult;

public interface Callback {
	
	public interface RegistrationCallback extends Callback{
		public void call(boolean result,  ErrorCode error, Object ctx);
	}
	
	public interface AttachSessionCallback extends Callback{
		public void call(boolean result, Map<ServiceInstanceToken, ItemResult> items, ErrorCode error, Object ctx);
	}
	
	public interface GetServiceCallback extends Callback{
		public void call(boolean result, ModelService service, ErrorCode error, Object ctx);
	}
	
	public interface GetMetadataCallback extends Callback{
		public void call(boolean result, ModelMetadataKey key, ErrorCode error, Object ctx);
	}
	
	public interface ProtocolCallback extends Callback {
		public void call(boolean result, Response response, ErrorCode error, Object ctx);
	}
}
