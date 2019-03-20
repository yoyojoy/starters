package com.shengyecapital.boot.ceph.exception;

public class CephFileUploadException extends RuntimeException {

    public CephFileUploadException(String message, Throwable t) {
        super(message, t);
    }

    public CephFileUploadException(String message) {
        super(message);
    }
}
