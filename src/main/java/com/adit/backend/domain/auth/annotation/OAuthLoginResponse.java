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
@ApiResponse(responseCode = "200", description = "로그인 성공",
	content = @Content(mediaType = "application/json",
		examples = @ExampleObject(
			value = """
				{
				  "success": true,
				  "data": {
				    "Role": "Guest"
				  },
				  "error": null
				}
				"""
		),
		schema = @Schema(implementation = ApiResponse.class)))
@Operation(summary = "사용자 로그인", description = "카카오에서 받은 인가 코드를 기반으로 토큰과 Role을 반환합니다.")
@ForbiddenResponse
@UnauthorizedResponse
public @interface OAuthLoginResponse {
}