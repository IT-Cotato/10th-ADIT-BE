package com.adit.global.error;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import com.adit.global.error.exception.GlobalErrorCode;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Global Exception Handler에서 발생한 에러에 대한 응답 처리를 관리
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorResponse {
	private HttpStatus status;                 // 에러 상태 코드
	private String code;        // 에러 구분 코드
	private String resultMsg;           // 에러 메시지
	private List<FieldError> errors;    // 상세 에러 메시지
	private String reason;              // 에러 이유

	/**
	 * ErrorResponse 생성자-1
	 *
	 * @param code GlobalErrorCode
	 */
	@Builder
	protected ErrorResponse(final GlobalErrorCode code) {
		this.resultMsg = code.getMessage();
		this.status = code.getHttpStatus();
		this.code = code.getCode();
		this.errors = new ArrayList<>();
	}

	/**
	 * ErrorResponse 생성자-2
	 *
	 * @param code   GlobalErrorCode
	 * @param reason String
	 */
	@Builder
	protected ErrorResponse(final GlobalErrorCode code, final String reason) {
		this.resultMsg = code.getMessage();
		this.status = code.getHttpStatus();
		this.code = code.getCode();
		this.reason = reason;
	}

	/**
	 * ErrorResponse 생성자-3
	 *
	 * @param code   GlobalErrorCode
	 * @param errors List<FieldError>
	 */
	@Builder
	protected ErrorResponse(final GlobalErrorCode code, final List<FieldError> errors) {
		this.resultMsg = code.getMessage();
		this.status = code.getHttpStatus();
		this.errors = errors;
		this.code = code.getCode();
	}

	/**
	 * Global Exception 전송 타입-1
	 *
	 * @param code          GlobalErrorCode
	 * @param bindingResult BindingResult
	 * @return ErrorResponse
	 */
	public static ErrorResponse of(final GlobalErrorCode code, final BindingResult bindingResult) {
		return new ErrorResponse(code, FieldError.of(bindingResult));
	}

	/**
	 * Global Exception 전송 타입-2
	 *
	 * @param code GlobalErrorCode
	 * @return ErrorResponse
	 */
	public static ErrorResponse of(final GlobalErrorCode code) {
		return new ErrorResponse(code);
	}

	/**
	 * Global Exception 전송 타입-3
	 *
	 * @param code   GlobalErrorCode
	 * @param reason String
	 * @return ErrorResponse
	 */
	public static ErrorResponse of(final GlobalErrorCode code, final String reason) {
		return new ErrorResponse(code, reason);
	}

	/**
	 * 에러를 e.getBindingResult() 형태로 전달 받는 경우 해당 내용을 상세 내용으로 변경하는 기능을 수행한다.
	 */
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

		public static List<FieldError> of(final String field, final String value, final String reason) {
			List<FieldError> fieldErrors = new ArrayList<>();
			fieldErrors.add(new FieldError(field, value, reason));
			return fieldErrors;
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