package com.gymconnect.workload.interceptor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionIdInterceptorTest {

    private final TransactionIdInterceptor interceptor = new TransactionIdInterceptor();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void preHandle_shouldReuseIncomingTransactionId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TransactionIdInterceptor.TRANSACTION_ID_HEADER, "upstream-txn");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertEquals("upstream-txn", MDC.get("transactionId"));
        assertEquals("upstream-txn", response.getHeader(TransactionIdInterceptor.TRANSACTION_ID_HEADER));
    }

    @Test
    void preHandle_shouldGenerateTransactionId_whenHeaderMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());

        String generated = MDC.get("transactionId");
        assertNotNull(generated);
        assertEquals(generated, response.getHeader(TransactionIdInterceptor.TRANSACTION_ID_HEADER));
    }

    @Test
    void preHandle_shouldGenerateTransactionId_whenHeaderBlank() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TransactionIdInterceptor.TRANSACTION_ID_HEADER, "   ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, new Object());

        String generated = MDC.get("transactionId");
        assertNotNull(generated);
        assertEquals("   ".equals(generated), false);
    }

    @Test
    void afterCompletion_shouldClearMdc() {
        MDC.put("transactionId", "to-be-cleared");

        interceptor.afterCompletion(new MockHttpServletRequest(),
                new MockHttpServletResponse(), new Object(), null);

        assertNull(MDC.get("transactionId"));
    }
}
