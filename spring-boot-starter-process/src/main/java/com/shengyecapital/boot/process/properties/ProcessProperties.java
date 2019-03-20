package com.shengyecapital.boot.process.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

@Data
@ConfigurationProperties(prefix = "activiti.process")
public class ProcessProperties {

    @NotNull(message = "please provide property config 'spring.process.database-schema-update' ")
    private String databaseSchemaUpdate;
    @NotNull(message = "please provide property config 'spring.process.database-schema' ")
    private String databaseSchema;
    @NotNull(message = "please provide property config 'spring.process.async-executor-activate' ")
    private Boolean asyncExecutorActivate;
    @NotNull(message = "please provide property config 'spring.process.process-definition-location-prefix' ")
    private String processDefinitionLocationPrefix;
    @NotNull(message = "please provide property config 'spring.process.history-level' ")
    private String historyLevel;
    @NotNull(message = "please provide property config 'spring.process.db-identity-used' ")
    private Boolean dbIdentityUsed;
    @NotNull(message = "please provide property config 'spring.process.db-history-used' ")
    private Boolean dbHistoryUsed;
    @NotNull(message = "please provide property config 'spring.process.activityFontName' ")
    private String activityFontName;
    @NotNull(message = "please provide property config 'spring.process.labelFontName' ")
    private String labelFontName;
}

