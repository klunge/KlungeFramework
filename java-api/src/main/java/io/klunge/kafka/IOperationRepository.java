package io.klunge.kafka;

import io.klunge.pojos.Event;

/**
 * @author Zeldal Özdemir
 */
public interface IOperationRepository {

    void failOperation(String eventId, SerializableConsumer<Event> action);

    void successOperation(String eventId, SerializableConsumer<Event> action);

    void publishEvent(String topic, String event, long opDate);
}
