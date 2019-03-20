package com.shengyecapital.boot.ceph.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "ceph.client")
public class CephProperties {

    @NotNull(message = "please provide property config 'ceph.client.endpoint' ")
    private String endpoint;
    @NotNull(message = "please provide property config 'ceph.client.accessKey' ")
    private String accessKey;
    @NotNull(message = "please provide property config 'ceph.client.secretKey' ")
    private String secretKey;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
