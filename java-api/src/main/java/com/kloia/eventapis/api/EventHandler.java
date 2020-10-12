package com.kloia.eventapis.api;

import com.kloia.eventapis.common.ReceivedEvent;

/**
 * @author Zeldal Özdemir
 */
public interface EventHandler<D extends ReceivedEvent> {
    Object execute(D event) throws Exception;
}
