package io.klunge.common;


import io.klunge.api.IUserContext;
import io.klunge.kafka.KafkaOperationRepository;
import io.klunge.pojos.EventState;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

/**
 * @author Zeldal Özdemir
 */
@Aspect
@Slf4j
@SuppressWarnings("checkstyle:IllegalThrows")
public class EventExecutionInterceptor {

    private KafkaOperationRepository kafkaOperationRepository;
    private OperationContext operationContext;
    private IUserContext userContext;

    public EventExecutionInterceptor(KafkaOperationRepository kafkaOperationRepository, OperationContext operationContext, IUserContext userContext) {
        this.operationContext = operationContext;
        this.kafkaOperationRepository = kafkaOperationRepository;
        this.userContext = userContext;
    }

    @AfterReturning(value = "this(io.klunge.api.EventHandler+) && execution(* execute(..))", returning = "retVal")
    public void afterReturning(Object retVal) throws Throwable {
        log.debug("AfterReturning:" + (retVal == null ? "" : retVal.toString()));
        operationContext.clearCommandContext();
        userContext.clearUserContext();
    }

    @AfterThrowing(value = "this(io.klunge.api.EventHandler+) && execution(* execute(..))", throwing = "exception")
    public void afterThrowing(Exception exception) throws Throwable {
        try {
            log.debug("afterThrowing EventHandler method:" + exception.getMessage());
            kafkaOperationRepository.failOperation(operationContext.getCommandContext(), event -> event.setEventState(EventState.TXN_FAILED));
        } finally {
            operationContext.clearCommandContext();
            userContext.clearUserContext();
        }
    }
}