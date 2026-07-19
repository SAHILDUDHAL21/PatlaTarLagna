package com.patlatarlagna;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main Entry Point for PatlaTarLagna Matrimonial matchmaking platform.
 */
@SpringBootApplication
@EnableJpaAuditing
public class PatlatarlagnaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatlatarlagnaApplication.class, args);
    }
}
