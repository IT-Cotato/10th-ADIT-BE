package com.adit.backend.global.security.jwt.util;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.adit.backend.domain.auth.entity.Token;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.principal.PrincipalDetails;
import com.adit.backend.domain.user.principal.PrincipalDetailsService;
import com.adit.backend.domain.user.repository.UserRepository;
import com.adit.backend.global.error.exception.TokenException;

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
		validateKeyStrength(keyBytes);
	}

	private void validateKeyStrength(byte[] keyBytes) {
		if (keyBytes.length < 32) { // 256 bits
			throw new IllegalArgumentException("Secret key must be at least 256 bits long");
		}
	}

	private String generateToken(Authentication authentication, long expireTime) {
		Date now = new Date();
		Date expiredDate = new Date(now.getTime() + expireTime);

		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining());

		String subject = String.valueOf(getUserDetails(authentication).getUser().getId());

		return Jwts.builder()
			.subject(subject)
			.claim(KEY_ROLE, authorities)
			.issuedAt(now)
			.expiration(expiredDate)
			.signWith(secretKey, Jwts.SIG.HS256)
			.compact();
	}

	public String generateAccessToken(Authentication authentication) {
		return generateToken(authentication, accessTokenExpirationAt);
	}

	public String generateRefreshToken(Authentication authentication) {
		return generateToken(authentication, refreshTokenExpirationAt);
	}

	public Token createToken(Authentication authentication) {
		String accessToken = generateAccessToken(authentication);
		String refreshToken = generateRefreshToken(authentication);
		return Token.builder()
			.accessToken(accessToken)
			.refreshToken(refreshToken)
			.build();
	}

/*	public Token checkRefreshTokenAndReIssueAccessToken(Authentication authentication, String refreshToken) {
		return jwtTokenService.findByAccessTokenOrThrow(refreshToken)
			.filter(token -> isRefreshTokenValid(refreshToken))
			.map(token -> {
				String newRefreshToken = generateRefreshToken(authentication);
				String newAccessToken = generateAccessToken(authentication);
				jwtTokenService.updateTokens(newAccessToken, newRefreshToken, token);
				return token.builder()
					.accessToken(newAccessToken)
					.refreshToken(refreshToken)
					.build();
			})
			.orElseThrow(() -> new TokenException(REFRESH_TOKEN_EXPIRED));
	}*/

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

	public boolean isAccessTokenValid(String accessToken) {
		try {
			if (!StringUtils.hasText(accessToken)) {
				throw new TokenException(TOKEN_NOT_FOUND);
			}
			Claims claims = parseClaims(accessToken);
			return !claims.getExpiration().before(new Date());
		} catch (SecurityException | MalformedJwtException e) {
			throw new TokenException(INVALID_JWT_SIGNATURE);
		} catch (ExpiredJwtException e) {
			throw new TokenException(ACCESS_TOKEN_EXPIRED);
		} catch (UnsupportedJwtException e) {
			throw new TokenException(TOKEN_UNSURPPORTED);
		}
	}

	public Optional<String> extractAccessTokenFromHeader(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(accessTokenHeader))
			.filter(token -> token.startsWith(BEARER))
			.map(token -> token.replace(BEARER, ""));
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
		PrincipalDetails principal = principalDetailsService.loadUserByUsername(user.getEmail());
		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}

	public Authentication getAuthentication(String token) {
		try {
			Claims claims = parseClaims(token);
			Authentication auth = createAuthentication(claims, token);
			log.info("[Authentication] 사용자 인증 생성: {}:", claims.getSubject());
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