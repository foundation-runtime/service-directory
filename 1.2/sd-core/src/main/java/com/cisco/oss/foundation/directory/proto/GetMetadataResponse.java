package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;

public class GetMetadataResponse extends Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ModelMetadataKey metadata;

	public GetMetadataResponse() {
	}

	public GetMetadataResponse(ModelMetadataKey metadata) {
		this.metadata = metadata;
	}

	public ModelMetadataKey getMetadata() {
		return metadata;
	}

	public void setMetadata(ModelMetadataKey metadata) {
		this.metadata = metadata;
	}

}
