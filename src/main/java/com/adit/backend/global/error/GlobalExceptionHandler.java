package com.adit.backend.global.error;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.adit.backend.domain.event.exception.EventAlreadyExistsException;
import com.adit.backend.domain.event.exception.EventNotFoundException;
import com.adit.backend.domain.place.exception.CommonPlaceNotFoundException;
import com.adit.backend.domain.place.exception.FriendNotFoundException;
import com.adit.backend.domain.place.exception.NotValidException;
import com.adit.backend.domain.place.exception.UserPlaceNotFoundException;
import com.adit.backend.domain.user.exception.UserException;
import com.adit.backend.global.common.ApiResponse;
import com.adit.backend.global.error.exception.TokenException;
import com.adit.backend.infra.crawler.exception.CrawlingException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller 내에서 발생하는 Exception 대해서 Catch 하여 응답을 보내는 전역 핸들러
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final HttpStatus HTTP_STATUS_OK = HttpStatus.OK;
	private static final HttpStatus HTTP_STATUS_BAD_REQUEST = HttpStatus.BAD_REQUEST;

	/**
	 * [Exception] API 호출 시 '객체' 혹은 '파라미터' 데이터 값이 유효하지 않은 경우
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException ex, HttpServletRequest request) {

		log.error("[Error] handleMethodArgumentNotValidException: {}", ex.getMessage());
		log.error("[Error] 예외 발생 지점: {} | {}", request.getMethod(), request.getRequestURI());

		BindingResult bindingResult = ex.getBindingResult();
		StringBuilder stringBuilder = new StringBuilder();
		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			stringBuilder.append(fieldError.getField()).append(":");
			stringBuilder.append(fieldError.getDefaultMessage());
			stringBuilder.append(", ");
		}

		ErrorResponse response = ErrorResponse.of(GlobalErrorCode.NOT_VALID_ERROR, String.valueOf(stringBuilder));
		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] API 호출 시 'Header' 내에 데이터 값이 유효하지 않은 경우
	 */
	@ExceptionHandler(MissingRequestHeaderException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleMissingRequestHeaderException(
		MissingRequestHeaderException ex, HttpServletRequest request) {

		log.error("[Error] MissingRequestHeaderException: {}", ex.getMessage());
		log.error("[Error] 예외 발생 지점: {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(GlobalErrorCode.REQUEST_BODY_MISSING_ERROR, ex.getMessage());
		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] 클라이언트에서 Body로 '객체' 데이터가 넘어오지 않았을 경우
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException ex, HttpServletRequest request) {

		log.error("[Error] HttpMessageNotReadableException: {}", ex.getMessage());
		log.error("[Error] 예외 발생 지점: {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(GlobalErrorCode.REQUEST_BODY_MISSING_ERROR, ex.getMessage());
		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_BAD_REQUEST);
	}

	/**
	 * [Exception] 클라이언트에서 request로 '파라미터로' 데이터가 넘어오지 않았을 경우
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleMissingRequestHeaderExceptionException(
		MissingServletRequestParameterException ex, HttpServletRequest request) {

		log.error("[Error] MissingServletRequestParameterException: {}", ex.getMessage());
		log.error("[Error] 예외 발생 지점: {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(GlobalErrorCode.MISSING_REQUEST_PARAMETER_ERROR, ex.getMessage());
		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_BAD_REQUEST);
	}

	/**
	 * [Exception] 잘못된 서버 요청일 경우 발생한 경우
	 */
	@ExceptionHandler(HttpClientErrorException.BadRequest.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleBadRequestException(
		HttpClientErrorException.BadRequest ex, HttpServletRequest request) {

		log.error("[Error] HttpClientErrorException.BadRequest: {}", ex.getMessage());
		log.error("[Error] 예외 발생 지점: {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(GlobalErrorCode.BAD_REQUEST_ERROR, ex.getMessage());
		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] 잘못된 주소로 요청한 경우
	 */
	@ExceptionHandler(NoHandlerFoundException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleNoHandlerFoundExceptionException(
		NoHandlerFoundException ex, HttpServletRequest request) {

		log.error("[Error] NoHandlerFoundException: {}", ex.getMessage());
		log.error("[Error] 예외 발생 지점: {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(GlobalErrorCode.NOT_FOUND_ERROR, ex.getMessage());
		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] NULL 값이 발생한 경우
	 */
	@ExceptionHandler(NullPointerException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleNullPointerException(
		NullPointerException ex, HttpServletRequest request) {

		log.error("[Error] NullPointerException: {}", ex.getMessage());
		log.error("[Error] 예외 발생 지점: {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(GlobalErrorCode.NULL_POINT_ERROR, ex.getMessage());
		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * Input / Output 내에서 발생한 경우
	 */
	@ExceptionHandler(IOException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleIOException(IOException ex,
		HttpServletRequest request) {

		log.error("[Error] IOException: {}", ex.getMessage());
		log.error("[Error] 예외 발생 지점: {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(GlobalErrorCode.IO_ERROR, ex.getMessage());
		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] UserException 처리
	 */
	@ExceptionHandler(UserException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleUserException(UserException ex,
		HttpServletRequest request) {

		log.error("[Error] 유저 예외 발생: {}", ex.getMessage());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(ex.getErrorCode(), request.getRequestURI());
		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] TokenException 처리
	 */
	@ExceptionHandler(TokenException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleTokenException(TokenException ex,
		HttpServletRequest request) {

		log.error("[Error] 토큰 예외 발생: {}", ex.getErrorCode().getMessage());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(ex.getErrorCode(), request.getRequestURI());
		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] CommonPlace를 찾지 못한 경우
	 */
	@ExceptionHandler(CommonPlaceNotFoundException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleCommonPlaceNotFoundException(
		CommonPlaceNotFoundException ex, HttpServletRequest request) {

		log.error("[Error] CommonPlaceNotFoundException: {}", ex.getMessage());
		log.error("[Error] 예외 발생 지점: {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse errorResponse = ErrorResponse.of(GlobalErrorCode.COMMON_PLACE_NOT_FOUND, ex.getMessage());
		return new ResponseEntity<>(ApiResponse.failure(errorResponse), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] UserPlace를 찾지 못한 경우
	 */
	@ExceptionHandler(UserPlaceNotFoundException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleUserPlaceNotFoundException(UserPlaceNotFoundException ex,
		HttpServletRequest request) {

		log.error("[Error] UserPlaceNotFoundException: {}", ex.getMessage());
		log.error("[Error] 예외 발생 지점: {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse errorResponse = ErrorResponse.of(GlobalErrorCode.USER_PLACE_NOT_FOUND, ex.getMessage());
		return new ResponseEntity<>(ApiResponse.failure(errorResponse), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] Friend 를 찾지 못한 경우
	 */
	@ExceptionHandler(FriendNotFoundException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleFriendNotFoundException(
		FriendNotFoundException ex, HttpServletRequest request) {

		log.error("[Error] FriendNotFoundException: {}", ex.getMessage());
		log.error("[Error] 예외 발생 지점: {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse errorResponse = ErrorResponse.of(GlobalErrorCode.FRIEND_NOT_FOUND, ex.getMessage());
		return new ResponseEntity<>(ApiResponse.failure(errorResponse), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] 요청인자가 유효하지 않은 경우
	 */
	@ExceptionHandler(NotValidException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleNotValidException(
		NotValidException ex, HttpServletRequest request) {

		log.error("[Error] NotValidException: {}", ex.getMessage());
		log.error("[Error] 예외 발생 지점: {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse errorResponse = ErrorResponse.of(GlobalErrorCode.NOT_VALID, ex.getMessage());
		return new ResponseEntity<>(ApiResponse.failure(errorResponse), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] 이벤트를 찾을 수 없을 때
	 */
	@ExceptionHandler(EventNotFoundException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleEventNotFoundException(
		EventNotFoundException ex, HttpServletRequest request) {

		log.error("[Error] EventNotFoundException: {}", ex.getMessage());
		log.error("[Error] 예외 발생 지점: {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(GlobalErrorCode.EVENT_NOT_FOUND, ex.getMessage());
		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] 이미 존재하는 이벤트일 때
	 */
	@ExceptionHandler(EventAlreadyExistsException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleEventAlreadyExistsException(
		EventAlreadyExistsException ex, HttpServletRequest request) {

		log.error("[Error] EventAlreadyExistsException: {}", ex.getMessage());
		log.error("[Error] 예외 발생 지점: {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(GlobalErrorCode.EVENT_ALREADY_EXISTS, ex.getMessage());
		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] CrawlingException 처리
	 */
	@ExceptionHandler(CrawlingException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleCrawlingException(CrawlingException ex,
		HttpServletRequest request) {

		log.error("[Error] 크롤링 예외 발생: {}", ex.getErrorCode().getMessage());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(ex.getErrorCode(), request.getRequestURI());
		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}
}
