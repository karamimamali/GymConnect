package com.gymconnect.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class TransactionIdInterceptor implements HandlerInterceptor {

    private static final String TRANSACTION_ID_KEY = "transactionId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        String transactionId = UUID.randomUUID().toString();
        MDC.put(TRANSACTION_ID_KEY, transactionId);
        response.setHeader("X-Transaction-Id", transactionId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        MDC.remove(TRANSACTION_ID_KEY);
    }
}
