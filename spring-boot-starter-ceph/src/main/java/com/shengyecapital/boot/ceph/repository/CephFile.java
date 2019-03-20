package com.shengyecapital.boot.ceph.repository;

import java.util.Date;

public class CephFile {

    private Integer id;

    private String fileName;

    private String bucketName;

    private String objectName;

    private String fileMd5;

    private String fileExt;

    private Date createTime;

    private Date updateTime;

    private Byte isDelete;

    public CephFile(){}

    public CephFile(String fileName, String bucketName, String objectName, String fileMd5, String fileExt) {
        this.fileName = fileName;
        this.bucketName = bucketName;
        this.objectName = objectName;
        this.fileMd5 = fileMd5;
        this.fileExt = fileExt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getFileExt() {
        return fileExt;
    }

    public void setFileExt(String fileExt) {
        this.fileExt = fileExt;
    }

    public Byte getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Byte isDelete) {
        this.isDelete = isDelete;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
