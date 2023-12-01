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

    private final static Long maxPermits = 100L;

    /**
     * 根据userId
     *
     * @param key
     */
    public void doRateLimiter(String key) {

        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        rateLimiter.trySetRate(RateType.OVERALL, 1, 10, RateIntervalUnit.SECONDS);

        long availablePermits = rateLimiter.availablePermits();

        // 保证最大值为 maxPermits
        boolean acquire = rateLimiter.tryAcquire(Math.max(1L, availablePermits - maxPermits));

        ThrowUtils.throwIf(!acquire, new BusinessException(ErrorCode.TOO_MANY_REQUEST));
    }
}
