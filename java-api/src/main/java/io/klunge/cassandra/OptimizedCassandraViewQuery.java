package io.klunge.cassandra;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.klunge.common.EventKey;
import io.klunge.exception.EventStoreException;
import io.klunge.view.Entity;
import io.klunge.view.EntityFunctionSpec;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Zeldal Özdemir
 */
@Slf4j
public class OptimizedCassandraViewQuery<E extends Entity> extends BaseCassandraViewQuery<E> {

    Function<String, E> findOne;

    public OptimizedCassandraViewQuery(String tableName, CassandraSession cassandraSession,
                                       ObjectMapper objectMapper,
                                       List<EntityFunctionSpec<E, ?>> commandSpecs,
                                       Function<String, E> findOne) {
        super(tableName, cassandraSession, objectMapper, commandSpecs);
        this.findOne = findOne;
    }

    @Override
    public E queryEntity(String entityId) throws EventStoreException {
        E existing = findOne.apply(entityId);
        if (existing != null)
            return super.queryEntity(entityId, Integer.MAX_VALUE, existing);
        else
            return super.queryEntity(entityId);
    }

    @Override
    public E queryEntity(EventKey eventKey) throws EventStoreException {
        return this.queryEntity(eventKey.getEntityId(), eventKey.getVersion());
    }

    @Override
    public E queryEntity(String entityId, int version) throws EventStoreException {
        E existing = findOne.apply(entityId);
        if (existing != null && existing.getVersion() < version)
            return super.queryEntity(entityId, version, existing);
        else
            return super.queryEntity(entityId, version);
    }

    @Override
    public List<E> queryByOpId(String opId) throws EventStoreException {
        return queryByOpId(opId, findOne);
    }

    @Override
    public List<E> queryByOpId(String opId, Function<String, E> givenFindOne) throws EventStoreException {
        List<EventKey> eventKeys = super.queryEventKeysByOpId(opId);
        List<E> entities = new ArrayList<>();
        for (EventKey eventKey : eventKeys) {
            entities.add(queryEntity(eventKey.getEntityId()));
        }
        return entities;
    }

    @Override
    public List<EventKey> queryEventKeysByOpId(String opId) {
        return super.queryEventKeysByOpId(opId);
    }
}
