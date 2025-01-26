package com.adit.backend.domain.user.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

// 중복 닉네임 응답용 어노테이션
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "400", description = "중복된 닉네임",
	content = @Content(mediaType = "application/json",
		examples = @ExampleObject(
			value = """
				{
				  "success": false,
				  "data": null,
				  "error": {
				    "status": "INTERNAL_SERVER_ERROR",
				    "code": "G999",
				    "resultMsg": "Internal Server Error Exception",
				    "errors": null,
				    "reason": "이미 존재하는 닉네임입니다."
				  }
				}
				"""
		),
		schema = @Schema(implementation = ApiResponse.class)))
public @interface DuplicateNicknameResponse {
}
