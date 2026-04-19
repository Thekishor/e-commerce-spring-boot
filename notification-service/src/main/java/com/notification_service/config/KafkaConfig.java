package com.notification_service.config;

import common.events.kafka.OrderEvent;
import common.events.kafka.UserEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Bean
    public ConsumerFactory<String, OrderEvent> consumerFactoryOrderEvent() {
        Map<String, Object> configProp = new HashMap<>();
        configProp.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProp.put(ConsumerConfig.GROUP_ID_CONFIG, "order-event-group");
        configProp.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProp.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProp.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(
                configProp,
                new StringDeserializer(),
                new JacksonJsonDeserializer<>(OrderEvent.class)
        );
    }

    @Bean
    public ConsumerFactory<String, UserEvent> consumerFactoryUserEvent() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "user-event-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new JacksonJsonDeserializer<>(UserEvent.class)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> listenerContainerFactoryOrderEvent() {
        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> containerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        containerFactory.setConsumerFactory(consumerFactoryOrderEvent());
        return containerFactory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserEvent> listenerContainerFactoryUserEvent() {
        ConcurrentKafkaListenerContainerFactory<String, UserEvent> containerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        containerFactory.setConsumerFactory((consumerFactoryUserEvent()));
        return containerFactory;
    }
}
