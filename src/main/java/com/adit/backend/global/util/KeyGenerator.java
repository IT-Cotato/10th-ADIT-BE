package com.adit.backend.global.util;

import java.util.UUID;

public final class KeyGenerator {

	public static String generateKey() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
