package io.klunge.api.emon.configuration;

import io.klunge.api.emon.service.EventMessageListener;
import io.klunge.api.emon.service.MultipleEventMessageListener;
import io.klunge.kafka.JsonDeserializer;
import io.klunge.kafka.PublishedEventWrapper;
import io.klunge.pojos.Operation;
import io.klunge.spring.configuration.KlungeConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Configuration
@Slf4j
@Import(KlungeConfiguration.class)
public class EventListenConfiguration {

    @Autowired
    private KlungeConfiguration klungeConfiguration;

    @Autowired
    private List<EventMessageListener> eventMessageListeners;

    @Value(value = "${klunge.eventBus.eventTopicRegex:^.+Event$}")
    private String eventTopicRegexStr;

    @Value(value = "${klunge.eventBus.consumerGroupRegex:^(.+-command-query|.+-command)$}")
    private String consumerGroupRegexStr;

    @Bean("eventTopicRegex")
    public Pattern eventTopicRegex() {
        return Pattern.compile(eventTopicRegexStr);
    }

    @Bean("consumerGroupRegex")
    public Pattern consumerGroupRegex() {
        return Pattern.compile(consumerGroupRegexStr);
    }

    @Bean(name = "operationListenerContainer")
    public ConcurrentMessageListenerContainer<String, Operation> operationListenerContainer() {
        Map<String, Object> consumerProperties = klungeConfiguration.getEventBus().buildConsumerProperties();
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        DefaultKafkaConsumerFactory<String, Operation> operationConsumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), new JsonDeserializer<>(Operation.class));

        ContainerProperties containerProperties = new ContainerProperties(Operation.OPERATION_EVENTS);
        containerProperties.setMessageListener(new MultipleEventMessageListener(eventMessageListeners));
        containerProperties.setAckMode(ContainerProperties.AckMode.BATCH);
        ConcurrentMessageListenerContainer<String, Operation> operationListenerContainer = new ConcurrentMessageListenerContainer<>(operationConsumerFactory, containerProperties);
        operationListenerContainer.setBeanName("emon-operations");
        operationListenerContainer.setConcurrency(klungeConfiguration.getEventBus().getConsumer().getOperationConcurrency());
        return operationListenerContainer;
    }

    @Bean(name = "messageListenerContainer")
    public ConcurrentMessageListenerContainer<String, PublishedEventWrapper> messageListenerContainer() {
        Map<String, Object> consumerProperties = klungeConfiguration.getEventBus().buildConsumerProperties();
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProperties.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, 3000);

        DefaultKafkaConsumerFactory<String, PublishedEventWrapper> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties, new StringDeserializer(), new JsonDeserializer<>(PublishedEventWrapper.class));

        ContainerProperties containerProperties = new ContainerProperties(Pattern.compile(eventTopicRegexStr));
        containerProperties.setMessageListener(new MultipleEventMessageListener(eventMessageListeners));
        containerProperties.setAckMode(ContainerProperties.AckMode.BATCH);
        ConcurrentMessageListenerContainer<String, PublishedEventWrapper> messageListenerContainer = new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);
        messageListenerContainer.setConcurrency(klungeConfiguration.getEventBus().getConsumer().getEventConcurrency());
        messageListenerContainer.setBeanName("emon-events");
        return messageListenerContainer;
    }

}
