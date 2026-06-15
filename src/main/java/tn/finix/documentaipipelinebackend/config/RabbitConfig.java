package tn.finix.documentaipipelinebackend.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitConfig {

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    public static final String EXCHANGE = "document.exchange";

    public static final String QUEUE = "document.processing";

    public static final String DLQ = "document.processing.dlq";

    public static final String ROUTING_KEY = "document.process";

    public static final String DLQ_ROUTING_KEY = "document.process.dlq";

    //the mail agent  receives messages and routes to queue, btw not written by ai ena nekteb fehom just saying
    @Bean
    public DirectExchange documentExchange() {
        return new DirectExchange(EXCHANGE);
    }

    //the letter box
    @Bean
    public Queue documentQueue() {
        return QueueBuilder
                .durable(QUEUE)
                .deadLetterExchange(EXCHANGE)
                .deadLetterRoutingKey(DLQ_ROUTING_KEY)
                .build();
    }

    //letter box in case of failure
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder
                .durable(DLQ)
                .build();
    }

    //the binding between the letter box and the mail agent
    @Bean
    public Binding documentBinding() {
        return BindingBuilder
                .bind(documentQueue())
                .to(documentExchange())
                .with(ROUTING_KEY);
    }

    //the binding between the letter box in case of failure and the mail agent
    @Bean
    public Binding dlqBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(documentExchange())
                .with(DLQ_ROUTING_KEY);
    }
}