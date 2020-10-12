package io.klunge.api.impl;

import io.klunge.api.IdCreationStrategy;

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
