package com.adit.backend.domain.user.annotation;

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

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(summary = "닉네임 변경", description = "사용자 로그인 정보를 이용하여 요청된 값으로 닉네임을 변경합니다.")
@ApiResponse(responseCode = "200", description = "닉네임 변경 성공",
	content = @Content(mediaType = "application/json",
		examples = @ExampleObject(
			name = "SuccessResponse",
			value = """
            {
              "success": true,
              "data": {
                "nickname": "새로운닉네임"
              },
              "error": null
            }
            """
		),
		schema = @Schema(implementation = ApiResponse.class)))
@UnauthorizedResponse
@ForbiddenResponse
public @interface ChangeNicknameApiSpec {}