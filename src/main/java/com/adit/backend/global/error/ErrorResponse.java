package com.adit.backend.global.error;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {
	private LocalDateTime timestamp;    // 에러 발생 시간
	private int status;                 // 에러 상태 코드
	private String error;               // HTTP 상태 메시지
	private String message;             // 에러 메시지
	private String path;                // 에러 발생 경로
	private List<FieldError> errors;    // 상세 에러 메시지 (필요한 경우)

	@Builder
	protected ErrorResponse(final GlobalErrorCode code, final String path) {
		this.timestamp = LocalDateTime.now();
		this.status = code.getHttpStatus();
		this.error = HttpStatus.valueOf(code.getHttpStatus()).getReasonPhrase();
		this.message = code.getMessage();
		this.path = path;
		this.errors = new ArrayList<>();
	}

	@Builder
	protected ErrorResponse(final GlobalErrorCode code, final String path, final List<FieldError> errors) {
		this.timestamp = LocalDateTime.now();
		this.status = code.getHttpStatus();
		this.error = HttpStatus.valueOf(code.getHttpStatus()).getReasonPhrase();
		this.message = code.getMessage();
		this.path = path;
		this.errors = errors;
	}

	public static ErrorResponse of(final GlobalErrorCode code, final String path) {
		return new ErrorResponse(code, path);
	}

	public static ErrorResponse of(final GlobalErrorCode code, final String path, final BindingResult bindingResult) {
		return new ErrorResponse(code, path, FieldError.of(bindingResult));
	}

	@Getter
	public static class FieldError {
		private final String field;
		private final String value;
		private final String reason;

		@Builder
		FieldError(String field, String value, String reason) {
			this.field = field;
			this.value = value;
			this.reason = reason;
		}

		private static List<FieldError> of(final BindingResult bindingResult) {
			final List<org.springframework.validation.FieldError> fieldErrors = bindingResult.getFieldErrors();
			return fieldErrors.stream()
				.map(error -> new FieldError(
					error.getField(),
					error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
					error.getDefaultMessage()))
				.toList();
		}
	}
}
