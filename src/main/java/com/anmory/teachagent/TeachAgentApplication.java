package com.anmory.teachagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * Main application class for TeachAgent.
 * 
 * @SpringBootApplication - Enables Spring Boot auto-configuration
 * @EnableAsync - Enables asynchronous method execution for question generation
 * @CrossOrigin - Enables cross-origin requests for the web interface
 */
@SpringBootApplication
@EnableAsync
@CrossOrigin
public class TeachAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeachAgentApplication.class, args);
    }

}
