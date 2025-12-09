package com.oneonone.bettingservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@OpenAPIDefinition(
        servers = {
                @Server(url = "/", description = "Betting Service (via Gateway)")
        }
)
@SpringBootApplication(scanBasePackages = {
        "com.oneonone.bettingservice",
        "com.oneonone.common"
})
@EnableJpaAuditing
public class BettingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BettingServiceApplication.class, args);
    }

}
