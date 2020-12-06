package io.klunge.cassandra;

import io.klunge.common.EventKey;
import io.klunge.common.RecordedEvent;
import io.klunge.exception.EventStoreException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public interface ConcurrencyResolver<T extends Exception> extends ConcurrentEventResolver<RecordedEvent, T> {

    void tryMore() throws T;

    EventKey calculateNext(EventKey failedEventKey, int lastVersion) throws T, EventStoreException;

    default Pair<EventKey, RecordedEvent> calculateNext(RecordedEvent failedEvent, EventKey failedEventKey, int lastVersion) throws T, EventStoreException {
        return new ImmutablePair<>(calculateNext(failedEventKey, lastVersion), failedEvent);
    }

}
