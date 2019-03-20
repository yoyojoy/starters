package com.shengyecapital.autoconfigure;

import com.shengyecapital.aop.ZookeeperAop;
import com.shengyecapital.condition.ZookeeperCondition;
import com.shengyecapital.distributedlock.ZookeeperDistributedLock;
import lombok.Data;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * 分布式锁自动配置器
 *
 * @author xiaojun
 * @date 2018/11/10 23:27
 */
@Configuration
public class LockAutoConfiguration {

    @Conditional(ZookeeperCondition.class)
    @ConfigurationProperties(prefix = "spring.zookeeper")
    @Data
    public class CoordinateConfiguration {
        private String zkServers;

        private int sessionTimeout = 30000;

        private int connectionTimeout = 5000;

        private int baseSleepTimeMs = 1000;

        private int maxRetries = 2;

        @Bean(destroyMethod = "close")
        public CuratorFramework curatorFramework() {
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(this.baseSleepTimeMs, this.maxRetries);
            CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                    .connectString(this.zkServers)
                    .sessionTimeoutMs(this.sessionTimeout)
                    .connectionTimeoutMs(this.connectionTimeout)
                    .retryPolicy(retryPolicy)
                    .build();
            curatorFramework.start();
            return curatorFramework;
        }

        @Bean("zookeeperDistributedLock")
        public ZookeeperDistributedLock zookeeperDistributedLock(CuratorFramework curatorFramework) {
            return new ZookeeperDistributedLock(curatorFramework);
        }

        @Bean
        public ZookeeperAop zookeeperAop(ZookeeperDistributedLock zookeeperDistributedLock) {
            return new ZookeeperAop(zookeeperDistributedLock);
        }
    }
}
