package com.inventory.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for the isolated AI module. Provides a dedicated
 * {@link RestTemplate} with connect/read timeouts so a slow or unresponsive
 * OpenAI endpoint cannot block request threads indefinitely (Requirement 11.6).
 */
@Configuration
public class AIConfig {

    @Value("${app.openai.timeout-seconds:30}")
    private long timeoutSeconds;

    @Bean(name = "openAiRestTemplate")
    public RestTemplate openAiRestTemplate(RestTemplateBuilder builder) {
        Duration timeout = Duration.ofSeconds(timeoutSeconds);
        return builder
                .connectTimeout(timeout)
                .readTimeout(timeout)
                .build();
    }
}
