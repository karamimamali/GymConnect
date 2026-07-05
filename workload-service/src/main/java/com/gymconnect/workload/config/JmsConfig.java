package com.gymconnect.workload.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gymconnect.workload.dto.TrainerWorkloadRequest;
import jakarta.jms.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.Map;

/**
 * JMS configuration for consuming trainer workload events from ActiveMQ.
 *
 * <p>Messages travel as JSON text; the {@code _type} property carries the logical
 * type id {@value #WORKLOAD_TYPE_ID} agreed with the producer, mapped here to this
 * service's own {@link TrainerWorkloadRequest} DTO so neither service needs the
 * other's classes.</p>
 *
 * <p>Sessions are transacted: if a listener throws, the message is redelivered and,
 * once the broker's redelivery policy is exhausted, moved to the broker DLQ. That
 * covers unreadable messages (e.g. malformed JSON); payloads that parse but miss
 * required data are routed to the application DLQ by the listener itself.</p>
 */
@Configuration
@EnableJms
public class JmsConfig {

    /** Broker-neutral type id shared by producer and consumer. */
    public static final String WORKLOAD_TYPE_ID = "TrainerWorkloadMessage";

    /** Message property carrying the type id used to resolve the target DTO. */
    public static final String TYPE_ID_PROPERTY = "_type";

    private static final Logger logger = LoggerFactory.getLogger(JmsConfig.class);

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(jmsObjectMapper());
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName(TYPE_ID_PROPERTY);
        converter.setTypeIdMappings(Map.of(WORKLOAD_TYPE_ID, TrainerWorkloadRequest.class));
        return converter;
    }

    /**
     * Listener container for the workload queue. The concurrency range is
     * profile-driven ({@code app.jms.concurrency}) so the number of consumer
     * threads can be scaled horizontally per environment without a code change.
     */
    @Bean
    public DefaultJmsListenerContainerFactory workloadListenerContainerFactory(
            ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer,
            MessageConverter jacksonJmsMessageConverter,
            @Value("${app.jms.concurrency:1-5}") String concurrency) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(jacksonJmsMessageConverter);
        factory.setConcurrency(concurrency);
        factory.setSessionTransacted(true);
        factory.setErrorHandler(throwable ->
                logger.error("Workload listener failed; message will be redelivered "
                        + "(broker DLQ after redelivery policy is exhausted)", throwable));
        logger.info("Workload JMS listener configured with concurrency {}", concurrency);
        return factory;
    }

    private ObjectMapper jmsObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // LocalDate travels as ISO-8601 text ("2026-05-10"), matching the producer.
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
