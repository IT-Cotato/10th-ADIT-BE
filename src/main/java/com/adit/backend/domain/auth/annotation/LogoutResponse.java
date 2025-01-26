package com.adit.backend.domain.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.adit.backend.global.annotation.ForbiddenResponse;
import com.adit.backend.global.annotation.UnauthorizedResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

// 로그아웃 성공 응답
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "204", description = "로그아웃 성공")
@Operation(summary = "사용자 로그아웃", description = "사용자의 JWT 토큰을 제거하고 로그아웃합니다.")
@ForbiddenResponse
@UnauthorizedResponse
public @interface LogoutResponse {}
