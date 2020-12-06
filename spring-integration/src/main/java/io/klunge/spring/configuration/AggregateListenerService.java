package io.klunge.spring.configuration;

import io.klunge.exception.EventStoreException;
import io.klunge.pojos.Operation;
import io.klunge.view.AggregateListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class AggregateListenerService {
    @Autowired(required = false)
    List<AggregateListener> aggregateListeners;


    @Transactional(rollbackFor = Exception.class)
    public void listenOperations(ConsumerRecord<String, Operation> record) throws EventStoreException {
        for (AggregateListener snapshotRecorder : aggregateListeners) {
            snapshotRecorder.listenOperations(record);
        }
    }
}
