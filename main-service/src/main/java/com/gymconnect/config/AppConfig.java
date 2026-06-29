package com.gymconnect.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("com.gymconnect")
@PropertySource("classpath:application.properties")
public class AppConfig {
}
