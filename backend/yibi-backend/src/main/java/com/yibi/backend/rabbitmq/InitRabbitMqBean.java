package com.yibi.backend.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
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
            String exchange_name = "yibi_exchange";
            channel.exchangeDeclare(exchange_name, "direct");
            String queueName = "yibi_queue";
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, exchange_name, "yibi_routingKey");
            log.info("RabbitMQ启动成功");
        } catch (IOException | TimeoutException e) {
            log.error("RabbitMQ启动失败");
            throw new RuntimeException(e);
        }
    }
}
