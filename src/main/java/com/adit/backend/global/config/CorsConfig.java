package com.adit.backend.global.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

	public static final String SERVER_URL = "http://ec2-3-39-228-156.ap-northeast-2.compute.amazonaws.com:8080";
	public static final String FRONT_URL = "http://localhost:3000";

	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

		CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(true);
		config.addAllowedOrigin(SERVER_URL);
		config.addAllowedOrigin(FRONT_URL);
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		config.setExposedHeaders(List.of("accessToken"));
		config.addExposedHeader("Set-Cookie");
		config.setMaxAge(3600L);

		source.registerCorsConfiguration("/api/**", config);
		return new CorsFilter(source);
	}
}

