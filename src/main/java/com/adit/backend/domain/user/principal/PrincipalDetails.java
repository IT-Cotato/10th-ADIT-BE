package com.adit.backend.domain.user.principal;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.adit.backend.domain.user.entity.User;

public record PrincipalDetails(
	User user,
	Map<String, Object> attributes,
	String attributeKey) implements OAuth2User, UserDetails {

	@Override
	public String getName() {
		return attributes.get(attributeKey).toString();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(
			new SimpleGrantedAuthority(user.getRole().getKey()));
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return user.getSocialId();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public User getUser() {
		return user();
	}

	public Long getUserId(){return user.getId();}

}