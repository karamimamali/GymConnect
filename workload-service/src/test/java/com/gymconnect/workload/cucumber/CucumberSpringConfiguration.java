package com.gymconnect.workload.cucumber;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Boots the complete workload service for Cucumber scenarios: real security
 * filter chain, an in-process MongoDB (flapdoodle) and an in-VM ActiveMQ
 * broker, so the JMS listener and REST API behave exactly as in production
 * minus external servers.
 */
@CucumberContextConfiguration
@SpringBootTest(properties = {
        // In-VM broker: the @JmsListener consumes without an external ActiveMQ
        "spring.activemq.broker-url=vm://workload-cucumber?broker.persistent=false",
        // Single consumer => events are processed strictly in send order, which
        // keeps the asynchronous scenarios deterministic
        "app.jms.concurrency=1-1",
        "eureka.client.enabled=false"
})
@AutoConfigureMockMvc
public class CucumberSpringConfiguration {

    // Started once for the suite; stopped by the JVM shutdown hook below.
    private static final TransitionWalker.ReachedState<RunningMongodProcess> MONGOD =
            Mongod.instance().start(Version.Main.V7_0);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(MONGOD::close));
    }

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri",
                () -> "mongodb://" + MONGOD.current().getServerAddress() + "/workload-cucumber");
        registry.add("spring.data.mongodb.auto-index-creation", () -> true);
    }
}
