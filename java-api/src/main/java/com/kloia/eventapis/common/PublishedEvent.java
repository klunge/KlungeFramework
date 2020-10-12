package com.kloia.eventapis.common;


import com.fasterxml.jackson.annotation.JsonView;
import com.kloia.eventapis.api.Views;
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
