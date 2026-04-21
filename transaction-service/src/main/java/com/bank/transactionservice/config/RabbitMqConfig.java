package com.bank.transactionservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${rabbitmq.queue.transaction.name:transaction.queue}")
    private String queueName;

    @Value("${rabbitmq.exchange.transaction.name:transaction.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routingkey.transaction.name:transaction.routingkey}")
    private String routingKeyName;

    @Bean
    public Queue transactionQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public DirectExchange transactionExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding transactionBinding(Queue transactionQueue, DirectExchange transactionExchange) {
        return BindingBuilder.bind(transactionQueue)
                .to(transactionExchange)
                .with(routingKeyName);
    }
}
