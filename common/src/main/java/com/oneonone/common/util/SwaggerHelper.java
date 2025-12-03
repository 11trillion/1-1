package com.oneonone.common.util;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;

public class SwaggerHelper {

    /**
     * 400 Bad Request
     */
    public static ApiResponse createBadRequestResponse() {
        return new ApiResponse()
                .description("잘못된 요청")
                .content(errorResponseContent());
    }

    /**
     * 500 Internal Server Error
     */
    public static ApiResponse createInternalServerErrorResponse() {
        return new ApiResponse()
                .description("서버 내부 오류")
                .content(errorResponseContent());
    }

    /**
     * 공통 Error ApiResponse Schema
     */
    private static Content errorResponseContent() {
        Schema<?> schema = new Schema<>()
                .type("object")
                .addProperty("success", new Schema<>().type("boolean").example(false))
                .addProperty("message", new Schema<>().type("string").example("에러 메시지"))
                .addProperty("code", new Schema<>().type("string").example("ERROR_CODE"));

        return new Content().addMediaType(
                "application/json",
                new MediaType().schema(schema)
        );
    }
}
