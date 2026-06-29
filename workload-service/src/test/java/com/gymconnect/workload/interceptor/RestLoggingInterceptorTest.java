package com.gymconnect.workload.interceptor;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RestLoggingInterceptorTest {

    private final RestLoggingInterceptor interceptor = new RestLoggingInterceptor();

    @Test
    void preHandle_shouldReturnTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/workloads");

        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
    }

    @Test
    void afterCompletion_shouldLogSuccess_withoutException() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/workloads/Mike");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        // Should not throw when there is no exception.
        interceptor.afterCompletion(request, response, new Object(), null);
    }

    @Test
    void afterCompletion_shouldLogError_whenExceptionPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/workloads/Mike");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(500);

        // Should not throw when logging an error path.
        interceptor.afterCompletion(request, response, new Object(),
                new RuntimeException("boom"));
    }
}
