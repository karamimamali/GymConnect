package com.gymconnect.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Service-discovery registry for the GymConnect platform.
 *
 * <p>Every other microservice registers itself here so that they can locate one
 * another by logical service id (e.g. {@code workload-service}) instead of a
 * hard-coded host/port.</p>
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
