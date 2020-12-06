package io.klunge.common;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Zeldal Özdemir
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class ReceivedEvent implements RecordedEvent {

    private EventKey sender;

    private EventType eventType;

    public EventType getEventType() {
        return eventType;
    }
}
