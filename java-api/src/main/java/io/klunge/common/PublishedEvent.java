package io.klunge.common;


import com.fasterxml.jackson.annotation.JsonView;
import io.klunge.api.Views;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Zeldal Özdemir
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class PublishedEvent extends ReceivedEvent {

    @JsonView(Views.PublishedOnly.class)
    EventKey sender;

    public abstract EventType getEventType();
}
