package io.klunge.cassandra;

import io.klunge.common.EventKey;
import io.klunge.pojos.EventState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Zeldal Özdemir
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityEvent {

    private EventKey eventKey;

    private String opId;

    private Date opDate;

    private String eventType;

    private EventState status;

    private String auditInfo;

    private String eventData;


}
