package io.klunge.cassandra;

import io.klunge.common.EventKey;
import io.klunge.exception.EventStoreException;

public class DefaultConcurrencyResolver implements ConcurrencyResolver<ConcurrentEventException> {
    @Override
    public void tryMore() throws ConcurrentEventException {
        throw new ConcurrentEventException("Concurrent Events");
    }

    @Override
    public EventKey calculateNext(EventKey entityEvent, int lastVersion) throws EventStoreException, ConcurrentEventException {
        throw new ConcurrentEventException("Concurrent Events for:" + entityEvent);
    }
}
