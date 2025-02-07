package com.adit.backend.global.security.jwt.enums;

public enum TokenStatus {
	VALID,      // 토큰이 유효함
	EXPIRED,    // 토큰이 만료됨
	INVALID,    // 토큰 형식이나 서명이 잘못됨
	NOT_FOUND   // 토큰이 존재하지 않음
}