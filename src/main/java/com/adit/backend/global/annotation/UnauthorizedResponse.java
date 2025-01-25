package com.adit.backend.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

// 인증 실패 응답 어노테이션
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "401", description = "인증 실패",
	content = @Content(mediaType = "application/json",
		examples = @ExampleObject(
			value = """
				{
				  "success": false,
				  "data": null,
				  "error": {
				    "timestamp": "2025-00-00T12:00:00.000+00:00",
				    "status": 401,
				    "error": "Unauthorized",
				    "message": "올바르지 않은 토큰입니다.",
				    "path": "/api/example/path"
				  }
				}
				"""
		),
		schema = @Schema(implementation = ApiResponse.class)))
public @interface UnauthorizedResponse {
}