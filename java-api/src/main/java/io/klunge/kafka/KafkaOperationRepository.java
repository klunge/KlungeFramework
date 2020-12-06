package io.klunge.kafka;

import io.klunge.api.IUserContext;
import io.klunge.common.OperationContext;
import io.klunge.pojos.Event;
import io.klunge.pojos.Operation;
import io.klunge.pojos.TransactionState;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * @author Zeldal Özdemir
 */
@Slf4j
public class KafkaOperationRepository implements IOperationRepository {
    private OperationContext operationContext;
    private IUserContext userContext;
    private KafkaProducer<String, Operation> operationsKafka;
    private KafkaProducer<String, PublishedEventWrapper> eventsKafka;
    private String senderGroupId;

    public KafkaOperationRepository(OperationContext operationContext,
                                    IUserContext userContext, KafkaProducer<String, Operation> operationsKafka,
                                    KafkaProducer<String, PublishedEventWrapper> eventsKafka,
                                    String senderGroupId) {
        this.operationContext = operationContext;
        this.userContext = userContext;
        this.operationsKafka = operationsKafka;
        this.eventsKafka = eventsKafka;
        this.senderGroupId = senderGroupId;
    }

    @Override
    public void failOperation(String eventId, SerializableConsumer<Event> action) {
        Operation operation = new Operation();
        operation.setSender(senderGroupId);
        operation.setAggregateId(eventId);
        operation.setUserContext(userContext.getUserContext());
        operation.setContext(operationContext.getContext());
        operation.setTransactionState(TransactionState.TXN_FAILED);
        operation.setOpDate(System.currentTimeMillis());
        log.debug("Publishing Operation:" + operation.toString());
        operationsKafka.send(new ProducerRecord<>(Operation.OPERATION_EVENTS, operationContext.getContext().getOpId(), operation));
    }

    @Override
    public void successOperation(String eventId, SerializableConsumer<Event> action) {
        Operation operation = new Operation();
        operation.setSender(senderGroupId);
        operation.setAggregateId(eventId);
        operation.setTransactionState(TransactionState.TXN_SUCCEEDED);
        operation.setUserContext(userContext.getUserContext());
        operation.setContext(operationContext.getContext());
        operation.setOpDate(System.currentTimeMillis());
        log.debug("Publishing Operation:" + operation.toString());
        operationsKafka.send(new ProducerRecord<>(Operation.OPERATION_EVENTS, operationContext.getContext().getOpId(), operation));
    }

    @Override
    public void publishEvent(String topic, String event, long opDate) {
        PublishedEventWrapper publishedEventWrapper = new PublishedEventWrapper(operationContext.getContext(), event, opDate);
        publishedEventWrapper.setUserContext(userContext.getUserContext());
        publishedEventWrapper.setSender(senderGroupId);
        log.debug("Publishing Topic:" + topic + " Event:" + publishedEventWrapper.toString());
        eventsKafka.send(new ProducerRecord<>(topic, operationContext.getContext().getOpId(), publishedEventWrapper));
    }

}
