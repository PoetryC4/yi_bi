package com.yibi.backend.utils;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EmailCodeUtils {
    public static class CodeEmailPair {

        private String verificationCode;
        private long createTime;

        public String getVerificationCode() {
            return verificationCode;
        }

        public long getCreateTime() {
            return createTime;
        }

        public void setVerificationCode(String _verificationCode) {
            this.verificationCode = _verificationCode;
        }

        public void setCreateTime(long _createTime) {
            this.createTime = _createTime;
        }
    }

    private static final ConcurrentHashMap<String, CodeEmailPair> emailCodeMap = new ConcurrentHashMap<>();

    public static String generateVerificationCode(String toEmail) {
        Random random = new Random();
        CodeEmailPair codeEmailPair = new CodeEmailPair();
        codeEmailPair.setVerificationCode(String.format("%06d", random.nextInt(1000000)));
        codeEmailPair.setCreateTime(System.currentTimeMillis());
        emailCodeMap.put(toEmail, codeEmailPair);

        int delayInMillis = 3000000; // 2秒延迟
        Runnable task = () -> {
            removePair(toEmail);
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.schedule(task, delayInMillis, TimeUnit.MILLISECONDS);

        return codeEmailPair.getVerificationCode();
    }

    public static CodeEmailPair getPair(String userEmail) {
        return emailCodeMap.getOrDefault(userEmail, null);
    }

    public static void removePair(String userEmail) {
        if (!emailCodeMap.containsKey(userEmail)) return;
        else emailCodeMap.remove(userEmail);
    }
}
