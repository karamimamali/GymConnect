package com.gymconnect.workload.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Transaction-level logging: records which endpoint was invoked, the incoming
 * request, and the outgoing response status for every REST call.
 */
@Component
public class RestLoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RestLoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        logger.info("REST call: {} {} from {}", request.getMethod(),
                request.getRequestURI(), request.getRemoteAddr());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        int status = response.getStatus();
        if (ex != null) {
            logger.error("REST response: {} {} -> {} (error: {})", request.getMethod(),
                    request.getRequestURI(), status, ex.getMessage());
        } else {
            logger.info("REST response: {} {} -> {}", request.getMethod(),
                    request.getRequestURI(), status);
        }
    }
}
