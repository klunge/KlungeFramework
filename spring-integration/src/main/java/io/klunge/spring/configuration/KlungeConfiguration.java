package io.klunge.spring.configuration;

import io.klunge.cassandra.EventStoreConfig;
import io.klunge.kafka.KafkaProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConfigurationProperties("klunge")
@Component
@Slf4j
@Data
public class KlungeConfiguration {
    private EventStoreConfig storeConfig;
    private KafkaProperties eventBus;
    private Map<String, String> eventRecords;
    private String baseEventsPackage;

    public String getTableNameForEvents(String eventName) {
        return getEventRecords().getOrDefault(eventName, eventName);
    }
}


