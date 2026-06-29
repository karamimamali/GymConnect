package com.gymconnect.workload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Trainer-workload (reporting) microservice.
 *
 * <p>Receives ADD/DELETE training events from the main service and maintains, in
 * an in-memory store, the total training duration of every trainer broken down by
 * year and month. Exposes that summary on request.</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
public class WorkloadServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkloadServiceApplication.class, args);
    }
}
