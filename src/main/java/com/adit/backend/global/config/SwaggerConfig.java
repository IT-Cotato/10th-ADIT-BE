package com.adit.backend.global.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {
	@Bean
	public OpenAPI openAPI() {
		// 액세스 토큰 인증 방식 정의
		SecurityScheme accessTokenAuth = new SecurityScheme()
			.type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT")
			.in(SecurityScheme.In.HEADER)
			.name("Authorization");

		// 리프레시 토큰 인증 방식 정의
		SecurityScheme refreshTokenAuth = new SecurityScheme()
			.type(SecurityScheme.Type.APIKEY)
			.in(SecurityScheme.In.COOKIE)
			.name("refreshToken");

		SecurityRequirement securityRequirement = new SecurityRequirement()
			.addList("accessTokenAuth")
			.addList("refreshTokenAuth");

		return new OpenAPI()
			.components(new Components()
				.addSecuritySchemes("accessTokenAuth", accessTokenAuth)
				.addSecuritySchemes("refreshTokenAuth", refreshTokenAuth))
			.security(Arrays.asList(securityRequirement))
			.info(new Info()
				.title("오딧(ODIT) api 명세서")
				.description("ODIT api 명세서입니다.")
				.version("1.0.0"));
	}
}