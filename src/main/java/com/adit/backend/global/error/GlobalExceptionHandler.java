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
import com.adit.backend.domain.event.exception.EventException;
import com.adit.backend.domain.image.exception.ImageException;
import com.adit.backend.domain.notification.exception.NotificationException;
import com.adit.backend.domain.place.exception.PlaceException;
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

		log.error("[Error] 유효성 검증 실패: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} ", (Object)ex.getStackTrace());
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

		log.error("[Error] 요청 헤더 누락: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} ", (Object)ex.getStackTrace());
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

		log.error("[Error] Request Body 누락: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} ", (Object)ex.getStackTrace());
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

		log.error("[Error] Request Parameter 누락: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} ", (Object)ex.getStackTrace());
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

		log.error("[Error] 잘못된 서버요청: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} ", (Object)ex.getStackTrace());
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

		log.error("[Error] 존재하지 않는 요청 주소: {}", ex.getMessage());
		log.error("[Error] 발생 이유: {} ", (Object)ex.getStackTrace());
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
		log.error("[Error] 발생 이유: {} ", (Object)ex.getStackTrace());
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
		log.error("[Error] 발생 이유: {} ", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			GlobalErrorCode.IO_ERROR,
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

		log.error("[Error] 크롤링 관련 예외 발생: {}", ex.getErrorCode().getMessage());
		log.error("[Error] 발생 이유: {} ", (Object)ex.getStackTrace());
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
		log.error("[Error] 발생 이유: {} ", (Object)ex.getStackTrace());
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

		log.error("[Error] 토큰 관련 예외 발생: {}", ex.getErrorCode().getMessage());
		log.error("[Error] 발생 이유: {} ", (Object)ex.getStackTrace());
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

		log.error("[Error] AI 관련 예외 발생: {}", ex.getErrorCode().getMessage());
		log.error("[Error] 발생 이유: {} ", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			ex.getErrorCode(),
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(response), HTTP_STATUS_OK);
	}

	/**
	 * [Exception] 친구 관련 오류 (FriendShipException)
	 *
	 * @param ex      FriendShipException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(FriendShipException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleFriendShipException(
		FriendShipException ex, HttpServletRequest request) {

		log.error("[Error] 친구 관련 예외 발생: {}", ex.getErrorCode().getMessage());
		log.error("[Error] 발생 이유: {} ", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			ex.getErrorCode(),
			ex.getMessage(),
			request.getRequestURI()
		);
		return new ResponseEntity<>(ApiResponse.failure(response), HttpStatus.OK);
	}

	/**
	 * [Exception] 알림 관련 오류 (NotificationException)
	 *
	 * @param ex      NotificationException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(NotificationException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleNotificationException(
		NotificationException ex, HttpServletRequest request) {

		log.error("[Error] 알림 관련 예외 발생: {}", ex.getErrorCode().getMessage());
		log.error("[Error] 발생 이유: {} ", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			ex.getErrorCode(),
			ex.getMessage(),
			request.getRequestURI()
		);
		return new ResponseEntity<>(ApiResponse.failure(response), HttpStatus.OK);
	}

	/**
	 * [Exception] 이미지 관련 오류 (ImageException)
	 *
	 * @param ex      ImageException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(ImageException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleImageException(
		ImageException ex, HttpServletRequest request) {

		log.error("[Error] 이미지 관련 예외 발생: {}", ex.getErrorCode().getMessage());
		log.error("[Error] 발생 이유: {} ", (Object)ex.getStackTrace());
		log.error("[Error] 예외 발생 지점 : {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			ex.getErrorCode(),
			ex.getMessage(),
			request.getRequestURI()
		);
		return new ResponseEntity<>(ApiResponse.failure(response), HttpStatus.OK);
	}

	/**
	 * [Exception] 이벤트 관련 오류 (EventException)
	 *
	 * @param ex      EventException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse < ErrorResponse>>
	 */
	@ExceptionHandler(EventException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleEventException(
		EventException ex, HttpServletRequest request) {

		log.error("[Error] 이벤트 관련 에러 발생: {}", ex.getErrorCode().getMessage());
		log.error("[Error] 발생 이유: {} :", ex.getStackTrace());
		log.error("[Error] 예외 발생 지점: {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			ex.getErrorCode(),
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(response), HttpStatus.OK);
	}

	/**
	 * [Exception] 장소 관련 오류 (PlaceException)
	 *
	 * @param ex      PlaceException
	 * @param request HttpServletRequest
	 * @return ResponseEntity<ApiResponse<ErrorResponse>>
	 */
	@ExceptionHandler(PlaceException.class)
	protected ResponseEntity<ApiResponse<ErrorResponse>> handleEventException(
		PlaceException ex, HttpServletRequest request) {

		log.error("[Error] 장소 관련 에러 발생: {}", ex.getErrorCode().getMessage());
		log.error("[Error] 발생 이유: {} :", ex.getStackTrace());
		log.error("[Error] 예외 발생 지점: {} | {}", request.getMethod(), request.getRequestURI());

		ErrorResponse response = ErrorResponse.of(
			ex.getErrorCode(),
			ex.getMessage(),
			request.getRequestURI()
		);

		return new ResponseEntity<>(ApiResponse.failure(response), HttpStatus.OK);
	}

}
