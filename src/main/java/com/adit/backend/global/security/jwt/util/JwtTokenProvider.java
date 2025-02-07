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
import com.adit.backend.global.security.jwt.enums.TokenStatus;
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
	private final UserRepository userRepository;

	private static final String BEARER = "Bearer ";
	private static final String KEY_ROLE = "role";
	private static SecretKey secretKey;

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

	@PostConstruct
	private void setSecretKey() {
		byte[] keyBytes = Base64.getDecoder().decode(key);
		secretKey = Keys.hmacShaKeyFor(keyBytes);
		log.debug("[Token] Secret Key 초기화 완료");
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
		log.debug("[Token] 토큰 생성 완료 - userId: {}", userId);
		return Token.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

	private static Claims parseClaims(String token) {
		log.debug("[Token] 토큰 파싱 시작: {}", token);
		if (!StringUtils.hasText(token)) {
			throw new TokenException(INVALID_TOKEN);
		}
		try {
			return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
		} catch (ExpiredJwtException e) {
			log.warn("[Token] 만료된 토큰: {}", token);
			return e.getClaims();
		} catch (MalformedJwtException e) {
			log.error("[Token] 잘못된 형식의 토큰: {}", token);
			throw new TokenException(INVALID_TOKEN);
		} catch (SecurityException e) {
			log.error("[Token] 유효하지 않은 서명: {}", token);
			throw new TokenException(INVALID_JWT_SIGNATURE);
		}
	}

	public TokenStatus validateAccessToken(String accessToken) {
		if (!StringUtils.hasText(accessToken)) {
			log.warn("[Token] 액세스 토큰 없음");
			return TokenStatus.NOT_FOUND;
		}
		try {
			Claims claims = parseClaims(accessToken);
			if (claims.getExpiration().before(new Date())) {
				log.warn("[Token] 만료된 액세스 토큰");
				return TokenStatus.EXPIRED;
			}
			return TokenStatus.VALID;
		} catch (SecurityException | MalformedJwtException e) {
			log.error("[Token] 유효하지 않은 JWT 토큰", e);
			return TokenStatus.INVALID;
		} catch (UnsupportedJwtException e) {
			log.error("[Token] 지원되지 않는 JWT 토큰", e);
			return TokenStatus.INVALID;
		}
	}

	// refresh token 검증 시에도 예외 대신 명확한 예외 처리를 진행
	public boolean isRefreshTokenValid(String refreshToken) {
		try {
			Claims claims = parseClaims(refreshToken);
			if (!claims.getExpiration().after(new Date())) {
				log.warn("[Token] 리프레시 토큰 만료");
				throw new TokenException(REFRESH_TOKEN_EXPIRED);
			}
			return claims.getExpiration().after(new Date());
		} catch (ExpiredJwtException e) {
			log.warn("[Token] 리프레시 토큰 만료", e);
			throw new TokenException(REFRESH_TOKEN_EXPIRED);
		} catch (Exception e) {
			log.error("[Token] 리프레시 토큰 검증 실패", e);
			throw new TokenException(INVALID_TOKEN);
		}
	}

	public Authentication getAuthentication(String token) {
		try {
			Claims claims = parseClaims(token);
			return createAuthentication(claims, token);
		} catch (Exception e) {
			log.error("[Authentication] 인증 객체 생성 실패", e);
			throw new TokenException(ACCESS_TOKEN_EXPIRED);
		}
	}

	public Authentication getAuthenticationFromRefreshToken(String refreshToken) {
		Claims claims = parseClaims(refreshToken);
		return createAuthentication(claims, refreshToken);
	}

	private Authentication createAuthentication(Claims claims, String token) {
		List<SimpleGrantedAuthority> authorities = getAuthorities(claims);
		User user = userRepository.findById(Long.valueOf(claims.getSubject()))
			.orElseThrow();
		UserDetails principal = principalDetailsService.loadUserByUsername(user.getEmail());
		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}

	private List<SimpleGrantedAuthority> getAuthorities(Claims claims) {
		return Collections.singletonList(new SimpleGrantedAuthority(claims.get(KEY_ROLE).toString()));
	}

	public String extractAccessTokenFromHeader(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(accessTokenHeader))
			.filter(token -> token.startsWith(BEARER))
			.map(token -> token.replace(BEARER, ""))
			.orElseThrow(() -> {
				log.warn("[Token] 헤더에서 액세스 토큰을 찾을 수 없음");
				return new TokenException(TOKEN_NOT_FOUND);
			});
	}

	public Optional<String> extractRefreshTokenFromCookie(HttpServletRequest request) {
		Optional<String> token = Optional.ofNullable(request.getCookies())
			.flatMap(cookies -> Arrays.stream(cookies)
				.filter(cookie -> cookie.getName().equals(refreshCookieName))
				.map(Cookie::getValue)
				.findFirst());
		if (token.isEmpty()) {
			log.warn("[Token] 쿠키에서 리프레시 토큰을 찾을 수 없음");
		}
		return token;
	}

	public Long getExpiration(String token) {
		Claims claims = parseClaims(token);
		return claims.getExpiration().getTime() - new Date().getTime();
	}

	public PrincipalDetails getUserDetails(Authentication authentication) {
		return (PrincipalDetails)authentication.getPrincipal();
	}
}
