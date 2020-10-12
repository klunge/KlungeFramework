package io.klunge.view;

import io.klunge.exception.EventStoreException;

/**
 * @author Zeldal Özdemir
 */
@FunctionalInterface
public interface EntityFunction<E, W> {
    E apply(E previous, EntityEventWrapper<W> event) throws EventStoreException;
}