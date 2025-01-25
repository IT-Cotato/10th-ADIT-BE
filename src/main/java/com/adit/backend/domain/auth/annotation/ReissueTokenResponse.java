package com.adit.backend.domain.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.adit.backend.global.annotation.ForbiddenResponse;
import com.adit.backend.global.annotation.UnauthorizedResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

// 토큰 재발급 성공 응답
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "200", description = "토큰 재발급 성공",
	content = @Content(mediaType = "application/json",
		examples = @ExampleObject(
			value = """
				{
				  "success": true,
				  "data": {
				    "accessToken": "value"
				  },
				  "error": null
				}
				"""
		),
		schema = @Schema(implementation = ApiResponse.class)))
@Operation(summary = "사용자 토큰 재발급", description = "사용자의 RefreshToken을 이용하여 JWT 토큰을 재 발급합니다.")
@ForbiddenResponse
@UnauthorizedResponse
public @interface ReissueTokenResponse {
}