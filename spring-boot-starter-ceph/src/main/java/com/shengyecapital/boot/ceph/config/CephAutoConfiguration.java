package com.shengyecapital.boot.ceph.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.shengyecapital.boot.ceph.properties.CephProperties;
import com.shengyecapital.boot.ceph.properties.JDBCProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties({CephProperties.class, JDBCProperties.class})
@ConditionalOnProperty(prefix = "ceph", value = "enable", matchIfMissing = true)
public class CephAutoConfiguration {

    @Value("${ceph.client.endpoint}")
    private String endpoint;
    @Value("${ceph.client.accessKey}")
    private String accessKey;
    @Value("${ceph.client.secretKey}")
    private String secretKey;

    @Bean
    @Qualifier("shengyeAmazonS3")
    public AmazonS3 shengyeAmazonS3() {
        if (StringUtils.isBlank(endpoint) || StringUtils.isBlank(accessKey) || StringUtils.isBlank(secretKey)) {
            return null;
        }
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setMaxConnections(200);
        clientConfig.setProtocol(Protocol.HTTP);
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withClientConfiguration(clientConfig)
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, Regions.DEFAULT_REGION.name()))
                .build();
    }

    @Bean("cephDatasource")
    @ConfigurationProperties(prefix = "ceph.datasource")
    public DataSource cephDatasource() {
        JDBCProperties jdbc = new JDBCProperties();
        Properties properties = new Properties();
        properties.setProperty("druid.testWhileIdle", "true");
        properties.setProperty("druid.testOnBorrow", "true");
        properties.setProperty("druid.validationQuery", "select 1");
        properties.setProperty("druid.filters", "stat,wall,slf4j");
        properties.setProperty("druid.timeBetweenLogStatsMillis", "3600000");
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.configFromPropety(properties);
        druidDataSource.setUrl(jdbc.getUrl());
        druidDataSource.setUsername(jdbc.getUsername());
        druidDataSource.setPassword(jdbc.getPassword());
        druidDataSource.setDriverClassName(jdbc.getDriverClassName());
        return druidDataSource;
    }

    @Bean
    public JdbcTemplate shengyeCephJdbcTemplate(){
        return new JdbcTemplate(cephDatasource());
    }
}
