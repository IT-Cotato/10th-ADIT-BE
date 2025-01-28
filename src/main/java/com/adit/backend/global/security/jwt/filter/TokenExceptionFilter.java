package com.adit.backend.global.security.jwt.filter;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import com.adit.backend.global.security.jwt.exception.TokenException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TokenExceptionFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request,
		HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (TokenException e) {
			response.sendError(e.getErrorCode().getHttpStatus(), e.getMessage());
		}
	}

}
