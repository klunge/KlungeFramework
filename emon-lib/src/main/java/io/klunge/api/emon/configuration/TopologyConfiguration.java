package io.klunge.api.emon.configuration;

import io.klunge.kafka.JsonDeserializer;
import io.klunge.pojos.Operation;
import io.klunge.spring.configuration.KlungeConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.Properties;

@Configuration
@Import(KlungeConfiguration.class)
@Slf4j
@ConditionalOnProperty(value = "emon.offsetScheduler.enabled", havingValue = "true")
public class TopologyConfiguration {

    @Autowired
    private KlungeConfiguration klungeConfiguration;


    @Bean(name = "kafkaAdminProperties")
    public Properties kafkaAdminProperties() {
        String bootstrapServers = String.join(",", klungeConfiguration.getEventBus().getBootstrapServers());
        String zookeeperServers = String.join(",", klungeConfiguration.getEventBus().getZookeeperServers());
        Properties properties = new Properties();
        properties.putAll(klungeConfiguration.getEventBus().buildCommonProperties());
        properties.put("zookeeper", zookeeperServers);
        properties.put("bootstrap.servers", bootstrapServers);
        return properties;
    }

    @Bean("adminClient")
    public AdminClient adminClient(@Autowired @Qualifier("kafkaAdminProperties") Properties kafkaAdminProperties) {
        return AdminClient.create(kafkaAdminProperties);
    }

    @Bean
    public Consumer<String, Operation> kafkaConsumer() {
        return new DefaultKafkaConsumerFactory<>(
                klungeConfiguration.getEventBus().buildConsumerProperties(),
                new StringDeserializer(),
                new JsonDeserializer<>(Operation.class)).createConsumer();
    }


}
