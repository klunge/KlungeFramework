package com.kloia.eventapis.api.impl;

import com.kloia.eventapis.api.IdCreationStrategy;

import java.util.UUID;

/**
 * @author Zeldal Özdemir
 */
public class UUIDCreationStrategy implements IdCreationStrategy {
    @Override
    public String nextId() {
        return UUID.randomUUID().toString();
    }
}
