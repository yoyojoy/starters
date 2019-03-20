package com.shengyecapital.boot.process.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

@Data
@ConfigurationProperties(prefix = "activiti.datasource")
public class JDBCProperties {

    @NotNull(message = "please provide property config 'activiti.datasource.driver-class-name' ")
    private String driverClassName;
    @NotNull(message = "please provide property config 'activiti.datasource.url' ")
    private String url;
    @NotNull(message = "please provide property config 'activiti.datasource.username' ")
    private String username;
    @NotNull(message = "please provide property config 'activiti.datasource.password' ")
    private String password;
}
