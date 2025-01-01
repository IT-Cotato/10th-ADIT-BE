package com.adit.backend.domain.ai.enums;

public enum ContentType {
	RESTAURANT("식당"),
	EXHIBITION("전시"),
	PERFORMANCE("공연");

	private final String description;

	ContentType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
