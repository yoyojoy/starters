package com.shengyecapital.boot.ceph.service;

import lombok.Data;
import java.io.InputStream;

@Data
public class CephResponse {

	/**
	 * 存储的文件ID
	 */
	private int fileId;
	/**
	 * 存储的文件名称
	 */
	private String fileName;
	/**
	 * 桶名称
	 */
	private String cephBucket;
	/**
	 * 存放文件的key
	 */
	private String cephKey;
	/**
	 * 版本ID
	 */
	private String versionId;
	/**
	 * 文件的MD5值
	 */
	private String fileMd5;
	/**
	 * 文件流
	 */
	private InputStream inputStream;
}
