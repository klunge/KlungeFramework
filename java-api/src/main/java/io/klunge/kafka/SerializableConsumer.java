package io.klunge.kafka;

/**
 * @author Zeldal Özdemir
 */

import java.io.Serializable;
import java.util.function.Consumer;

@FunctionalInterface
public interface SerializableConsumer<T> extends Consumer<T>, Serializable {

}