package com.adit.backend.domain.user.principal;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adit.backend.domain.user.entity.User;
import com.adit.backend.domain.user.service.query.UserQueryService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional(readOnly = true)
public class PrincipalDetailsService implements UserDetailsService {

	private final UserQueryService userQueryService;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userQueryService.findUserByEmail(email);
		log.debug("[User] 사용자를 찾았습니다. {}", user.getEmail());
		return createPrincipalDetails(user, Collections.emptyMap(), "id");
	}

	public PrincipalDetails createPrincipalDetails(User user, Map<String, Object> attributes, String attributeKey) {
		return new PrincipalDetails(
			user,
			attributes,
			attributeKey
		);
	}

}
