package com.gymconnect.client;

import com.gymconnect.security.JwtTokenProvider;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Decorates every outbound Feign request to the workload service with:
 * <ul>
 *   <li>a freshly minted service JWT in the {@code Authorization} header, so the
 *       downstream service can authorise the call (requirement: Bearer-token auth
 *       for microservice integration); and</li>
 *   <li>the current {@code X-Transaction-Id}, so a transaction can be traced across
 *       both microservices.</li>
 * </ul>
 */
@Component
public class WorkloadRequestInterceptor implements RequestInterceptor {

    /** Identity embedded in the service-to-service token. */
    static final String SERVICE_TOKEN_SUBJECT = "gym-main-service";
    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String BEARER_PREFIX = "Bearer ";
    static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String TRANSACTION_ID_KEY = "transactionId";

    private final JwtTokenProvider jwtTokenProvider;

    public WorkloadRequestInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header(AUTHORIZATION_HEADER,
                BEARER_PREFIX + jwtTokenProvider.generateToken(SERVICE_TOKEN_SUBJECT));

        String transactionId = MDC.get(TRANSACTION_ID_KEY);
        if (transactionId != null && !transactionId.isBlank()) {
            template.header(TRANSACTION_ID_HEADER, transactionId);
        }
    }
}
