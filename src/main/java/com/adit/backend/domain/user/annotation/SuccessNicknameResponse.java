package com.adit.backend.domain.user.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

// 성공 응답용 어노테이션
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "200", description = "사용 가능한 닉네임",
	content = @Content(mediaType = "application/json",
		examples = @ExampleObject(
			value = """
                {
                  "success": true,
                  "data": "사용 가능한 닉네임입니다.",
                  "error": null
                }
                """
		),
		schema = @Schema(implementation = ApiResponse.class)))
public @interface SuccessNicknameResponse {}