package com.oneonone.pointservice;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        servers = {
                @Server(url = "/", description = "Point Service (via Gateway)")
        }
)
@SpringBootApplication(scanBasePackages = {
        "com.oneonone.pointservice",
        "com.oneonone.common"
})
public class PointServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PointServiceApplication.class, args);
    }

}
