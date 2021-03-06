package com.kloia.eventapis.spring.configuration;

import com.kloia.eventapis.api.IUserContext;
import com.kloia.eventapis.exception.EventStoreException;
import com.kloia.eventapis.pojos.Operation;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class SpringKafkaOpListener {
    @Autowired(required = false)
    AggregateListenerService aggregateListenerService;

    @Autowired
    IUserContext userContext;

    @KafkaListener(topics = Operation.OPERATION_EVENTS, containerFactory = "operationsKafkaListenerContainerFactory")
    void listenOperations(ConsumerRecord<String, Operation> record) throws EventStoreException {
        String key = record.key();
        Operation value = record.value();
        log.debug("Trying Snapshot: " + key + " " + value);
        userContext.extractUserContext(value.getUserContext());
        aggregateListenerService.listenOperations(record);
    }

    public void recover(Exception exception) throws Exception {
        log.error("Operation Handle is failed");
        throw exception;
    }
}
