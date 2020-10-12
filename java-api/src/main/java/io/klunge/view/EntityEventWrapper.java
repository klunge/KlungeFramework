package io.klunge.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.klunge.cassandra.EntityEvent;
import io.klunge.exception.EventStoreException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * @author Zeldal Özdemir
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityEventWrapper<E> {

    private Class<E> type;
    private ObjectMapper objectMapper;
    private EntityEvent entityEvent;

    public E getEventData() throws EventStoreException {
        try {
            return objectMapper.readValue(entityEvent.getEventData(), type);
        } catch (IOException e) {
            throw new EventStoreException(e.getMessage(), e);
        }
    }
}
