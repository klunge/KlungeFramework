package io.klunge.api.emon.domain;

import io.klunge.common.Context;
import io.klunge.pojos.Operation;
import io.klunge.pojos.TransactionState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Zeldal Özdemir
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationEvent implements Serializable {


    private static final long serialVersionUID = 3269757297830153667L;
    private TransactionState transactionState;
    private String aggregateId;
    private String sender;
    private long opDate;
    private Context context;

    public OperationEvent(Operation operation) {
        this.transactionState = operation.getTransactionState();
        this.aggregateId = operation.getAggregateId();
        this.sender = operation.getSender();
        this.context = operation.getContext();
        this.opDate = operation.getOpDate() != 0L ? operation.getOpDate() : System.currentTimeMillis();
    }
}
