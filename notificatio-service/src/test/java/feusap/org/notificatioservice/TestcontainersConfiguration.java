package feusap.org.notificatioservice;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka-native:latest"));
    }

    /**
     * The producer/consumer factories are built manually from
     * spring.kafka.bootstrap-servers, so point that property at the container.
     */
    @Bean
    DynamicPropertyRegistrar kafkaBootstrapServers(KafkaContainer kafka) {
        return registry -> registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

}
