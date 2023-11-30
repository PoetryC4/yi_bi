package com.yibi.backend.manager;

import com.yibi.backend.common.ErrorCode;
import com.yibi.backend.exception.BusinessException;
import com.yibi.backend.exception.ThrowUtils;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RateLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 根据userId
     * @param key
     */
    public void doRateLimiter(String key) {

        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        rateLimiter.trySetRate(RateType.OVERALL, 1, 10, RateIntervalUnit.SECONDS);

        boolean acquire = rateLimiter.tryAcquire(1L);

        ThrowUtils.throwIf(!acquire, new BusinessException(ErrorCode.TOO_MANY_REQUEST));
    }
}
