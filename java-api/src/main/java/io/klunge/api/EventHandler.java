package io.klunge.api;

import io.klunge.common.ReceivedEvent;

/**
 * @author Zeldal Özdemir
 */
public interface EventHandler<D extends ReceivedEvent> {
    Object execute(D event) throws Exception;
}
