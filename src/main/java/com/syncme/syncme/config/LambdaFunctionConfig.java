package com.syncme.syncme.config;

import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

@Configuration
public class LambdaFunctionConfig {

    @Bean
    public Function<Message<byte[]>, Message<byte[]>> handleRequest() {
        return message -> message;
    }
}
