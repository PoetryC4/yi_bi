package com.yibi.backend.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.yibi.backend.common.ErrorCode;
import com.yibi.backend.exception.BusinessException;
import com.yibi.backend.model.dto.chatglm.ChatGLMRequest;
import com.yibi.backend.service.ChatGLMService;
import com.yibi.backend.utils.EncryptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ChatGLMServiceImpl implements ChatGLMService {


    private String accessKey = "BkEt5rTYtNug8CJ3pzWYfobhEAv9Mzis";
    private String secretKey = "u3XXmtGTIPLLz68WGi6QnRwuMrjWB3eecYXAKwVzjWSDasQpnc2c6MRLoSsNrcaH";

    @Value("${llm.api-url}")
    private String llmApiUrl;

    @Value("${spring.application.name}")
    private String appName;

    private static final Gson GSON = new Gson();

    @Override
    public String getResponseFromGLM(ChatGLMRequest chatGLMRequest) {

        long timestamp = new Date().getTime();
        String signature = EncryptionUtils.generateSignature(appName + timestamp, timestamp, secretKey);
        String responseStr1 = HttpUtil.createPost(llmApiUrl)
                .header("Content-Type", "application/json")
                .header("AccessKey", accessKey)
                .header("Timestamp", String.valueOf(timestamp))
                .header("Code", appName)
                .header("Signature", signature)
                .timeout(1000 * 30)
                .body(GSON.toJson(chatGLMRequest))
                .execute()
                .body();
        if (responseStr1.contains("无权限")) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if (StringUtils.isBlank(responseStr1)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR);
        }
        return responseStr1;
    }
}
