package com.shengyecapital.boot.process.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.shengyecapital.boot.process.properties.ProcessProperties;
import com.shengyecapital.boot.process.properties.JDBCProperties;
import org.activiti.engine.*;
import org.activiti.engine.impl.persistence.StrongUuidGenerator;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties({ProcessProperties.class, JDBCProperties.class})
@ConditionalOnProperty(prefix = "activiti", value = "enable", matchIfMissing = true)
public class ProcessAutoConfiguration {
    @Autowired
    @Qualifier("activiDatasource")
    private DataSource dataSource;
    @Autowired
    @Qualifier("activitiTransactionManager")
    private PlatformTransactionManager transactionManager;
    @Autowired
    private ProcessProperties config;
    @Autowired
    private ApplicationContext applicationContext;


    @Bean("activiDatasource")
    @ConfigurationProperties(prefix = "activiti.datasource")
    public DataSource activitiDatasource() {
        JDBCProperties jdbc = new JDBCProperties();
        Properties properties = new Properties();
        properties.setProperty("druid.testWhileIdle", "true");
        properties.setProperty("druid.testOnBorrow", "true");
        properties.setProperty("druid.validationQuery", "select 1");
        properties.setProperty("druid.filters", "stat,wall,slf4j");
        properties.setProperty("druid.timeBetweenLogStatsMillis", "3600000");
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.configFromPropety(properties);
        druidDataSource.setDriverClassName(jdbc.getDriverClassName());
        druidDataSource.setUrl(jdbc.getUrl());
        druidDataSource.setUsername(jdbc.getUsername());
        druidDataSource.setPassword(jdbc.getPassword());
        return druidDataSource;
    }

    @Bean("activitiTransactionManager")
    @ConfigurationProperties(prefix = "activiti.datasource")
    public PlatformTransactionManager activitiTransactionManager(){
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    @ConfigurationProperties(prefix = "activiti.process")
    public SpringProcessEngineConfiguration springProcessEngineConfiguration(){
        SpringProcessEngineConfiguration springProcessEngineConfiguration = new SpringProcessEngineConfiguration();
        springProcessEngineConfiguration.setTransactionManager(transactionManager);
        springProcessEngineConfiguration.setDataSource(dataSource);
        try {
            springProcessEngineConfiguration.setDeploymentResources(applicationContext.getResources(config.getProcessDefinitionLocationPrefix()));
        }catch (Exception e){
            e.printStackTrace();
        }
        springProcessEngineConfiguration
                .setIdGenerator(new StrongUuidGenerator())
                .setDatabaseType(config.getDatabaseSchema())
                .setDatabaseSchemaUpdate(config.getDatabaseSchemaUpdate())
                .setLabelFontName(config.getLabelFontName())
                .setActivityFontName(config.getActivityFontName())
                .setAsyncExecutorActivate(config.getAsyncExecutorActivate())
                .setCreateDiagramOnDeploy(true)
                .setDbIdentityUsed(config.getDbIdentityUsed())
                .setHistory(config.getHistoryLevel())
                .setDbHistoryUsed(config.getDbHistoryUsed());
        return springProcessEngineConfiguration;
    }

    public ProcessEngine processEngine() {
        return this.springProcessEngineConfiguration().buildProcessEngine();
    }

    @Bean
    public RuntimeService runtimeService(){
        return this.processEngine().getRuntimeService();
    }

    @Bean
    public RepositoryService repositoryService(){
        return this.processEngine().getRepositoryService();
    }

    @Bean
    public FormService formService(){
        return this.processEngine().getFormService();
    }

    @Bean
    public IdentityService identityService(){
        return this.processEngine().getIdentityService();
    }

    @Bean
    public HistoryService historyService(){
        return this.processEngine().getHistoryService();
    }

    @Bean
    public ManagementService managementService(){
        return this.processEngine().getManagementService();
    }

    @Bean
    public TaskService taskService(){
        return this.processEngine().getTaskService();
    }

    @Bean
    public DynamicBpmnService dynamicBpmnService(){
        return this.processEngine().getDynamicBpmnService();
    }
}
