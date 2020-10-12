package io.klunge.api;

import io.klunge.cassandra.ConcurrencyResolver;
import io.klunge.cassandra.ConcurrentEventException;
import io.klunge.cassandra.ConcurrentEventResolver;
import io.klunge.cassandra.EntityEvent;
import io.klunge.common.EventKey;
import io.klunge.common.EventRecorder;
import io.klunge.common.PublishedEvent;
import io.klunge.exception.EventStoreException;
import io.klunge.view.Entity;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Zeldal Özdemir
 */
public interface EventRepository {

    List<EntityEvent> markFail(String opId);

    <P extends PublishedEvent> EventKey recordAndPublish(P publishedEvent) throws EventStoreException, ConcurrentEventException;

    <P extends PublishedEvent> EventKey recordAndPublish(Entity entity, P publishedEvent) throws EventStoreException, ConcurrentEventException;

    <P extends PublishedEvent> EventKey recordAndPublish(EventKey eventKey, P publishedEvent) throws EventStoreException, ConcurrentEventException;

    <P extends PublishedEvent, T extends Exception> EventKey recordAndPublish(
            Entity entity, P publishedEvent, Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory
    ) throws EventStoreException, T;

    <P extends PublishedEvent, T extends Exception> EventKey recordAndPublish(
            EventKey eventKey, P publishedEvent, Function<EntityEvent, ConcurrencyResolver<T>> concurrencyResolverFactory
    ) throws EventStoreException, T;

    <P extends PublishedEvent, T extends Exception> EventKey recordAndPublish(
            Entity entity, P publishedEvent, Supplier<ConcurrentEventResolver<P, T>> concurrencyResolverFactory
    ) throws EventStoreException, T;

    <P extends PublishedEvent, T extends Exception> EventKey recordAndPublish(
            EventKey eventKey, P publishedEvent, Supplier<ConcurrentEventResolver<P, T>> concurrencyResolverFactory
    ) throws EventStoreException, T;

    EventRecorder getEventRecorder();

}
