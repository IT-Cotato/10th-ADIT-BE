package com.adit.backend.domain.image.enums;

public enum Directory {
	USER("USER/"),
	PLACE("PLACE"),
	EVENT("EVENT"),
	TEST("TEST");

	private final String path;

	Directory(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
}
