package com.shengyecapital.boot.ceph.service;

import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.shengyecapital.boot.ceph.repository.CephFile;
import com.shengyecapital.boot.ceph.repository.CephRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service("cephService")
public class CephServiceImpl implements ICephService {

    @Value("${spring.profiles.active}")
    private String businessEnv;
    @Value("${spring.application.name}")
    private String applicationName;
    @Autowired
    private CephStoreService cephStoreService;
    @Autowired
    private CephRepository cephRepository;

    private String generateBucketName(String bucketName) {
        return String.format("%s-%s-%s", businessEnv, applicationName, bucketName).toLowerCase();
    }

    @Override
    public Bucket createBucket(String bucketName) throws AmazonS3Exception {
        bucketName = this.generateBucketName(bucketName);
        return cephStoreService.createBucket(bucketName);
    }

    @Override
    public List<Bucket> allBuckets() throws AmazonS3Exception {
        return cephStoreService.getBuckets();
    }

    @Override
    public boolean isBucketExists(String bucketName) throws AmazonS3Exception {
        bucketName = this.generateBucketName(bucketName);
        return cephStoreService.isBucketExists(bucketName);
    }

    @Override
    public boolean removeBucketWithoutFiles(String bucketName) throws AmazonS3Exception {
        bucketName = this.generateBucketName(bucketName);
        return cephStoreService.deleteBucket(bucketName);
    }

    @Override
    public boolean removeBucketAndFiles(String bucketName) throws AmazonS3Exception {
        bucketName = generateBucketName(bucketName);
        return cephStoreService.deleteBucketAndFiles(bucketName);
    }

    @Override
    public List<String> listFiles(String bucketName) throws AmazonS3Exception {
        bucketName = this.generateBucketName(bucketName);
        return cephStoreService.getFiles(bucketName);
    }

    @Override
    public boolean isFileExists(String bucketName, String cephKey) throws AmazonS3Exception {
        bucketName = this.generateBucketName(bucketName);
        CephFile file = cephRepository.queryCephFileByKey(bucketName, cephKey);
        if(file == null){
            return false;
        }
        return cephStoreService.isFileExists(bucketName, cephKey);
    }

    @Override
    public void removeFile(String bucketName, String cephKey) throws AmazonS3Exception {
        bucketName = this.generateBucketName(bucketName);
        cephStoreService.deleteFile(bucketName, cephKey);
        cephRepository.remove(bucketName, cephKey);
    }

    @Override
    public void removeFile(int fileId) throws AmazonS3Exception {
        CephFile file = cephRepository.queryCephFileById(fileId);
        if(file != null){
            cephStoreService.deleteFile(file.getBucketName(), file.getObjectName());
            cephRepository.remove(fileId);
        }
    }

    @Override
    public S3ObjectInputStream getObject(String bucketName, String cephKey) throws AmazonS3Exception {
        bucketName = this.generateBucketName(bucketName);
        return cephStoreService.getObject(bucketName, cephKey);
    }

    @Override
    public CephResponse getCephFileInfo(String bucketName, String cephKey) throws AmazonS3Exception {
        bucketName = this.generateBucketName(bucketName);
        CephFile file = cephRepository.queryCephFileByKey(bucketName, cephKey);
        if(file == null ){
            return null;
        }
        InputStream inputStream = cephStoreService.getInputStream(bucketName, cephKey);
        return this.generateResponse(inputStream, file);
    }

    @Override
    public CephResponse getCephFileInfo(int fileId) throws AmazonS3Exception {
        CephFile file = cephRepository.queryCephFileById(fileId);
        if(file == null ){
            return null;
        }
        InputStream inputStream = cephStoreService.getInputStream(file.getBucketName(), file.getObjectName());
        return this.generateResponse(inputStream, file);
    }

    @Override
    public CephResponse uploadInputStream(String bucketName, String cephKey, InputStream is, String fileName) throws AmazonS3Exception {
        if(is == null){
            return null;
        }
        return this.upload(bucketName, cephKey, is, fileName);
    }

    @Override
    public CephResponse uploadInputStream(String bucketName, InputStream is, String fileName) throws AmazonS3Exception, IOException {
        if(is == null){
            return null;
        }
        String cephKey = UUID.randomUUID().toString().replaceAll("-", "");
        return this.upload(bucketName, cephKey, is, fileName);
    }

    @Override
    public CephResponse uploadFile(String bucketName, String cephKey, File file) throws AmazonS3Exception {
        if(!file.exists()){
            return null;
        }
        return this.upload(bucketName, cephKey, file);
    }

    @Override
    public CephResponse uploadFile(String bucketName, File file) throws AmazonS3Exception, IOException {
        if(!file.exists()){
            return null;
        }
        String cephKey = UUID.randomUUID().toString().replaceAll("-", "");
        return this.upload(bucketName, cephKey, file);
    }

    private CephResponse upload(String bucketName, String cephKey, File file){
        bucketName = this.generateBucketName(bucketName);
        if (!cephStoreService.isBucketExists(bucketName)) {
            cephStoreService.createBucket(bucketName);
        }
        PutObjectResult result = cephStoreService.saveFile(bucketName, cephKey, file);
        CephResponse response = new CephResponse();
        response.setCephBucket(bucketName);
        response.setCephKey(cephKey);
        response.setVersionId(result.getVersionId());
        response.setFileMd5(result.getContentMd5());
        response.setInputStream(cephStoreService.getInputStream(bucketName, cephKey));
        return this.generateResponse(response, file);
    }

    private CephResponse upload(String bucketName, String cephKey, InputStream io, String fileName){
        bucketName = this.generateBucketName(bucketName);
        if (!cephStoreService.isBucketExists(bucketName)) {
            cephStoreService.createBucket(bucketName);
        }
        PutObjectResult result = cephStoreService.saveInputStream(bucketName, cephKey, io);
        CephResponse response = new CephResponse();
        response.setCephBucket(bucketName);
        response.setCephKey(cephKey);
        response.setVersionId(result.getVersionId());
        response.setFileMd5(result.getContentMd5());
        response.setInputStream(cephStoreService.getInputStream(bucketName, cephKey));
        return this.generateResponse(response, fileName);
    }

    private String getFileExt(String fileName){
        return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
    }

    private CephResponse generateResponse(CephResponse response, File file){
        String fileName = file.getName().substring(file.getName().lastIndexOf("/") + 1, file.getName().length());
        String ext = this.getFileExt(fileName);
        CephFile entity = new CephFile(fileName, response.getCephBucket(), response.getCephKey(), response.getFileMd5(), ext);
        int fileID = cephRepository.insertFile(entity);
        response.setFileId(fileID);
        response.setFileName(fileName);
        return response;
    }

    private CephResponse generateResponse(CephResponse response, String fileName){
        String ext = this.getFileExt(fileName);
        CephFile entity = new CephFile(fileName, response.getCephBucket(), response.getCephKey(), response.getFileMd5(), ext);
        int fileID = cephRepository.insertFile(entity);
        response.setFileId(fileID);
        response.setFileName(fileName);
        return response;
    }

    private CephResponse generateResponse(InputStream inputStream, CephFile file){
        CephResponse response = new CephResponse();
        response.setInputStream(inputStream);
        response.setFileName(file.getFileName());
        response.setCephBucket(file.getBucketName());
        response.setCephKey(file.getObjectName());
        response.setFileId(file.getId());
        response.setFileMd5(file.getFileMd5());
        return response;
    }

    private String reductionBucketName(String name){
        return name.replace(String.format("%s-%s-", businessEnv, applicationName).toLowerCase(), "");
    }
}
