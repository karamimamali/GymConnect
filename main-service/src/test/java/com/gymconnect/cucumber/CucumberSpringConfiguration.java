package com.gymconnect.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Boots the complete main-service application for Cucumber scenarios: real
 * security filter chain, H2 database and an in-VM ActiveMQ broker, so the
 * behaviour under test is the same as in production minus external servers.
 */
@CucumberContextConfiguration
@SpringBootTest(properties = {
        // In-VM broker: JMS scenarios run without an external ActiveMQ instance
        "spring.activemq.broker-url=vm://main-cucumber?broker.persistent=false",
        // No Eureka server is available (or needed) while testing
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
@AutoConfigureMockMvc
public class CucumberSpringConfiguration {
}
