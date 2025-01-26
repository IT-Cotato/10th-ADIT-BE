package com.adit.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
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
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.CorsFilter;

import com.adit.backend.global.security.jwt.filter.JwtAuthorizationFilter;
import com.adit.backend.global.security.jwt.filter.TokenExceptionFilter;
import com.adit.backend.global.security.jwt.handler.CustomAccessDeniedHandler;
import com.adit.backend.global.security.jwt.handler.CustomAuthenticationEntryPoint;
import com.adit.backend.global.security.oauth.handler.OAuth2FailureHandler;
import com.adit.backend.global.security.oauth.handler.OAuth2SuccessHandler;
import com.adit.backend.global.security.oauth.repository.HttpCookieOAuth2AuthorizationRequestRepository;
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

	private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

	private static final String[] WHITE_LIST = {
		"/",
		"/login/**",
		"/api/ai/**",
		"/api/user/**",
		"/api/auth/**",
		"/webjars/**",
		"/api/scraper/**",
		"/oauth2/**",
	};
	private final CorsFilter corsFilter;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;
	private final OAuth2FailureHandler oAuth2FailureHandler;
	private final JwtAuthorizationFilter jwtAuthorizationFilter;
	@Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
	String frontRedirectUrl;

	/**
	 * 보안 검사를 무시할 웹 리소스 설정
	 * 주로 정적 리소스나 API 문서에 대한 접근을 허용
	 */
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

	/**
	 * AuthenticationManager 빈 설정
	 * Spring Security의 인증 관리자를 생성
	 */
	@Bean
	public AuthenticationManager authenticationManager(HttpSecurity httpSecurity) throws Exception {
		return httpSecurity.getSharedObject(AuthenticationManagerBuilder.class)
			.build();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder sharedObject = http.getSharedObject(AuthenticationManagerBuilder.class);
		AuthenticationManager authenticationManager = sharedObject.build();
		http.authenticationManager(authenticationManager);

		http
			// CSRF 보호 비활성화
			.csrf(AbstractHttpConfigurer::disable)
			// 기본 HTTP 인증 비활성화
			.httpBasic(AbstractHttpConfigurer::disable)
			// 폼 로그인 비활성화
			.formLogin(AbstractHttpConfigurer::disable)
			// 로그아웃 기능 비활성화
			.logout(AbstractHttpConfigurer::disable)
			// X-Frame-Options 헤더 설정 비활성화
			.headers(c -> c.frameOptions(
				HeadersConfigurer.FrameOptionsConfig::disable).disable())

			// 세션 생성 정책을 STATELESS로 설정 (JWT 사용을 위함)
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			// CORS 필터 추가
			.addFilter(corsFilter)

			// URL 기반 접근 권한 설정
			.authorizeHttpRequests(request -> request
				.requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
				.requestMatchers(WHITE_LIST).permitAll()
				.anyRequest().authenticated()
			)

			// OAuth2 로그인 설정
			.oauth2Login(AbstractHttpConfigurer::disable
				// .userInfoEndpoint(userInfo -> userInfo
				// 	.userService(customOAuth2UserService)
				// )
				// .authorizationEndpoint(authEndpoint -> authEndpoint
				// 	.baseUri("/oauth2/authorization")
				// 	.authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository))
				// .redirectionEndpoint(redirect -> redirect
				// 	.baseUri(frontRedirectUrl))
				// .successHandler(oAuth2SuccessHandler)
				// .failureHandler(oAuth2FailureHandler)
			)

			// JWT 관련 필터 추가
			.addFilterBefore(new TokenExceptionFilter(), OAuth2AuthorizationRequestRedirectFilter.class)
			.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)

			// 인증/인가 예외 처리 핸들러 설정
			.exceptionHandling(exceptions -> exceptions
				.authenticationEntryPoint(new CustomAuthenticationEntryPoint())
				.accessDeniedHandler(new CustomAccessDeniedHandler()));

		return http.build();
	}
}