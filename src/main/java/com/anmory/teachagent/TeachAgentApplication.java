package com.anmory.teachagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@EnableAsync
@CrossOrigin
public class TeachAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeachAgentApplication.class, args);
    }

}
