package com.yibi.backend.rabbitmq;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.yibi.backend.common.ErrorCode;
import com.yibi.backend.exception.BusinessException;
import com.yibi.backend.model.dto.chatglm.ChatGLMRequest;
import com.yibi.backend.model.dto.chatglm.ChatGLMResponse;
import com.yibi.backend.model.dto.chatglm.ChatHistory;
import com.yibi.backend.model.entity.Chart;
import com.yibi.backend.service.ChartService;
import com.yibi.backend.service.ChatGLMService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class MyMessageConsumer {
    @Resource
    private ChartService chartService;

    @Resource
    private ChatGLMService chatGLMService;

    private static String secondQueryTemplate = "结合生成的图表，但不要出现任何代码相关内容，用纯文字完成下列需求: \n%s";

    private final static Gson GSON = new Gson();

    @SneakyThrows
    @RabbitListener(queues = {"yibi_queue"}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info(message);
        ChatGLMRequest chatGLMRequest = GSON.fromJson(message, ChatGLMRequest.class);
        channel.basicAck(deliveryTag, false);
        channel.basicNack(deliveryTag, false, true);

        Long chartId = chatGLMRequest.getChartId();
        Chart chart = chartService.getById(chartId);

        String responseFromGLM1 = chatGLMService.getResponseFromGLM(chatGLMRequest);

        ChatGLMResponse chatGLMResponse1 = GSON.fromJson(responseFromGLM1, ChatGLMResponse.class);
        if (chatGLMResponse1.getChat_history_id() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        String[] strings = chatGLMResponse1.getText().split("@MySpace");
        if (strings.length < 1) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成错误");
        }
        String code = strings[strings.length - 1];

        List<ChatHistory> chatHistoryList = chatGLMRequest.getHistory();

        chatHistoryList.add(ChatHistory.builder().role("user").content(chatGLMRequest.getQuery()).build());
        chatHistoryList.add(ChatHistory.builder().role("assistant").content("@MySpace\n" + code + "@MySpace\n").build());
        // 2. 生成结论文字
        String secondQuery = String.format(secondQueryTemplate, chart.getGoal());
        chatGLMRequest.setQuery(secondQuery);
        chatGLMRequest.setHistory(chatHistoryList);

        String responseFromGLM2 = chatGLMService.getResponseFromGLM(chatGLMRequest);

        ChatGLMResponse chatGLMResponse2 = GSON.fromJson(responseFromGLM2, ChatGLMResponse.class);
        if (chatGLMResponse2.getChat_history_id() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        chatHistoryList.add(ChatHistory.builder().role("user").content(secondQuery).build());
        chatHistoryList.add(ChatHistory.builder().role("assistant").content(chatGLMResponse2.getText()).build());

        chart.setGenText(chatGLMResponse2.getText());
        chart.setGenCode(code);
        chart.setChatHistoryList(GSON.toJson(chatHistoryList));
        chart.setIsFinished(1);

        chartService.updateById(chart);
    }
}
