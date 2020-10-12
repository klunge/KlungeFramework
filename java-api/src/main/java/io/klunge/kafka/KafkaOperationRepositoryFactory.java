package io.klunge.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.klunge.api.IUserContext;
import io.klunge.common.OperationContext;
import io.klunge.pojos.Operation;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaOperationRepositoryFactory {
    private KafkaProperties kafkaProperties;
    private IUserContext userContext;
    private OperationContext operationContext;

    public KafkaOperationRepositoryFactory(KafkaProperties kafkaProperties, IUserContext userContext, OperationContext operationContext) {
        this.kafkaProperties = kafkaProperties;
        this.userContext = userContext;
        this.operationContext = operationContext;
    }

    public KafkaOperationRepository createKafkaOperationRepository(ObjectMapper objectMapper) {
        KafkaProducer<String, Operation> operationsKafka = new KafkaProducer<>(
                kafkaProperties.buildProducerProperties(),
                new StringSerializer(),
                new JsonSerializer<>(objectMapper)
        );
        KafkaProducer<String, PublishedEventWrapper> eventsKafka = new KafkaProducer<>(
                kafkaProperties.buildProducerProperties(),
                new StringSerializer(),
                new JsonSerializer<>(objectMapper)
        );
        return new KafkaOperationRepository(
                operationContext,
                userContext,
                operationsKafka,
                eventsKafka,
                kafkaProperties.getConsumer().getGroupId()
        );
    }
}
