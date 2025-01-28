package com.adit.backend.global.error;

import org.apache.http.HttpStatus;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 각 도메인별 코드 형태를 [도메인-001] 방식으로 변경하고,
 * HTTP Status를 공식 문서를 참고하여 재검증한 에러 코드 모음
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public enum GlobalErrorCode implements ErrorCode {

	/********************************** Global Domain **********************************/
	BAD_REQUEST_ERROR(HttpStatus.SC_BAD_REQUEST, "GBL-001", "잘못된 요청입니다."),
	REQUEST_BODY_MISSING_ERROR(HttpStatus.SC_BAD_REQUEST, "GBL-002", "필수 요청 본문이 누락되었습니다."),
	INVALID_TYPE_VALUE(HttpStatus.SC_BAD_REQUEST, "GBL-003", "유효하지 않은 타입 값입니다."),
	MISSING_REQUEST_PARAMETER_ERROR(HttpStatus.SC_BAD_REQUEST, "GBL-004", "요청 파라미터가 누락되었습니다."),
	IO_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR, "GBL-005", "입출력 처리 중 오류가 발생했습니다."),
	FORBIDDEN_ERROR(HttpStatus.SC_FORBIDDEN, "GBL-006", "권한이 없습니다."),
	NOT_FOUND_ERROR(HttpStatus.SC_NOT_FOUND, "GBL-007", "요청한 리소스를 찾을 수 없습니다."),
	NULL_POINT_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR, "GBL-008", "서버 처리 중 Null Pointer Exception이 발생했습니다."),
	NOT_VALID_ERROR(HttpStatus.SC_BAD_REQUEST, "GBL-009", "유효성 검사에 실패했습니다."),
	NOT_VALID_HEADER_ERROR(HttpStatus.SC_BAD_REQUEST, "GBL-010", "헤더 데이터가 유효하지 않습니다."),
	SERVLET_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR, "GBL-011", "서블릿 처리 중 오류가 발생했습니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR, "GBL-012", "서버 내부 오류가 발생했습니다."),

	/********************************** Transaction Domain **********************************/
	INSERT_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR, "TRX-001", "Insert Transaction Error Exception"),
	UPDATE_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR, "TRX-002", "Update Transaction Error Exception"),
	DELETE_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR, "TRX-003", "Delete Transaction Error Exception"),

	/********************************** Auth Domain **********************************/
	ILLEGAL_REGISTRATION_ID(HttpStatus.SC_BAD_REQUEST, "AUTH-001", "잘못된 등록 ID입니다."),
	ACCESS_TOKEN_EXPIRED(HttpStatus.SC_UNAUTHORIZED, "AUTH-002", "토큰이 만료되었습니다."),
	INVALID_TOKEN(HttpStatus.SC_UNAUTHORIZED, "AUTH-003", "올바르지 않은 토큰입니다."),
	INVALID_JWT_SIGNATURE(HttpStatus.SC_UNAUTHORIZED, "AUTH-004", "잘못된 JWT 시그니처입니다."),
	TOKEN_NOT_FOUND(HttpStatus.SC_UNAUTHORIZED, "AUTH-005", "토큰을 찾지 못했습니다."),
	TOKEN_ALREADY_EXIST(HttpStatus.SC_CONFLICT, "AUTH-006", "토큰이 이미 생성되었습니다."),
	REFRESH_TOKEN_EXPIRED(HttpStatus.SC_UNAUTHORIZED, "AUTH-007", "리프레쉬 토큰이 만료되었습니다."),
	TOKEN_UNSURPPORTED(HttpStatus.SC_UNAUTHORIZED, "AUTH-008", "지원되지 않는 토큰입니다."),
	TOKEN_DELETE_FAILED(HttpStatus.SC_INTERNAL_SERVER_ERROR, "AUTH-009", "토큰 삭제를 실패했습니다."),

	/********************************** User Domain **********************************/
	USER_NOT_FOUND(HttpStatus.SC_NOT_FOUND, "USR-001", "사용자를 찾지 못했습니다."),
	NICKNAME_ALREADY_EXIST(HttpStatus.SC_BAD_REQUEST, "USR-002", "이미 존재하는 닉네임입니다."),

	/********************************** Kakao Domain **********************************/
	KAKAO_SERVER_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR, "KKO-001", "카카오 서버에 에러가 발생했습니다."),
	KAKAO_SERVER_CONNECTION_FAILED(HttpStatus.SC_INTERNAL_SERVER_ERROR, "KKO-002", "카카오 서버에 연결을 실패했습니다."),
	API_REQUEST_FAILED(HttpStatus.SC_INTERNAL_SERVER_ERROR, "KKO-003", "API 호출을 실패했습니다."),
	LOGOUT_FAILED(HttpStatus.SC_INTERNAL_SERVER_ERROR, "KKO-004", "로그아웃에 실패했습니다."),
	FAIL_CONVERT_RESPONSE(HttpStatus.SC_INTERNAL_SERVER_ERROR, "KKO-005", "응답 변환에 실패했습니다."),

	/********************************** Crawling Domain **********************************/
	INVALID_URL(HttpStatus.SC_BAD_REQUEST, "CRW-001", "유효하지 않은 URL입니다."),
	PLATFORM_NOT_SUPPORTED(HttpStatus.SC_BAD_REQUEST, "CRW-002", "지원하지 않는 플랫폼입니다."),
	CRAWLING_FAILED(HttpStatus.SC_INTERNAL_SERVER_ERROR, "CRW-003", "크롤링을 실패했습니다."),
	IFRAME_CRAWLING_FAILED(HttpStatus.SC_INTERNAL_SERVER_ERROR, "CRW-004", "iframe 크롤링에 실패했습니다."),
	CONTENT_EMPTY(HttpStatus.SC_MULTI_STATUS, "CRW-005", "크롤링된 컨텐츠가 비어있습니다"),
	IMAGE_EXTRACTION_FAILED(HttpStatus.SC_INTERNAL_SERVER_ERROR, "CRW-006", "이미지 추출에 실패했습니다"),
	TITLE_EXTRACTION_FAILED(HttpStatus.SC_BAD_REQUEST, "CRW-007", "제목 추출에 실패했습니다"),
	BODY_EXTRACTION_FAILED(HttpStatus.SC_INTERNAL_SERVER_ERROR, "CRW-008", "본문 추출에 실패했습니다"),
	TEXT_PREPROCESSING_FAILED(HttpStatus.SC_BAD_REQUEST, "CRW-009", "텍스트 전처리에 실패했습니다"),
	CONTENT_NOT_FOUND(HttpStatus.SC_NOT_FOUND, "CRW-010", "본문 컨텐츠를 찾을 수 없습니다"),
	TISTORY_PARSING_FAILED(HttpStatus.SC_BAD_REQUEST, "CRW-011", "티스토리 파싱에 실패했습니다"),
	SKIN_SELECTOR_NOT_FOUND(HttpStatus.SC_NOT_FOUND, "CRW-012", "적절한 스킨 선택자를 찾을 수 없습니다"),
	CHUNK_PROCESSING_FAILED(HttpStatus.SC_INTERNAL_SERVER_ERROR, "CRW-013", "청크 처리에 실패했습니다"),
	PLACE_EXTRACTION_FAILED(HttpStatus.SC_INTERNAL_SERVER_ERROR, "CRW-014", "장소 정보 추출에 실패했습니다"),
	INSTAGRAM_PARSING_FAILED(HttpStatus.SC_BAD_REQUEST, "CRW-015", "인스타그램 파싱에 실패했습니다"),
	INSTAGRAM_API_CONNECTION_FAILED(HttpStatus.SC_INTERNAL_SERVER_ERROR, "CRW-016", "인스타그램 API 연결에 실패했습니다"),
	INSTAGRAM_DATASET_NOT_FOUND(HttpStatus.SC_NOT_FOUND, "CRW-017", "인스타그램 데이터셋을 찾을 수 없습니다"),
	INSTAGRAM_CONTENT_EMPTY(HttpStatus.SC_MULTI_STATUS, "CRW-018", "인스타그램 컨텐츠가 비어있습니다"),
	INSTAGRAM_IMAGE_NOT_FOUND(HttpStatus.SC_NOT_FOUND, "CRW-019", "인스타그램 이미지를 찾을 수 없습니다"),

	/********************************** Scraper Domain **********************************/
	SCRAPER_API_FAILED(HttpStatus.SC_INTERNAL_SERVER_ERROR, "SCR-001", "API 호출을 실패했습니다"),
	FIELD_NOT_FOUND(HttpStatus.SC_NOT_FOUND, "SCR-002", "해당 필드를 찾지 못했습니다"),

	/********************************** Place Domain **********************************/
	COMMON_PLACE_NOT_FOUND(HttpStatus.SC_NOT_FOUND, "PLC-001", "해당 Common Place 를 찾지 못했습니다"),
	USER_PLACE_NOT_FOUND(HttpStatus.SC_NOT_FOUND, "PLC-002", "해당 User Place 를 찾지 못했습니다"),
	FRIEND_NOT_FOUND(HttpStatus.SC_NOT_FOUND, "PLC-003", "등록된 친구가 없습니다"),
	NOT_VALID(HttpStatus.SC_BAD_REQUEST, "PLC-004", "요청인자가 유효하지 않습니다"),
	ID_NOT_FOUND_ERROR(HttpStatus.SC_NOT_FOUND, "PLC-005", "해당 ID를 찾지 못했습니다"),

	/********************************** Event Domain **********************************/
	EVENT_NOT_FOUND(HttpStatus.SC_NOT_FOUND, "EVN-001", "이벤트를 찾을 수 없습니다."),
	EVENT_ALREADY_EXISTS(HttpStatus.SC_BAD_REQUEST, "EVN-002", "이미 존재하는 이벤트입니다."),
	EVENT_CREATION_FAILED(HttpStatus.SC_INTERNAL_SERVER_ERROR, "EVN-003", "이벤트 생성에 실패했습니다."),
	EVENT_UPDATE_FAILED(HttpStatus.SC_INTERNAL_SERVER_ERROR, "EVN-004", "이벤트 업데이트에 실패했습니다."),
	EVENT_DELETION_FAILED(HttpStatus.SC_INTERNAL_SERVER_ERROR, "EVN-005", "이벤트 삭제에 실패했습니다.");

	private final int httpStatus;
	private final String code;
	private final String message;

	@Override
	public int getHttpStatus() {
		return this.httpStatus;
	}

	@Override
	public String getMessage() {
		return this.message;
	}
}
