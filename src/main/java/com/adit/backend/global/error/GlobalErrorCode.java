package com.adit.backend.global.error;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * [공통 코드] API 통신에 대한 '에러 코드'를 Enum 형태로 관리를 한다.
 * Global Error CodeList : 전역으로 발생하는 에러코드를 관리한다.
 * Custom Error CodeList : 업무 페이지에서 발생하는 에러코드를 관리한다
 * Error Code Constructor : 에러코드를 직접적으로 사용하기 위한 생성자를 구성한다.
 *
 * @author lee
 */

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum GlobalErrorCode implements ErrorCode {
	/**
	 * ******************************* Global Error CodeList ***************************************
	 * HTTP Status Code
	 * 400 : Bad Request
	 * 401 : Unauthorized
	 * 403 : Forbidden
	 * 404 : Not Found
	 * 500 : Internal Server Error
	 * *********************************************************************************************
	 */
	// 잘못된 요청
	BAD_REQUEST_ERROR(BAD_REQUEST, "G001", "Bad Request Exception"),

	// @RequestBody 데이터 미 존재
	REQUEST_BODY_MISSING_ERROR(BAD_REQUEST, "G002", "Required request body is missing"),

	// 유효하지 않은 타입
	INVALID_TYPE_VALUE(BAD_REQUEST, "G003", " Invalid Type Value"),

	// Request Parameter 로 데이터가 전달되지 않을 경우
	MISSING_REQUEST_PARAMETER_ERROR(BAD_REQUEST, "G004", "Missing Servlet RequestParameter Exception"),

	// 입력/출력 값이 유효하지 않음
	IO_ERROR(BAD_REQUEST, "G005", "I/O Exception"),

	// 권한이 없음
	FORBIDDEN_ERROR(FORBIDDEN, "G008", "Forbidden Exception"),

	// 서버로 요청한 리소스가 존재하지 않음
	NOT_FOUND_ERROR(NOT_FOUND, "G009", "Not Found Exception"),

	// NULL Point Exception 발생
	NULL_POINT_ERROR(NOT_FOUND, "G010", "Null Point Exception"),

	// @RequestBody 및 @RequestParam, @PathVariable 값이 유효하지 않음
	NOT_VALID_ERROR(NOT_FOUND, "G011", "handle Validation Exception"),

	// @RequestBody 및 @RequestParam, @PathVariable 값이 유효하지 않음
	NOT_VALID_HEADER_ERROR(NOT_FOUND, "G012", "Header에 데이터가 존재하지 않는 경우 "),

	SERVLET_ERROR(BAD_REQUEST, "G013", "Servlet Exception"),

	// 서버가 처리 할 방법을 모르는 경우 발생
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G999", "Internal Server Error Exception"),

	/**
	 * ******************************* Custom Error CodeList ***************************************
	 */
	// Transaction Insert Error
	INSERT_ERROR(OK, "9999", "Insert Transaction Error Exception"),

	// Transaction Update Error
	UPDATE_ERROR(OK, "9999", "Update Transaction Error Exception"),

	// Transaction Delete Error
	DELETE_ERROR(OK, "9999", "Delete Transaction Error Exception"),

	// auth
	ILLEGAL_REGISTRATION_ID(NOT_ACCEPTABLE, "1000", "잘못된 등록 ID입니다."),

	ACCESS_TOKEN_EXPIRED(UNAUTHORIZED, "1001", "토큰이 만료되었습니다."),

	INVALID_TOKEN(UNAUTHORIZED, "1002", "올바르지 않은 토큰입니다."),

	INVALID_JWT_SIGNATURE(UNAUTHORIZED, "1003", "잘못된 JWT 시그니처입니다."),

	TOKEN_NOT_FOUND(UNAUTHORIZED, "1004", "토큰을 찾지 못했습니다."),

	TOKEN_ALREADY_EXIST(UNAUTHORIZED, "1005", "토큰이 이미 생성되었습니다!"),

	REFRESH_TOKEN_EXPIRED(UNAUTHORIZED, "1006", "리프레쉬 토큰이 만료되었습니다."),

	TOKEN_UNSURPPORTED(UNAUTHORIZED, "1007", "지원되지 않는 토큰입니다."),

	TOKEN_DELETE_FAILED(UNAUTHORIZED, "1008", "토큰 삭제를 실패했습니다."),
	//user
	USER_NOT_FOUND(NOT_FOUND, "2001", "사용자를 찾지 못했습니다."),

	NICKNAME_ALREADY_EXIST(BAD_REQUEST, "2002", "이미 존재하는 닉네임입니다."),

	//Auth
	KAKAO_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "3001", "카카오 서버에 에러가 발생했습니다."),

	KAKAO_SERVER_CONNECTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "3002", "카카오 서버에 연결을 실패했습니다."),

	API_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "3003", "API 호출을 실패했습니다."),

	LOGOUT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "3004" , "로그아웃에 실패했습니다."),


	FAIL_CONVERT_RESPONSE(NOT_MODIFIED,"9001" , "응답 변환에 실패했습니다."),

	// Crawling
	INVALID_URL(BAD_REQUEST, "501" , "유효하지 않은 URL입니다."),

	PLATFORM_NOT_SUPPORTED(NOT_FOUND,"502" , "지원하지 않는 플랫폼입니다."),

	CRAWLING_FAILED(BAD_REQUEST,"503" ,"크롤링을 실패했습니다." ),

	//scraper
	SCRAPER_API_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "4001", "API 호출을 실패했습니다"),

	FIELD_NOT_FOUND(NOT_FOUND, "4002", "해당 필드를 찾지 못했습니다"),

	//place
	COMMON_PLACE_NOT_FOUND(NOT_FOUND, "5001", "해당 Common Place 를 찾지 못했습니다"),

	USER_PLACE_NOT_FOUND(NOT_FOUND, "5002", "해당 User Place 를 찾지 못했습니다"),

	FRIEND_NOT_FOUND(NOT_FOUND, "5003", "등록된 친구가 없습니다"),

	ID_NOT_FOUND_ERROR(NOT_FOUND, "5002", "해당 ID를 찾지 못했습니다"),

	FIELD_NOT_FOUND(NOT_FOUND, "4002", "해당 필드를 찾지 못했습니다"),

	// 이벤트 관련 오류 코드 추가
	EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "5001", "이벤트를 찾을 수 없습니다."),

	EVENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "5002", "이미 존재하는 이벤트입니다."),

	EVENT_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "5003", "이벤트 생성에 실패했습니다."),

	EVENT_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "5004", "이벤트 업데이트에 실패했습니다."),

	EVENT_DELETION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "5005", "이벤트 삭제에 실패했습니다.");

	// 에러 코드의 '코드 상태'을 반환한다.
	private final HttpStatus httpStatus;

	// 에러 코드의 '코드간 구분 값'을 반환한다.
	private final String code;

	// 에러 코드의 '코드 메시지'을 반환한다.
	private final String message;

	@Override
	public HttpStatus getHttpStatus() {
		return this.httpStatus;
	}

	@Override
	public String getMessage() {
		return this.message;
	}
}


