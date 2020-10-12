package io.klunge.spring.configuration;


import io.klunge.api.ViewQuery;
import io.klunge.common.EventKey;
import io.klunge.common.EventRecorder;
import io.klunge.common.RecordedEvent;
import io.klunge.exception.EventStoreException;
import io.klunge.pojos.EventState;
import io.klunge.view.Entity;
import io.klunge.view.SnapshotRepository;

import javax.annotation.Nullable;

public class DataMigrationService<T extends Entity> {

    private EventRecorder eventRecorder;
    private ViewQuery<T> viewQuery;
    private SnapshotRepository<T, String> snapshotRepository;

    public DataMigrationService(EventRecorder eventRecorder, ViewQuery<T> viewQuery, SnapshotRepository<T, String> snapshotRepository) {
        this.eventRecorder = eventRecorder;
        this.viewQuery = viewQuery;
        this.snapshotRepository = snapshotRepository;
    }

    public T updateEvent(EventKey eventKey, boolean snapshot, @Nullable RecordedEvent newEventData, @Nullable EventState newEventState, @Nullable String newEventType) throws EventStoreException {
        eventRecorder.updateEvent(eventKey, newEventData, newEventState, newEventType);
        T entity = viewQuery.queryEntity(eventKey.getEntityId());
        if (snapshot)
            entity = snapshotRepository.save(entity);
        return entity;
    }

    public T updateEvent(EventKey eventKey, boolean snapshot, RecordedEvent newEventData) throws EventStoreException {
        return this.updateEvent(eventKey, snapshot, newEventData, null, null);
    }

    public T snapshotOnly(String entityId) throws EventStoreException {
        T entity = viewQuery.queryEntity(entityId);
        return snapshotRepository.save(entity);
    }
}
