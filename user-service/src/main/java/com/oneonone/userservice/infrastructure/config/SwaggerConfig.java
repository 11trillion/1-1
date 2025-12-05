package com.oneonone.userservice.infrastructure.config;

import com.oneonone.common.util.SwaggerHelper;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenApiCustomizer customGlobalResponses() {
        return openApi -> {
            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(op -> {
                        op.getResponses().addApiResponse(
                                "400",
                                SwaggerHelper.createBadRequestResponse()
                        );
                        op.getResponses().addApiResponse(
                                "500",
                                SwaggerHelper.createInternalServerErrorResponse()
                        );
                    })
            );
        };
    }
}
