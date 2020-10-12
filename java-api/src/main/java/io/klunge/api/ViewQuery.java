package io.klunge.api;


import io.klunge.cassandra.EntityEvent;
import io.klunge.common.EventKey;
import io.klunge.common.PublishedEvent;
import io.klunge.exception.EventStoreException;
import io.klunge.view.Entity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

/**
 * @author Zeldal Özdemir
 */
public interface ViewQuery<E extends Entity> {
    E queryEntity(String entityId) throws EventStoreException;

    E queryEntity(String entityId, int version) throws EventStoreException;

    E queryEntity(EventKey eventKey) throws EventStoreException;

    E queryEntity(String entityId, @Nullable Integer version, E previousEntity) throws EventStoreException;

    List<E> queryByOpId(String opId) throws EventStoreException;

    List<E> queryByOpId(String key, Function<String, E> findOne) throws EventStoreException;

    List<EntityEvent> queryHistory(String entityId) throws EventStoreException;

    EntityEvent queryEvent(String entityId, int version) throws EventStoreException;

    <T extends PublishedEvent> T queryEventData(String entityId, int version) throws EventStoreException;

    List<EventKey> queryEventKeysByOpId(String opId);
}
