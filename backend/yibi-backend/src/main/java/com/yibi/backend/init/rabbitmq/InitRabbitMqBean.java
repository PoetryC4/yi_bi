package com.yibi.backend.init.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class InitRabbitMqBean {

    @Value("${spring.rabbitmq.host}")
    private String rabbitMqHost;

    @PostConstruct
    public void mqInit() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitMqHost);
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            String exchangeName = "yibi_exchange";
            channel.exchangeDeclare(exchangeName, "direct");
            String queueName = "yibi_queue";

            Map<String, Object> queueArgs = new HashMap<>();
            queueArgs.put("x-message-ttl", 60000); // 设置过期时间为1分钟（以毫秒为单位）
            queueArgs.put("x-dead-letter-exchange", exchangeName); // 设置死信交换机
            queueArgs.put("x-dead-letter-routing-key", "yibi_dlx_routingKey"); // 设置死信队列的routing_key
            channel.queueDeclare(queueName, true, false, false, queueArgs);

            String deadLetterQueueName = "yibi_dlx_queue";
            channel.queueDeclare(deadLetterQueueName, true, false, false, null);

            channel.queueBind(queueName, exchangeName, "yibi_routingKey");
            // 积压任务数
            // channel.basicQos(10);
            log.info("RabbitMQ启动成功");
        } catch (IOException | TimeoutException e) {
            log.error("RabbitMQ启动失败");
            throw new RuntimeException(e);
        }
    }
}
