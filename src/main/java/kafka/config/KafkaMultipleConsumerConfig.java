package kafka.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class KafkaMultipleConsumerConfig {

    private final KafkaCustomProperties kafkaCustomProperties;

    @Bean
    @Qualifier("json")
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory("json"));
        return factory;
    }

    @Bean
    @Qualifier("string")
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerStringContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory("string"));
        return factory;
    }

    @Bean
    @Qualifier("byteArray")
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerByteArrayContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory("byteArray"));
        return factory;
    }

    private ConsumerFactory<String, Object> consumerFactory(String consumerName) {
        Map<String, Object> properties = new HashMap<>(kafkaCustomProperties.buildCommonProperties());
        if (nonNull(kafkaCustomProperties.getConsumer())) {
            KafkaProperties.Consumer consumerProperties = kafkaCustomProperties.getConsumer().get(consumerName);
            if (nonNull(consumerProperties)) {
                properties.putAll(consumerProperties.buildProperties(null));
            }
        }
        log.info("Kafka Consumer '{}' properties: {}", consumerName, properties);
        return new DefaultKafkaConsumerFactory<>(properties);
    }
}
