package com.yibi.backend.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {

        //BlockingQueue<Runnable> workQueue,
        //        ThreadFactory threadFactory,
        //        RejectedExecutionHandler handler
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count = 1;

            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("线程_" + count);
                return thread;
            }
        };
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 20, 3 * 60 * 1000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(10), threadFactory);
        return threadPoolExecutor;
    }
}
