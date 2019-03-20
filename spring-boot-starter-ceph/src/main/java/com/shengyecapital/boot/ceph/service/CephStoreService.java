package com.shengyecapital.boot.ceph.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CephStoreService {

    @Autowired
    @Qualifier("shengyeAmazonS3")
    private AmazonS3 amazonS3;

    /**
     * 传参bucketname，结合运行环境cephEnv，项目名称cephProjectName生成新的bucketname2，最终创建bucketname2，
     * bucketName cephEnv cephProjectName 只能是小写字母
     *
     * @param bucketName
     * @return Bucket
     * @throws AmazonS3Exception
     */
    public Bucket createBucket(String bucketName) throws AmazonS3Exception {
        Bucket bucket = amazonS3.createBucket(bucketName);
        return bucket;
    }

    /**
     * 获取所有的 bucket
     *
     * @return
     * @throws AmazonS3Exception
     */
    public List<Bucket> getBuckets() throws AmazonS3Exception {
        List<Bucket> buckets = amazonS3.listBuckets();
        return buckets;
    }

    /**
     * 获取某个bucket下所有的文件名称
     *
     * @param bucketName
     * @return
     * @throws AmazonS3Exception
     */
    public List<String> getFiles(String bucketName) throws AmazonS3Exception {
        List<String> list = new ArrayList<String>();
        ObjectListing objects = amazonS3.listObjects(bucketName);
        int i = 0;
        do {
            for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                list.add(i, objectSummary.getKey());
                i++;
            }
            objects = amazonS3.listNextBatchOfObjects(objects);
        } while (objects.isTruncated());
        return list;
    }

    /**
     * 获取文件
     *
     * @param bucketName
     * @param cephKey
     * @return
     * @throws AmazonS3Exception
     */
    public S3ObjectInputStream getObject(String bucketName, String cephKey) throws AmazonS3Exception {
        S3Object s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, cephKey));
        S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
        return s3ObjectInputStream;
    }

    /**
     * 获取文件流
     *
     * @param bucketName
     * @param cephKey
     * @return
     * @throws AmazonS3Exception
     */
    public InputStream getInputStream(String bucketName, String cephKey) throws AmazonS3Exception {
        InputStream is = null;
        S3Object s3Object = null;

        try {
            s3Object = amazonS3.getObject(new GetObjectRequest(bucketName, cephKey));
            try (S3ObjectInputStream stream = s3Object.getObjectContent()) {
                ByteArrayOutputStream temp = new ByteArrayOutputStream();
                IOUtils.copy(stream, temp);
                is = new ByteArrayInputStream(temp.toByteArray());
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (s3Object != null) {
                try {
                    s3Object.close();
                } catch (IOException e) {
                    log.error("Unable to close S3 object: {}", e.getMessage(), e);
                }
            }
        }

        return is;
    }

    /**
     * 删除一个文件
     *
     * @param bucketName
     * @param cephKey
     * @throws AmazonS3Exception
     */
    public void deleteFile(String bucketName, String cephKey) throws AmazonS3Exception {
        amazonS3.deleteObject(bucketName, cephKey);
    }

    /**
     * 删除一个bucket
     *
     * @param bucketName
     * @return
     * @throws AmazonS3Exception
     */
    public boolean deleteBucket(String bucketName) throws AmazonS3Exception {
        if (getFiles(bucketName).isEmpty()) {
            amazonS3.deleteBucket(bucketName);
            return true;
        } else {
            throw new AmazonServiceException("bucket: " + bucketName + " 下有文件，不能删除");
        }
    }

    /**
     * 删除某个bucket和他里面的所有文件
     *
     * @param bucketName
     * @return
     * @throws AmazonS3Exception
     */
    public boolean deleteBucketAndFiles(String bucketName) throws AmazonS3Exception {
        if (getFiles(bucketName).isEmpty()) {
            amazonS3.deleteBucket(bucketName);
            return true;
        } else {
            List<String> list = getFiles(bucketName);
            for (String s : list) {
                amazonS3.deleteObject(bucketName, s);
            }
            amazonS3.deleteBucket(bucketName);
            return true;
        }

    }

    /**
     * bucket是否存在
     *
     * @param bucketName
     * @return
     * @throws AmazonS3Exception
     */
    public boolean isBucketExists(String bucketName) throws AmazonS3Exception {
        List<Bucket> list = getBuckets();
        for (Bucket b : list) {
            if (b.getName().equals(bucketName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 文件是否存在
     *
     * @param bucketName
     * @param cephKey
     * @return
     * @throws AmazonS3Exception
     */
    public boolean isFileExists(String bucketName, String cephKey) throws AmazonS3Exception {
        List<String> files = getFiles(bucketName);
        for (int i = 0; i < files.size(); i++) {
            String file = files.get(i);
            if (cephKey.equals(file)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 保存一个文件
     *
     * @param bucketName
     * @param cephKey
     * @param inputStream
     * @return
     * @throws AmazonS3Exception
     */
    public PutObjectResult saveInputStream(String bucketName, String cephKey, InputStream inputStream)
            throws AmazonS3Exception {
        try {
            log.info("================写入数据start... " + cephKey);
            PutObjectRequest request = new PutObjectRequest(bucketName, cephKey, inputStream, new ObjectMetadata());
            request.getRequestClientOptions().setReadLimit(inputStream.available() * 1024);
            log.info("================写入数据end... " + cephKey);
            return amazonS3.putObject(request);
        } catch (IOException e) {
            log.error("保存文件异常", e);
            throw new AmazonS3Exception(e.getMessage());
        }
    }

    /**
     * * 保存一个文件
     *
     * @param bucketName
     * @param cephKey
     * @param file
     * @return
     * @throws AmazonS3Exception
     */
    public PutObjectResult saveFile(String bucketName, String cephKey, File file) throws AmazonS3Exception {
        PutObjectResult result = amazonS3.putObject(bucketName, cephKey, file);
        return result;
    }

}