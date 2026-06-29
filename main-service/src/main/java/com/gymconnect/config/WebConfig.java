package com.gymconnect.config;

import com.gymconnect.interceptor.RestLoggingInterceptor;
import com.gymconnect.interceptor.TransactionIdInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TransactionIdInterceptor transactionIdInterceptor;
    private final RestLoggingInterceptor restLoggingInterceptor;

    public WebConfig(TransactionIdInterceptor transactionIdInterceptor,
                     RestLoggingInterceptor restLoggingInterceptor) {
        this.transactionIdInterceptor = transactionIdInterceptor;
        this.restLoggingInterceptor = restLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(transactionIdInterceptor);
        registry.addInterceptor(restLoggingInterceptor);
    }
}
