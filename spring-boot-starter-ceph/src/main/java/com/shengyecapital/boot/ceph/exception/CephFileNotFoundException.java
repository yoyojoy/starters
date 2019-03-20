package com.shengyecapital.boot.ceph.exception;

public class CephFileNotFoundException extends RuntimeException {

    public CephFileNotFoundException(String message, Throwable t) {
        super(message, t);
    }

    public CephFileNotFoundException(String message) {
        super(message);
    }
}
