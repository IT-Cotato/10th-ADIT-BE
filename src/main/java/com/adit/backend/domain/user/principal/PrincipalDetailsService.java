package com.adit.backend.domain.user.principal;

import static com.adit.backend.global.error.GlobalErrorCode.*;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.repository.UserRepository;
import com.adit.backend.global.error.exception.BusinessException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional(readOnly = true)
public class PrincipalDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public PrincipalDetails loadUserByUsername(String socialId) throws UsernameNotFoundException {
		User user = userRepository.findBySocialId(socialId)
			.orElseThrow(() -> new BusinessException(USER_NOT_FOUND));

		return new PrincipalDetails(
			user,
			Collections.emptyMap(),
			"id"
		);
	}

	public PrincipalDetails createPrincipalDetails(User user, Map<String, Object> attributes, String attributeKey) {
		return new PrincipalDetails(
			user,
			attributes,
			attributeKey
		);
	}

	public User getUser(String socialId) {
		return userRepository.findBySocialId(socialId)
			.orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
	}
}
