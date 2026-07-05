package com.gymconnect.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gymconnect.dto.WorkloadRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.Map;

/**
 * JMS configuration for the outbound integration with the workload service.
 *
 * <p>Messages are serialized as JSON text (not Java-serialized objects), so the
 * consumer does not need this service's classes on its classpath. The logical
 * type id {@value #WORKLOAD_TYPE_ID} is carried in the {@code _type} message
 * property and mapped to the consumer's own DTO on the other side.</p>
 *
 * <p>The broker connection itself ({@code spring.activemq.*}) is auto-configured
 * by Spring Boot and varies per profile — see {@code application-*.properties}.</p>
 */
@Configuration
public class JmsConfig {

    /** Broker-neutral type id shared by producer and consumer. */
    public static final String WORKLOAD_TYPE_ID = "TrainerWorkloadMessage";

    /** Message property carrying the type id used to resolve the target DTO. */
    public static final String TYPE_ID_PROPERTY = "_type";

    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(jmsObjectMapper());
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName(TYPE_ID_PROPERTY);
        converter.setTypeIdMappings(Map.of(WORKLOAD_TYPE_ID, WorkloadRequest.class));
        return converter;
    }

    private ObjectMapper jmsObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        // LocalDate must travel as ISO-8601 text ("2026-05-10"), not an epoch array.
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}
