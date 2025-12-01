package com.oneonone.bettingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@SpringBootApplication
@EnableJpaAuditing
public class BettingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BettingServiceApplication.class, args);
    }

}
