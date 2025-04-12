package org.example.swim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@SpringBootApplication
public class SwimApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwimApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder()
            .connectTimeout(Duration.ofMillis(1_000L))
            .readTimeout(Duration.ofMillis(500L))
            .sslBundle(null)
            .build();
    }
}
