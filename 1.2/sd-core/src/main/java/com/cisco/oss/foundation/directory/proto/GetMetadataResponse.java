/**
 * Copyright (c) 2013-2014 by Cisco Systems, Inc. 
 * All rights reserved. 
 */
package com.cisco.oss.foundation.directory.proto;

import com.cisco.oss.foundation.directory.entity.ModelMetadataKey;

/**
 * Get ModelMetadataKey Response.
 * 
 * @author zuxiang
 *
 */
public class GetMetadataResponse extends Response {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The ModelMetadataKey.
	 */
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
