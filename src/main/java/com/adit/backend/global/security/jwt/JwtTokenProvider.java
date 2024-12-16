package com.adit.backend.global.security.jwt;

import static com.adit.backend.global.error.GlobalErrorCode.*;

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
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.adit.backend.domain.auth.entity.Token;
import com.adit.backend.domain.auth.service.TokenService;
import com.adit.backend.global.error.exception.TokenException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Component
@Slf4j
public class JwtTokenProvider {
	@Value("${jwt.key}")
	private String key;

	private static final String BEARER = "Bearer ";
	private static final String KEY_ROLE = "role";
	private static SecretKey secretKey;
	private final TokenService tokenService;
	@Value("${jwt.access.expiration}")
	private Long accessTokenExpirationPeriod;
	@Value("${jwt.refresh.expiration}")
	private Long refreshTokenExpirationPeriod;
	@Value("${jwt.access.header}")
	private String accessHeader;
	@Value("${jwt.refresh.header}")
	private String refreshHeader;

	@PostConstruct
	private void setSecretKey() {
		secretKey = Keys.hmacShaKeyFor(key.getBytes());
	}

	private static Claims parseClaims(String token) {
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

	public String generateAccessToken(Authentication authentication) {
		return generateToken(authentication, accessTokenExpirationPeriod);
	}

	private String generateToken(Authentication authentication, long expireTime) {
		Date now = new Date();
		Date expiredDate = new Date(now.getTime() + expireTime);

		String authorities = authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.collect(Collectors.joining());

		return Jwts.builder()
			.subject(authentication.getName())
			.claim(KEY_ROLE, authorities)
			.issuedAt(now)
			.expiration(expiredDate)
			.signWith(secretKey, Jwts.SIG.HS512)
			.compact();
	}

	// 1. refresh token 발급
	public String generateRefreshToken(Authentication authentication, String accessToken) {
		String refreshToken = generateToken(authentication, refreshTokenExpirationPeriod);
		tokenService.saveOrUpdate(authentication.getName(), refreshToken, accessToken);
		return refreshToken;
	}

	private Authentication createAuthentication(Claims claims, String token) {
		List<SimpleGrantedAuthority> authorities = getAuthorities(claims);
		User principal = new User(claims.getSubject(), "", authorities);
		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}

	private List<SimpleGrantedAuthority> getAuthorities(Claims claims) {
		return Collections.singletonList(new SimpleGrantedAuthority(
			claims.get(KEY_ROLE).toString()));
	}

	public Authentication getAuthentication(String token) {
		try {
			Claims parsedClaims = parseClaims(token);
			return createAuthentication(parsedClaims, token);
		} catch (ExpiredJwtException e) {
			throw new TokenException(ACCESS_TOKEN_EXPIRED);
		}
	}

	// 3. accessToken 재발급
	public String checkRefreshTokenAndReIssueAccessToken(Authentication authentication, String refreshToken) {
		Token token = tokenService.findByAccessTokenOrThrow(refreshToken);
		if (validateRefreshToken(refreshToken)) {
			String reIssuedRefreshToken = reissueRefreshToken(authentication, token);
			String reissueAccessToken = generateAccessToken(getAuthentication(reIssuedRefreshToken));
			tokenService.updateAccessToken(reissueAccessToken, token);
			return reissueAccessToken;
		} else {
			throw new TokenException(REFRESH_TOKEN_EXPIRED);
		}

	}

	public boolean validateRefreshToken(String refreshToken) {
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

	public String reissueRefreshToken(Authentication authentication, Token token) {
		String reIssuedRefreshToken = generateRefreshToken(authentication, token.getAccessToken());
		tokenService.updateRefreshToken(reIssuedRefreshToken, token);
		return reIssuedRefreshToken;
	}

	public boolean validateAccessToken(String accessToken) {
		try {
			if (!StringUtils.hasText(accessToken)) {
				throw new TokenException(TOKEN_NOT_FOUND);
			}
			Claims claims = parseClaims(accessToken);
			if (claims.getExpiration().before(new Date())) {
				throw new TokenException(ACCESS_TOKEN_EXPIRED);
			}
			return true;
		} catch (SecurityException | MalformedJwtException e) {
			throw new TokenException(INVALID_JWT_SIGNATURE);
		} catch (ExpiredJwtException e) {
			throw new TokenException(ACCESS_TOKEN_EXPIRED);
		} catch (UnsupportedJwtException e) {
			throw new TokenException(TOKEN_UNSURPPORTED);
		} catch (IllegalArgumentException e) {
			throw new TokenException(INVALID_TOKEN);
		}
	}

	public Optional<String> getSocialId(String token) {
		Claims claims = parseClaims(token);
		return Optional.ofNullable(claims.getSubject());
	}

	public Optional<String> extractRefreshToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(refreshHeader))
			.filter(refreshToken -> refreshToken.startsWith(BEARER))
			.map(refreshToken -> refreshToken.replace(BEARER, ""));
	}

	public Optional<String> extractAccessToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(accessHeader))
			.filter(refreshToken -> refreshToken.startsWith(BEARER))
			.map(refreshToken -> refreshToken.replace(BEARER, ""));
	}

}