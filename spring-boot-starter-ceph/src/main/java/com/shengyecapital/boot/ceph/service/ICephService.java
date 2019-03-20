package com.shengyecapital.boot.ceph.service;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ICephService {

	/**
	 * 传参bucketname，结合运行环境cephEnv，项目名称cephProjectName生成新的bucketname2，最终创建bucketname2，
	 * bucketname cephEnv cephProjectName 只能是小写字母
	 *<p>
     * To conform with DNS requirements, the following constraints apply:
     *  <ul>
     *      <li>Bucket names should not contain underscores</li>
     *      <li>Bucket names should be between 3 and 63 characters long</li>
     *      <li>Bucket names should not end with a dash</li>
     *      <li>Bucket names cannot contain adjacent periods</li>
     *      <li>Bucket names cannot contain dashes next to periods (e.g.,
     *      "my-.bucket.com" and "my.-bucket" are invalid)</li>
     *      <li>Bucket names cannot contain uppercase characters</li>
     *  </ul>
     * </p>
	 * @param bucketName
	 * @return Bucket
	 * @throws AmazonS3Exception
	 */
	Bucket createBucket(String bucketName) throws AmazonS3Exception;

	/**
	 * 获取所有的 bucket
	 * @return  A list of all of the Amazon S3 buckets owned by the authenticated sender of the request
	 * @throws AmazonS3Exception
	 */
	List<Bucket> allBuckets() throws AmazonS3Exception;

	/**
	 * bucket是否存在
	 * @param bucketName
	 * @return
	 * @throws AmazonS3Exception
	 */
	boolean isBucketExists(String bucketName) throws AmazonS3Exception;

	/**
	 * 删除一个bucket
	 * @param bucketName
	 * @return
	 * @throws AmazonS3Exception
	 */
	boolean removeBucketWithoutFiles(String bucketName) throws AmazonS3Exception;

	/**
	 * 删除某个bucket和他里面的所有文件
	 * @param bucketName
	 * @return
	 * @throws AmazonS3Exception
	 */
	boolean removeBucketAndFiles(String bucketName) throws AmazonS3Exception;

	/**
	 * 文件是否存在
	 * @param bucketName
	 * @param cephKey
	 * @return
	 * @throws AmazonS3Exception
	 */
	boolean isFileExists(String bucketName, String cephKey) throws AmazonS3Exception;

	/**
	 * 获取某个bucket下所有的文件名称
	 * @param bucketName
	 * @return
	 * @throws AmazonS3Exception
	 */
	List<String> listFiles(String bucketName) throws AmazonS3Exception;

	/**
	 * 删除一个文件
	 * 
	 * @param bucketName
	 * @param cephKey
	 * @throws AmazonS3Exception
	 */
	void removeFile(String bucketName, String cephKey) throws AmazonS3Exception;

	/**
	 * 删除一个文件
	 * @param fileId
	 * @throws AmazonS3Exception
	 */
	void removeFile(int fileId) throws AmazonS3Exception;

	/**
	 * 获取文件数据
	 * @param bucketName
	 * @param cephKey
	 * @return
	 * @throws AmazonS3Exception
	 */
	S3ObjectInputStream getObject(String bucketName, String cephKey) throws AmazonS3Exception;

	/**
	 * 获取文件数据
	 * @param bucketName
	 * @param cephKey
	 * @return
	 * @throws AmazonS3Exception
	 */
	CephResponse getCephFileInfo(String bucketName, String cephKey) throws AmazonS3Exception;

	/**
	 * 获取文件数据
	 * @param fileId
	 * @return
	 * @throws AmazonS3Exception
	 */
	CephResponse getCephFileInfo(int fileId) throws AmazonS3Exception;

	/**
	 * 上传文件，cephKey 生成规则自定义，在CephResponse中返回，入库时用CephResponse中bucketName 和
	 * cephKey
	 * @param bucketName
	 * @param cephKey
	 * @param is
	 * @param fileName
	 * @return CephResponse
	 * @throws AmazonS3Exception
	 */
	CephResponse uploadInputStream(String bucketName, String cephKey, InputStream is, String fileName) throws AmazonS3Exception;

	/**
	 * cephKey 自动生成，在CephResponse中返回，入库时用CephResponse中bucketName 和 cephKey
	 * @param bucketName
	 * @param is
	 * @param fileName
	 * @return
	 * @throws AmazonS3Exception
	 * @throws IOException
	 */
	CephResponse uploadInputStream(String bucketName, InputStream is, String fileName) throws AmazonS3Exception, IOException;

	/**
	 * 上传文件，cephKey 生成规则自定义，在CephResponse中返回，入库时用CephResponse中bucketName 和
	 * cephKey
	 * @param bucketName
	 * @param cephKey
	 * @param file
	 * @return int fileId
	 * @throws AmazonS3Exception
	 */
	CephResponse uploadFile(String bucketName, String cephKey, File file) throws AmazonS3Exception;

	/**
	 * cephKey 自动生成，在CephResponse中返回，入库时用CephResponse中bucketName 和 cephKey
	 * @param bucketName
	 * @param file
	 * @return
	 * @throws AmazonS3Exception
	 * @throws IOException
	 */
	CephResponse uploadFile(String bucketName, File file) throws AmazonS3Exception, IOException;
}
