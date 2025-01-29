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

import com.adit.backend.domain.ai.exception.AiException;
import com.adit.backend.domain.event.exception.EventAlreadyExistsException;
import com.adit.backend.domain.event.exception.EventNotFoundException;
import com.adit.backend.domain.place.exception.CommonPlaceNotFoundException;
import com.adit.backend.domain.place.exception.FriendNotFoundException;
import com.adit.backend.domain.place.exception.NotValidException;
import com.adit.backend.domain.place.exception.UserPlaceNotFoundException;
import com.adit.backend.domain.user.exception.FriendShipException;
import com.adit.backend.domain.user.exception.UserException;
import com.adit.backend.global.common.ApiResponse;
import com.adit.backend.global.security.jwt.exception.TokenException;
import com.adit.backend.infra.crawler.exception.CrawlingException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * [Exception] Controller 내에서 발생하는 Exception을 Catch 하여
 * 적절한 응답을 보내는 전역 예외 처리기
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final HttpStatus HTTP_STATUS_OK = HttpStatus.OK;
	private static final HttpStatus HTTP_STATUS_BAD_REQUEST = HttpStatus.BAD_REQUEST;

	/**
	 * [Exception] 유효성 검증 실패 (MethodArgumentNotValidException)
	 *
	 * @param ex      MethodArgumentNotValidException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException ex, HttpServletRequest request) {

		log.error("[Error] handleMethodArgumentNotValidException: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		BindingResult bindingResult = ex.getBindingResult();
		StringBuilder stringBuilder = new StringBuilder();
		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			stringBuilder.append(fieldError.getField()).append(":");
			stringBuilder.append(fieldError.getDefaultMessage());
			stringBuilder.append(", ");
		}
		ErrorResponse response = ErrorResponse.of(
			GlobalErrorCode.NOT_VALID_ERROR,
			request.getRequestURI(),
			String.valueOf(stringBuilder));

		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] 요청 Header 누락 (MissingRequestHeaderException)
	 *
	 * @param ex      MissingRequestHeaderException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(MissingRequestHeaderException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleMissingRequestHeaderException(
		MissingRequestHeaderException ex, HttpServletRequest request) {

		log.error("[Error] MissingRequestHeaderException: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			GlobalErrorCode.REQUEST_BODY_MISSING_ERROR,
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] Request Body 누락 (HttpMessageNotReadableException)
	 *
	 * @param ex      HttpMessageNotReadableException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException ex, HttpServletRequest request) {

		log.error("[Error] HttpMessageNotReadableException: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			GlobalErrorCode.REQUEST_BODY_MISSING_ERROR,
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_BAD_REQUEST);
	}

	/**
	 * [Exception] Request Parameter 누락 (MissingServletRequestParameterException)
	 *
	 * @param ex      MissingServletRequestParameterException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleMissingRequestHeaderExceptionException(
		MissingServletRequestParameterException ex, HttpServletRequest request) {

		log.error("[Error] MissingServletRequestParameterException: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			GlobalErrorCode.MISSING_REQUEST_PARAMETER_ERROR,
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_BAD_REQUEST);
	}

	/**
	 * [Exception] 잘못된 서버 요청 (HttpClientErrorException.BadRequest)
	 *
	 * @param ex      HttpClientErrorException.BadRequest
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(HttpClientErrorException.BadRequest.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleBadRequestException(
		HttpClientErrorException.BadRequest ex, HttpServletRequest request) {

		log.error("[Error] HttpClientErrorException.BadRequest: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			GlobalErrorCode.BAD_REQUEST_ERROR,
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] 존재하지 않는 요청 주소 (NoHandlerFoundException)
	 *
	 * @param ex      NoHandlerFoundException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(NoHandlerFoundException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleNoHandlerFoundExceptionException(
		NoHandlerFoundException ex, HttpServletRequest request) {

		log.error("[Error] NoHandlerFoundException: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			GlobalErrorCode.NOT_FOUND_ERROR,
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] NullPointerException
	 *
	 * @param ex      NullPointerException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(NullPointerException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleNullPointerException(
		NullPointerException ex, HttpServletRequest request) {

		log.error("[Error] NullPointerException: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			GlobalErrorCode.NULL_POINT_ERROR,
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] IOException
	 *
	 * @param ex      IOException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(IOException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleIOException(IOException ex, HttpServletRequest request) {

		log.error("[Error] IOException: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			GlobalErrorCode.IO_ERROR,
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] CommonPlaceNotFoundException
	 *
	 * @param ex      CommonPlaceNotFoundException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(CommonPlaceNotFoundException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleCommonPlaceNotFoundException(
		CommonPlaceNotFoundException ex, HttpServletRequest request) {

		log.error("[Error] CommonPlaceNotFoundException: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse errorResponse = ErrorResponse.of(
			GlobalErrorCode.COMMON_PLACE_NOT_FOUND,
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(errorResponse), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] UserPlaceNotFoundException
	 *
	 * @param ex      UserPlaceNotFoundException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(UserPlaceNotFoundException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleUserPlaceNotFoundException(
		UserPlaceNotFoundException ex, HttpServletRequest request) {

		log.error("[Error] UserPlaceNotFoundException: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse errorResponse = ErrorResponse.of(
			GlobalErrorCode.USER_PLACE_NOT_FOUND,
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(errorResponse), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] FriendNotFoundException
	 *
	 * @param ex      FriendNotFoundException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(FriendNotFoundException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleFriendNotFoundException(
		FriendNotFoundException ex, HttpServletRequest request) {

		log.error("[Error] FriendNotFoundException: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse errorResponse = ErrorResponse.of(
			GlobalErrorCode.FRIEND_NOT_FOUND,
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(errorResponse), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] 잘못된 요청 인자 (NotValidException)
	 *
	 * @param ex      NotValidException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(NotValidException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleNotValidException(
		NotValidException ex, HttpServletRequest request) {

		log.error("[Error] NotValidException: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse errorResponse = ErrorResponse.of(
			GlobalErrorCode.NOT_VALID,
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(errorResponse), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] 이벤트를 찾을 수 없을 때 (EventNotFoundException)
	 *
	 * @param ex      EventNotFoundException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(EventNotFoundException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleEventNotFoundException(
		EventNotFoundException ex, HttpServletRequest request) {

		log.error("[Error] EventNotFoundException: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			GlobalErrorCode.EVENT_NOT_FOUND,
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] 이미 존재하는 이벤트 (EventAlreadyExistsException)
	 *
	 * @param ex      EventAlreadyExistsException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(EventAlreadyExistsException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleEventAlreadyExistsException(
		EventAlreadyExistsException ex, HttpServletRequest request) {

		log.error("[Error] EventAlreadyExistsException: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			GlobalErrorCode.EVENT_ALREADY_EXISTS,
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] 크롤링 관련 오류 (CrawlingException)
	 *
	 * @param ex      CrawlingException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(CrawlingException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleCrawlingException(
		CrawlingException ex, HttpServletRequest request) {

		log.error("[Error] 크롤링 예외 발생: {}", ex.getErrorCode().getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			ex.getErrorCode(),
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] UserException
	 *
	 * @param ex      UserException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(UserException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleUserException(
		UserException ex, HttpServletRequest request) {

		log.error("[Error] 유저 예외 발생: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			ex.getErrorCode(),
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] TokenException
	 *
	 * @param ex      TokenException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(TokenException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleTokenException(
		TokenException ex, HttpServletRequest request) {

		log.error("[Error] 토큰 예외 발생: {}", ex.getErrorCode().getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			ex.getErrorCode(),
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] AI 관련 오류 (AiException)
	 *
	 * @param ex      AiException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(AiException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleAiException(
		AiException ex, HttpServletRequest request) {

		log.error("[Error] AI 예외 발생: {}", ex.getErrorCode().getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			ex.getErrorCode(),
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	@ExceptionHandler(FriendShipException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleFriendShipException(
		FriendShipException ex, HttpServletRequest request) {

		log.error("[Error] FriendShip 예외 발생: {}", ex.getErrorCode().getMessage());
		log.error("[Error] 발생 이유: {} :", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			ex.getErrorCode(),
			ex.getMessage(),
			request.getRequestURI()
		);
		return new ResponseEntity<>(ApiResponse.failure(response), HttpStatus.OK);
	}

}
