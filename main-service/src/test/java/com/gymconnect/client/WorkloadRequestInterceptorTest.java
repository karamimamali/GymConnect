package com.gymconnect.client;

import com.gymconnect.security.JwtTokenProvider;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkloadRequestInterceptorTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void apply_shouldAddBearerToken() {
        when(jwtTokenProvider.generateToken(WorkloadRequestInterceptor.SERVICE_TOKEN_SUBJECT))
                .thenReturn("signed-token");
        WorkloadRequestInterceptor interceptor = new WorkloadRequestInterceptor(jwtTokenProvider);
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertEquals("Bearer signed-token",
                template.headers().get(WorkloadRequestInterceptor.AUTHORIZATION_HEADER)
                        .iterator().next());
    }

    @Test
    void apply_shouldForwardTransactionId_whenPresentInMdc() {
        when(jwtTokenProvider.generateToken(WorkloadRequestInterceptor.SERVICE_TOKEN_SUBJECT))
                .thenReturn("signed-token");
        MDC.put("transactionId", "txn-42");
        WorkloadRequestInterceptor interceptor = new WorkloadRequestInterceptor(jwtTokenProvider);
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertEquals("txn-42",
                template.headers().get(WorkloadRequestInterceptor.TRANSACTION_ID_HEADER)
                        .iterator().next());
    }

    @Test
    void apply_shouldNotAddTransactionId_whenAbsent() {
        when(jwtTokenProvider.generateToken(WorkloadRequestInterceptor.SERVICE_TOKEN_SUBJECT))
                .thenReturn("signed-token");
        WorkloadRequestInterceptor interceptor = new WorkloadRequestInterceptor(jwtTokenProvider);
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertFalse(template.headers().containsKey(WorkloadRequestInterceptor.TRANSACTION_ID_HEADER));
        assertTrue(template.headers().containsKey(WorkloadRequestInterceptor.AUTHORIZATION_HEADER));
    }
}
