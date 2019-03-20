package com.shengyecapital.boot.ceph.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "ceph.datasource")
public class JDBCProperties {


    @NotNull(message = "please provide property config 'ceph.datasource.driver-class-name' ")
    private String driverClassName;
    @NotNull(message = "please provide property config 'ceph.datasource.url' ")
    private String url;
    @NotNull(message = "please provide property config 'ceph.datasource.username' ")
    private String username;
    @NotNull(message = "please provide property config 'ceph.datasource.password' ")
    private String password;

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

