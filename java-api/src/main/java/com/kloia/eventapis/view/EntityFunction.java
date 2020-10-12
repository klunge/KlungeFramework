package com.kloia.eventapis.view;

import com.kloia.eventapis.exception.EventStoreException;

/**
 * @author Zeldal Özdemir
 */
@FunctionalInterface
public interface EntityFunction<E, W> {
    E apply(E previous, EntityEventWrapper<W> event) throws EventStoreException;
}