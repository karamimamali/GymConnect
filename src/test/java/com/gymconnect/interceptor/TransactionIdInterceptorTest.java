package com.gymconnect.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransactionIdInterceptorTest {

    private final TransactionIdInterceptor interceptor = new TransactionIdInterceptor();

    @Test
    void preHandleSetsTransactionId() throws Exception {
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assert result;
        assertNotNull(MDC.get("transactionId"));
        assertNotNull(response.getHeader("X-Transaction-Id"));
    }

    @Test
    void afterCompletionClearsMdc() throws Exception {
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        MDC.put("transactionId", "test-id");
        interceptor.afterCompletion(request, response, new Object(), null);

        assertNull(MDC.get("transactionId"));
    }
}
