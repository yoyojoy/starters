package com.shengyecapital.aop;


import com.shengyecapital.annotation.ZookeeperLock;
import com.shengyecapital.common.exception.ServerErrorException;
import com.shengyecapital.distributedlock.DistributedLock;
import com.shengyecapital.enums.LockFailActionEnum;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author xiaojun
 * @date 2018/11/10 23:19
 */
@Aspect
@Component
@Slf4j
public class ZookeeperAop extends BaseAop {

    private DistributedLock distributedLock;

    public ZookeeperAop(@NonNull DistributedLock distributedLock) {
        this.distributedLock = distributedLock;
    }

    @Pointcut("@annotation(com.shengyecapital.annotation.ZookeeperLock)")
    public void distributed() {

    }

    @Around("distributed()&&@annotation(zooKeeperLock)")
    public Object execute(ProceedingJoinPoint pjp, ZookeeperLock zooKeeperLock) throws Throwable {
        String lockKey = zooKeeperLock.lockKey();
        Method method = getMethod(pjp);
        String key;
        if (StringUtils.isBlank(lockKey)) {
            key = zooKeeperLock.prefix();
        } else {
            key = zooKeeperLock.prefix() + "/" + parseKey(lockKey, method, pjp.getArgs());
        }
        int retryTimes = zooKeeperLock.action().equals(LockFailActionEnum.CONTINUE) ? zooKeeperLock.retryTimes() : 0;

        boolean lock = distributedLock.lock(key, zooKeeperLock.expireTime(), retryTimes, zooKeeperLock.sleepMills());
        if (!lock) {
            throw new ServerErrorException("【系统温馨提示】获取操作锁失败，请重试");
        }
        //得到锁,执行方法，释放锁
        log.debug("获取锁成功 : " + key);
        try {
            return pjp.proceed();
        } catch (Exception e) {
            log.error("执行业务异常", e);
            throw new ServerErrorException(e.getMessage());
        } finally {
            boolean releaseResult = distributedLock.releaseLock(key);
            log.debug("释放锁 : " + key + (releaseResult ? " success" : " failed"));
        }
    }
}
