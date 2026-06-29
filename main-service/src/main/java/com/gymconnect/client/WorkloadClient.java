package com.gymconnect.client;

import com.gymconnect.dto.WorkloadRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Declarative HTTP client for the workload (reporting) microservice. The target
 * instance is resolved from Eureka by the logical service id {@code workload-service}.
 */
@FeignClient(name = "workload-service")
public interface WorkloadClient {

    @PostMapping("/api/workloads")
    void sendWorkload(@RequestBody WorkloadRequest request);
}
