package com.gymconnect.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RestLoggingInterceptorTest {

    private final RestLoggingInterceptor interceptor = new RestLoggingInterceptor();

    @Test
    void preHandleLogsRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/trainees/user1");
        HttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    void afterCompletionLogsResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/trainees/user1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);

        interceptor.afterCompletion(request, response, new Object(), null);
    }

    @Test
    void afterCompletionLogsError() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/trainees/user1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(500);

        interceptor.afterCompletion(request, response, new Object(),
                new RuntimeException("test error"));
    }
}
