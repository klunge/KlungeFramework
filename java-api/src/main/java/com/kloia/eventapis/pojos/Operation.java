package com.kloia.eventapis.pojos;

import com.kloia.eventapis.common.Context;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Zeldal Özdemir
 */
@Data
@ToString(exclude = "userContext")
@AllArgsConstructor
@NoArgsConstructor
public class Operation implements Serializable {
    public static final String OPERATION_EVENTS = "operation-events";
    private static final long serialVersionUID = -2003849346218727591L;

    private TransactionState transactionState;
    private String aggregateId;
    private String sender;
    private String parentId; // alias for as context.getParentOpId()
    private Context context;
    private long opDate;
    private Map<String, String> userContext;

    /**
     * Backward compatible.
     *
     * @return
     */
    public String getParentId() {
        return parentId != null ? parentId : (context != null ? context.getParentOpId() : null);
    }
}
