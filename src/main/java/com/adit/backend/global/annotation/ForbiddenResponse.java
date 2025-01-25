package com.adit.backend.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

// 접근 권한 없음 응답 어노테이션
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "403", description = "접근 권한 없음",
	content = @Content(mediaType = "application/json",
		examples = @ExampleObject(
			value = """
				{
				  "success": false,
				  "data": null,
				  "error": {
					"timestamp": "2025-00-00T12:00:00.000+00:00",
					"status": 403,
					"error": "Forbidden",
					"message": "접근 권한이 없습니다.",
					"path": "/api/example/path"
				  }
				}
            	"""
		),
		schema = @Schema(implementation = ApiResponse.class)))
public @interface ForbiddenResponse {}