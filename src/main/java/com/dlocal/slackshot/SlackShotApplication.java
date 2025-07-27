package com.dlocal.slackshot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SlackShotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SlackShotApplication.class, args);
    }
} 