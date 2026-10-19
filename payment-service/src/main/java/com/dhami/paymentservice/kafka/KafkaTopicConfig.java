package com.dhami.paymentservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

// Configures Kafka topics required by the payment service.
@Configuration
public class KafkaTopicConfig {

    // Defines the payment.succeeded topic with 3 partitions and 1 replica.
    @Bean
    public NewTopic paymentSucceededTopic() {
        // TopicBuilder.name("payment.succeeded")
        return TopicBuilder.name(KafkaTopics.PAYMENT_SUCCEEDED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    //  Defines the payment.failed topic with 3 partitions and 1 replica.
    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(KafkaTopics.PAYMENT_FAILED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}

/*
NewTopic -
NewTopic is a class provided by the Apache Kafka client library.
It represents a Kafka topic definition, including its name, number of partitions,
and replication factor.

TopicBuilder -
TopicBuilder is a Spring Kafka utility class that provides a fluent API for constructing NewTopic objects.
 */