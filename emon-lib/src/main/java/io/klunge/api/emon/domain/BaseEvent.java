package io.klunge.api.emon.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.klunge.common.ReceivedEvent;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseEvent extends ReceivedEvent {
}
