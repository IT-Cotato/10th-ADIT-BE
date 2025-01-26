package com.adit.backend.global.security.jwt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.global.security.jwt.entity.BlackList;
import com.adit.backend.global.security.jwt.repository.BlackListRepository;
import com.adit.backend.global.security.jwt.util.JwtTokenProvider;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtTokenService {

	private final BlackListRepository blackListRepository;
	private final JwtTokenProvider tokenProvider;

	@Transactional
	public void setBlackList(String token) {
		BlackList blackList = BlackList.builder()
			.id(token)
			.ttl((tokenProvider.getExpiration(token)))
			.build();
		blackListRepository.save(blackList);
		log.info("[Token] 토큰 블랙리스트 추가 완료 : {}", token);
	}
}