package com.adit.backend.global.security.jwt.util;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.enums.Role;
import com.adit.backend.domain.user.principal.PrincipalDetails;
import com.adit.backend.domain.user.principal.PrincipalDetailsService;
import com.adit.backend.domain.user.repository.UserRepository;
import com.adit.backend.global.security.jwt.entity.Token;
import com.adit.backend.global.security.jwt.exception.TokenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtTokenProvider {

	private final PrincipalDetailsService principalDetailsService;

	private static final String BEARER = "Bearer ";
	private static final String KEY_ROLE = "role";

	private static SecretKey secretKey;
	private final UserRepository userRepository;
	@Value("${token.key}")
	private String key;
	@Value("${token.access.expiration}")
	private Long accessTokenExpirationAt;
	@Value("${token.refresh.expiration}")
	private Long refreshTokenExpirationAt;
	@Value("${token.access.header}")
	private String accessTokenHeader;
	@Value("${token.refresh.cookie.name}")
	private String refreshCookieName;

	private static Claims parseClaims(String token) {
		log.info("[Token] 토큰 유효성 검증:, {}", token);
		if (!StringUtils.hasText(token)) {
			throw new TokenException(INVALID_TOKEN);
		}
		try {
			return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		} catch (MalformedJwtException e) {
			throw new TokenException(INVALID_TOKEN);
		} catch (SecurityException e) {
			throw new TokenException(INVALID_JWT_SIGNATURE);
		}
	}

	@PostConstruct
	private void setSecretKey() {
		byte[] keyBytes = Base64.getDecoder().decode(key);
		secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	private String generateToken(Long userId, String role, long expireTime) {
		Date now = new Date(System.currentTimeMillis());
		Date expiredDate = new Date(System.currentTimeMillis() + expireTime);

		return Jwts.builder()
			.subject(String.valueOf(userId))
			.claim(KEY_ROLE, role)
			.issuedAt(now)
			.expiration(expiredDate)
			.signWith(secretKey, Jwts.SIG.HS256)
			.compact();
	}

	public Token createToken(Long userId, Role role) {
		String accessToken = generateToken(userId, role.getKey(), accessTokenExpirationAt);
		String refreshToken = generateToken(userId, role.getKey(), refreshTokenExpirationAt);
		log.info("[Token] 토큰 발급 완료");
		return Token.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

	public boolean isRefreshTokenValid(String refreshToken) {
		try {
			Claims claims = parseClaims(refreshToken);
			return claims.getExpiration().after(new Date());
		} catch (ExpiredJwtException e) {
			throw new TokenException(REFRESH_TOKEN_EXPIRED);  // 리프레시 토큰 만료 시 재로그인 요구
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void isAccessTokenValid(String accessToken) {
		try {
			if (!StringUtils.hasText(accessToken)) {
				throw new TokenException(TOKEN_NOT_FOUND);
			}
			Claims claims = parseClaims(accessToken);
			claims.getExpiration();
		} catch (SecurityException | MalformedJwtException e) {
			throw new TokenException(INVALID_JWT_SIGNATURE);
		} catch (ExpiredJwtException e) {
			throw new TokenException(ACCESS_TOKEN_EXPIRED);
		} catch (UnsupportedJwtException e) {
			throw new TokenException(TOKEN_UNSURPPORTED);
		}
	}

	public String extractAccessTokenFromHeader(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(accessTokenHeader))
			.filter(token -> token.startsWith(BEARER))
			.map(token -> token.replace(BEARER, ""))
			.orElseThrow(() -> new TokenException(NULL_POINT_ERROR));
	}

	public Optional<String> extractRefreshTokenFromCookie(HttpServletRequest request) {
		return Optional.ofNullable(request.getCookies())
			.flatMap(cookies -> Arrays.stream(cookies)
				.filter(cookie -> cookie.getName().equals(refreshCookieName))
				.map(Cookie::getValue)
				.findFirst());
	}

	private Authentication createAuthentication(Claims claims, String token) {
		List<SimpleGrantedAuthority> authorities = getAuthorities(claims);
		User user = userRepository.findById(Long.valueOf(claims.getSubject())).orElseThrow();
		UserDetails principal = principalDetailsService.loadUserByUsername(user.getEmail());
		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}

	public Authentication getAuthentication(String token) {
		try {
			Claims claims = parseClaims(token);
			Authentication auth = createAuthentication(claims, token);
			log.info("[Authentication] 사용자 인증 생성: {}", claims.getSubject());
			return auth;
		} catch (Exception e) {
			log.info("[Authentication] 사용자 인증 생성 실패", e.getCause());
			throw new TokenException(ACCESS_TOKEN_EXPIRED);
		}
	}

	public Authentication getAuthenticationFromRefreshToken(String refreshToken) {
		Claims claims = parseClaims(refreshToken);
		return createAuthentication(claims, refreshToken);
	}

	private List<SimpleGrantedAuthority> getAuthorities(Claims claims) {
		return Collections.singletonList(new SimpleGrantedAuthority(
			claims.get(KEY_ROLE).toString()));
	}

	public Long getExpiration(String token) {
		Claims claims = parseClaims(token);
		return claims.getExpiration().getTime() - new Date().getTime();
	}

	public PrincipalDetails getUserDetails(Authentication authentication) {
		return (PrincipalDetails)authentication.getPrincipal();
	}

}