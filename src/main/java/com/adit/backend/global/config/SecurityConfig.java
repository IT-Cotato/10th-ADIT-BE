package com.adit.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

import com.adit.backend.global.security.jwt.filter.JwtAuthenticationFilter;
import com.adit.backend.global.security.jwt.filter.TokenExceptionFilter;
import com.adit.backend.global.security.jwt.handler.CustomAccessDeniedHandler;
import com.adit.backend.global.security.jwt.handler.CustomAuthenticationEntryPoint;
import com.adit.backend.global.security.oauth.handler.OAuth2SuccessHandler;
import com.adit.backend.global.security.oauth.service.CustomOAuth2UserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * The type Security config.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class SecurityConfig {

	private final CorsFilter corsFilter;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring()
			.requestMatchers(
				"/error",
				"/favicon.ico",
				"/v3/api-docs/**",
				"/swagger-ui/**",
				"/swagger-resources/**");
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)
			.headers(c -> c.frameOptions(
				HeadersConfigurer.FrameOptionsConfig::disable).disable())
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.addFilter(corsFilter)
			.authorizeHttpRequests(request -> request
				.requestMatchers(
					"/",
					"/login/**",
					"/api/ai/**",
					"/api/user/**",
					"/api/auth/**",
					"/swagger-ui/**",
					"/swagger-ui.html",
					"/v3/api-docs/**",
					"/swagger-resources/**",
					"/webjars/**",
					"/api/scraper/**",
					"/oauth2/**"  // OAuth2 엔드포인트 추가
				).permitAll()
				.anyRequest().authenticated()
			)
			// OAuth2 로그인 설정 추가
			.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo
					.userService(customOAuth2UserService)
				)
				.successHandler(oAuth2SuccessHandler)
			)
			.addFilterBefore(new TokenExceptionFilter(), OAuth2AuthorizationRequestRedirectFilter.class)
			.addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.exceptionHandling(exceptions -> exceptions
				.authenticationEntryPoint(new CustomAuthenticationEntryPoint())
				.accessDeniedHandler(new CustomAccessDeniedHandler()));

		return http.build();
	}
}
