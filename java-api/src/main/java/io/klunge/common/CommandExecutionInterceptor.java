package io.klunge.common;


import io.klunge.api.Command;
import io.klunge.api.CommandHandler;
import io.klunge.api.EventRepository;
import io.klunge.cassandra.ConcurrentEventException;
import io.klunge.cassandra.DefaultConcurrencyResolver;
import io.klunge.exception.EventStoreException;
import io.klunge.kafka.KafkaOperationRepository;
import io.klunge.pojos.CommandRecord;
import io.klunge.pojos.EventState;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * @author Zeldal Özdemir
 */
@Aspect
@Slf4j
@SuppressWarnings("checkstyle:IllegalThrows")
public class CommandExecutionInterceptor {

    private KafkaOperationRepository kafkaOperationRepository;
    private OperationContext operationContext;

    public CommandExecutionInterceptor(KafkaOperationRepository kafkaOperationRepository, OperationContext operationContext) {
        this.operationContext = operationContext;
        this.kafkaOperationRepository = kafkaOperationRepository;
    }


    @Before("this(io.klunge.api.CommandHandler) && @annotation(command)")
    public void before(JoinPoint jp, Command command) throws Throwable {
        Object target = jp.getTarget();
        if (!(target instanceof CommandHandler))
            throw new IllegalArgumentException("Point is not Instance of CommandHandler");
        CommandHandler commandHandler = (CommandHandler) target;
        long commandTimeout = command.commandTimeout();
        operationContext.startNewContext(commandTimeout); // Ability to generate new Context
        operationContext.setCommandContext(target.getClass().getSimpleName());
        CommandRecord commandDto = recordCommand(jp, commandHandler, command);
        log.debug("before method:" + (commandDto == null ? "" : commandDto.toString()));
    }

    private CommandRecord recordCommand(JoinPoint jp, CommandHandler commandHandler, Command command) throws ConcurrentEventException, EventStoreException {
        EventRepository eventRepository;
        CommandRecord commandRecord = new CommandRecord();
        commandRecord.setEventName(commandHandler.getClass().getSimpleName());
        for (int i = 0; i < jp.getArgs().length; i++) {
            Object arg = jp.getArgs()[i];
            commandRecord.getParameters().put(i, arg);
        }
        try {
            Field declaredField = commandHandler.getClass().getDeclaredField(command.eventRepository());
            if (!declaredField.isAccessible())
                declaredField.setAccessible(true);
            eventRepository = (EventRepository) declaredField.get(commandHandler);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            log.error("Error while accessing EventRecorder(" + command.eventRepository() + ") of Command:" + commandHandler.getClass().getSimpleName() + " message: " + e.getMessage(), e);
            return null;
        }
        if (eventRepository != null) {
            eventRepository.getEventRecorder().recordEntityEvent(commandRecord, System.currentTimeMillis(), Optional.empty(), entityEvent -> new DefaultConcurrencyResolver());
        } else
            log.error("Error while accessing EventRecorder(" + command.eventRepository() + " is null ) of Command:" + commandHandler.getClass().getSimpleName());
        return commandRecord;
    }

    @AfterReturning(value = "this(io.klunge.api.CommandHandler) && @annotation(command)", returning = "retVal")
    public void afterReturning(Command command, Object retVal) {
        log.debug("AfterReturning:" + (retVal == null ? "" : retVal.toString()));
        operationContext.clearCommandContext();
    }

    @AfterThrowing(value = "this(io.klunge.api.CommandHandler) && @annotation(command)", throwing = "exception")
    public void afterThrowing(Command command, Exception exception) {
        try {
            log.info("afterThrowing Command: " + exception);
            kafkaOperationRepository.failOperation(operationContext.getCommandContext(), event -> event.setEventState(EventState.TXN_FAILED));
        } finally {
            operationContext.clearCommandContext();
        }
    }
}