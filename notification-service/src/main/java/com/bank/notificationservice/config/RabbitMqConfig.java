package com.bank.notificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    
    public static final String TRANSACTION_EXCHANGE = "transaction.exchange";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String TRANSACTION_ROUTING_KEY = "transaction.event";
    
    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(TRANSACTION_EXCHANGE);
    }
    
    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }
    
    @Bean
    public Binding transactionBinding(Queue notificationQueue, TopicExchange transactionExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(transactionExchange)
                .with(TRANSACTION_ROUTING_KEY);
    }
    
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
