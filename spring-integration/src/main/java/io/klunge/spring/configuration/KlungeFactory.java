package io.klunge.spring.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.klunge.api.IUserContext;
import io.klunge.api.impl.EmptyUserContext;
import io.klunge.cassandra.CassandraSession;
import io.klunge.common.CommandExecutionInterceptor;
import io.klunge.common.EventExecutionInterceptor;
import io.klunge.common.OperationContext;
import io.klunge.kafka.JsonDeserializer;
import io.klunge.kafka.KafkaOperationRepository;
import io.klunge.kafka.KafkaOperationRepositoryFactory;
import io.klunge.kafka.KafkaProperties;
import io.klunge.kafka.PublishedEventWrapper;
import io.klunge.pojos.Operation;
import io.klunge.spring.filter.OpContextFilter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import javax.annotation.PreDestroy;
import javax.servlet.DispatcherType;
import java.util.EnumSet;

@Slf4j
@Configuration
@Import(SpringKafkaOpListener.class)
public class KlungeFactory {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private KlungeConfiguration klungeConfiguration;

    @Autowired
    private CassandraSession cassandraSession;

    @Bean
    public OperationContext createOperationContext() {
        return new OperationContext();
    }

    @Bean
    CassandraSession cassandraSession() {
        return new CassandraSession(klungeConfiguration.getStoreConfig());
    }

    @PreDestroy
    public void destroy() {
        cassandraSession.destroy();
    }


    @Bean
    public FilterRegistrationBean createOpContextFilter(@Autowired OperationContext operationContext) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new OpContextFilter(operationContext));
        registration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));
        return registration;
    }

    @Bean
    @Scope("prototype")
    public RequestInterceptor opIdInterceptor(@Autowired OperationContext operationContext) {
        return template -> {
            String key = operationContext.getContextOpId();
            if (key != null) {
                template.header(OpContextFilter.OP_ID_HEADER, key);
            }
        };
    }

    @Bean
    public CommandExecutionInterceptor createCommandExecutionInterceptor(@Autowired KafkaOperationRepository kafkaOperationRepository,
                                                                         @Autowired OperationContext operationContext) {
        return new CommandExecutionInterceptor(kafkaOperationRepository, operationContext);
    }

    @Bean
    public EventExecutionInterceptor createEventExecutionInterceptor(@Autowired KafkaOperationRepository kafkaOperationRepository,
                                                                     @Autowired OperationContext operationContext,
                                                                     @Autowired IUserContext userContext) {
        return new EventExecutionInterceptor(kafkaOperationRepository, operationContext, userContext);
    }

    @Bean
    public KafkaOperationRepositoryFactory kafkaOperationRepositoryFactory(@Autowired OperationContext operationContext,
                                                                           IUserContext userContext) {
        KafkaProperties eventBus = klungeConfiguration.getEventBus();
        return new KafkaOperationRepositoryFactory(eventBus, userContext, operationContext);
    }

    @Bean
    public KafkaOperationRepository kafkaOperationRepository(KafkaOperationRepositoryFactory kafkaOperationRepositoryFactory) {
        return kafkaOperationRepositoryFactory.createKafkaOperationRepository(objectMapper);
    }

    @Bean
    public EventMessageConverter eventMessageConverter(OperationContext operationContext, IUserContext userContext, KafkaOperationRepository kafkaOperationRepository) {
        return new EventMessageConverter(objectMapper, operationContext, userContext, kafkaOperationRepository);
    }

    @Bean
    public ConsumerFactory<String, PublishedEventWrapper> kafkaConsumerFactory() {
        KafkaProperties properties = klungeConfiguration.getEventBus().clone();
        properties.getConsumer().setEnableAutoCommit(false);
        return new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties(),
                new StringDeserializer(), new JsonDeserializer<>(PublishedEventWrapper.class, objectMapper));
    }

    @Bean
    public ConsumerFactory<String, Operation> kafkaOperationsFactory() {
        KafkaProperties properties = klungeConfiguration.getEventBus().clone();
        properties.getConsumer().setEnableAutoCommit(false);
        return new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties(),
                new StringDeserializer(), new JsonDeserializer<>(Operation.class, objectMapper));
    }

    @Bean({"eventsKafkaListenerContainerFactory", "kafkaListenerContainerFactory"})
    public ConcurrentKafkaListenerContainerFactory<String, PublishedEventWrapper> eventsKafkaListenerContainerFactory(
            EventMessageConverter eventMessageConverter, ConsumerFactory<String, PublishedEventWrapper> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, PublishedEventWrapper> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(klungeConfiguration.getEventBus().getConsumer().getEventConcurrency());
        factory.setMessageConverter(eventMessageConverter);
        factory.getContainerProperties().setPollTimeout(3000);
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(klungeConfiguration.getEventBus().getConsumer().getEventSchedulerPoolSize());
        scheduler.setBeanName("EventsFactory-Scheduler");
        scheduler.initialize();

        factory.getContainerProperties().setScheduler(scheduler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }

    @Bean("operationsKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Operation> operationsKafkaListenerContainerFactory(
            ConsumerFactory<String, Operation> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Operation> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        RetryTemplate retryTemplate = new RetryTemplate();
        factory.setRetryTemplate(retryTemplate);

        factory.setConcurrency(klungeConfiguration.getEventBus().getConsumer().getOperationSchedulerPoolSize());
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(klungeConfiguration.getEventBus().getConsumer().getOperationSchedulerPoolSize());
        scheduler.setBeanName("OperationsFactory-Scheduler");
        scheduler.initialize();
        factory.getContainerProperties().setScheduler(scheduler);
        ThreadPoolTaskScheduler consumerScheduler = new ThreadPoolTaskScheduler();
        consumerScheduler.setPoolSize(klungeConfiguration.getEventBus().getConsumer().getOperationSchedulerPoolSize());
        consumerScheduler.setBeanName("OperationsFactory-ConsumerScheduler");
        consumerScheduler.initialize();

        factory.getContainerProperties().setPollTimeout(3000L);
        factory.getContainerProperties().setAckOnError(false);
        factory.getContainerProperties().setConsumerTaskExecutor(consumerScheduler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        /**
         * This is Fix for Spring Kafka versions which does not have ConsumerAwareErrorHandler handler till 2.0
         * When Listener faces with error, it retries snapshot operation
         * See https://github.com/klunge/KlungeFramework/issues/44
         */
        factory.getContainerProperties().setTransactionManager(new EmptyTransactionManager());
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean(IUserContext.class)
    public IUserContext getUserContext() {
        return new EmptyUserContext();
    }

    private static class EmptyTransactionManager implements PlatformTransactionManager {
        @Override
        @SuppressFBWarnings("NP_NONNULL_RETURN_VIOLATION")
        public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
            return null;
        }

        @Override
        public void commit(TransactionStatus status) throws TransactionException {

        }

        @Override
        public void rollback(TransactionStatus status) throws TransactionException {

        }
    }

}
