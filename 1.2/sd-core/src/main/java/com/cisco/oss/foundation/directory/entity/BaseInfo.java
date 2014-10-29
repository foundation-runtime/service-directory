package com.cisco.oss.foundation.directory.entity;

/**
 * The immutable Object of Info to distribute.
 *  
 * @author zuxiang
 *
 */
public class BaseInfo {
	/**
	 * The create transaction Xid.
	 */
	private long createXid;
	
	/**
	 * The modify transaction Xid.
	 */
	private long modifyXid;

	/**
	 * The create time.
	 */
	private long createTime;

	/**
	 * The latest modification time.
	 */
	private long modifyTime;

	/**
	 * Object version number.
	 */
	private long version;
	
	public BaseInfo(){};

	public BaseInfo(long createXid, long modifyXid, long createTime,
			long modifyTime, long version) {
		this.createXid = createXid;
		this.modifyXid = modifyXid;
		this.createTime = createTime;
		this.modifyTime = modifyTime;
		this.version = version;
	}

	public long getCreateXid() {
		return createXid;
	}

	public long getModifyXid() {
		return modifyXid;
	}

	public long getCreateTime() {
		return createTime;
	}

	public long getModifyTime() {
		return modifyTime;
	}

	public long getVersion() {
		return version;
	}
}
