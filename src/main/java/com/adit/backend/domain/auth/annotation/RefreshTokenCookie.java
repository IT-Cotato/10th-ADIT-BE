package com.adit.backend.domain.auth.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

// 리프레시 토큰 파라미터 어노테이션
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(
	name = "refreshToken",
	in = ParameterIn.COOKIE,
	description = "리프레시 토큰",
	hidden = true

)
public @interface RefreshTokenCookie {
}