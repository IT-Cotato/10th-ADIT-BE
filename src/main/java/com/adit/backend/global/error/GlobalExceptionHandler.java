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
import com.adit.backend.domain.place.exception.placeException;
import com.adit.backend.domain.scraper.exception.scraperException;
import com.adit.backend.global.common.ApiResponse;
import com.adit.backend.global.error.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

/**
 * Controller 내에서 발생하는 Exception 대해서 Catch 하여 응답값(Response)을 보내주는 기능을 수행함.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	private final static HttpStatus HTTP_STATUS_OK = HttpStatus.OK;
	private final static HttpStatus HTTP_STATUS_BAD_REQUEST = HttpStatus.BAD_REQUEST;

	/**
	 * [Exception] API 호출 시 '객체' 혹은 '파라미터' 데이터 값이 유효하지 않은 경우
	 *
	 * @param ex MethodArgumentNotValidException
	 * @return ResponseEntity<ErrorResponse>
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
		log.error("handleMethodArgumentNotValidException", ex);
		BindingResult bindingResult = ex.getBindingResult();
		StringBuilder stringBuilder = new StringBuilder();
		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			stringBuilder.append(fieldError.getField()).append(":");
			stringBuilder.append(fieldError.getDefaultMessage());
			stringBuilder.append(", ");
		}
		final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.NOT_VALID_ERROR, String.valueOf(stringBuilder));
		return new ResponseEntity<>(response, HTTP_STATUS_OK);
	}

	/**
	 * [Exception] API 호출 시 'Header' 내에 데이터 값이 유효하지 않은 경우
	 *
	 * @param ex MissingRequestHeaderException
	 * @return ResponseEntity<ErrorResponse>
	 */
	@ExceptionHandler(MissingRequestHeaderException.class)
	protected ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
		log.error("MissingRequestHeaderException", ex);
		final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.REQUEST_BODY_MISSING_ERROR, ex.getMessage());
		return new ResponseEntity<>(response, HTTP_STATUS_OK);
	}

	/**
	 * [Exception] 클라이언트에서 Body로 '객체' 데이터가 넘어오지 않았을 경우
	 *
	 * @param ex HttpMessageNotReadableException
	 * @return ResponseEntity<ErrorResponse>
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException ex) {
		log.error("HttpMessageNotReadableException", ex);
		final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.REQUEST_BODY_MISSING_ERROR, ex.getMessage());
		return new ResponseEntity<>(response, HTTP_STATUS_BAD_REQUEST);
	}

	/**
	 * [Exception] 클라이언트에서 request로 '파라미터로' 데이터가 넘어오지 않았을 경우
	 *
	 * @param ex MissingServletRequestParameterException
	 * @return ResponseEntity<ErrorResponse>
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	protected ResponseEntity<ErrorResponse> handleMissingRequestHeaderExceptionException(
		MissingServletRequestParameterException ex) {
		log.error("handleMissingServletRequestParameterException", ex);
		final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.MISSING_REQUEST_PARAMETER_ERROR,
			ex.getMessage());
		return new ResponseEntity<>(response, HTTP_STATUS_BAD_REQUEST);
	}

	/**
	 * [Exception] 잘못된 서버 요청일 경우 발생한 경우
	 *
	 * @param e HttpClientErrorException
	 * @return ResponseEntity<ErrorResponse>
	 */
	@ExceptionHandler(HttpClientErrorException.BadRequest.class)
	protected ResponseEntity<ErrorResponse> handleBadRequestException(HttpClientErrorException e) {
		log.error("HttpClientErrorException.BadRequest", e);
		final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.BAD_REQUEST_ERROR, e.getMessage());
		return new ResponseEntity<>(response, HTTP_STATUS_OK);
	}

	/**
	 * [Exception] 잘못된 주소로 요청 한 경우
	 *
	 * @param e NoHandlerFoundException
	 * @return ResponseEntity<ErrorResponse>
	 */
	@ExceptionHandler(NoHandlerFoundException.class)
	protected ResponseEntity<ErrorResponse> handleNoHandlerFoundExceptionException(NoHandlerFoundException e) {
		log.error("handleNoHandlerFoundExceptionException", e);
		final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.NOT_FOUND_ERROR, e.getMessage());
		return new ResponseEntity<>(response, HTTP_STATUS_OK);
	}

	/**
	 * [Exception] NULL 값이 발생한 경우
	 *
	 * @param e NullPointerException
	 * @return ResponseEntity<ErrorResponse>
	 */
	@ExceptionHandler(NullPointerException.class)
	protected ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException e) {
		log.error("handleNullPointerException", e);
		final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.NULL_POINT_ERROR, e.getMessage());
		return new ResponseEntity<>(response, HTTP_STATUS_OK);
	}

	/**
	 * Input / Output 내에서 발생한 경우
	 *
	 * @param ex IOException
	 * @return ResponseEntity<ErrorResponse>
	 */
	@ExceptionHandler(IOException.class)
	protected ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
		log.error("handleIOException", ex);
		final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.IO_ERROR, ex.getMessage());
		return new ResponseEntity<>(response, HTTP_STATUS_OK);
	}

	// ==================================================================================================================

	/**
	 * [Exception] 모든 Exception 경우 발생
	 *
	 * @param ex Exception
	 * @return ResponseEntity<ErrorResponse>
	 * GlobalExceptionHandler에서 ApiResponse.failure 메서드를 활용하여 통일된 에러 응답을 반환
	 */
	@ExceptionHandler(Exception.class)
	protected final ResponseEntity<ApiResponse<Object>> handleAllExceptions(Exception ex) {
		log.error("Exception", ex);
		ErrorResponse errorResponse = ErrorResponse.of(GlobalErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage());
		return new ResponseEntity<>(ApiResponse.failure(errorResponse), HttpStatus.OK);
	}


	/**
	 * [Exception] 이벤트를 찾을 수 없을 때
	 *
	 * @param ex EventNotFoundException
	 * @return ResponseEntity<ErrorResponse>
	 */
	@ExceptionHandler(EventNotFoundException.class)
	protected ResponseEntity<ErrorResponse> handleEventNotFoundException(EventNotFoundException ex) {
		log.error("EventNotFoundException", ex);
		final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.EVENT_NOT_FOUND, ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * [Exception] 이미 존재하는 이벤트일 때
	 *
	 * @param ex EventAlreadyExistsException
	 * @return ResponseEntity<ErrorResponse>
	 */
	@ExceptionHandler(EventAlreadyExistsException.class)
	protected ResponseEntity<ErrorResponse> handleEventAlreadyExistsException(EventAlreadyExistsException ex) {
		log.error("EventAlreadyExistsException", ex);
		final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.EVENT_ALREADY_EXISTS, ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	/**
	 * [Exception] 이벤트 생성 실패 시 발생한 경우
	 *
	 * @param ex EventCreationFailedException
	 * @return ResponseEntity<ErrorResponse>
	 */
	// @ExceptionHandler(EventCreationFailedException.class)
	// protected ResponseEntity<ErrorResponse> handleEventCreationFailedException(EventCreationFailedException ex) {
	// 	log.error("EventCreationFailedException", ex);
	// 	final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.EVENT_CREATION_FAILED, ex.getMessage());
	// 	return new ResponseEntity<>(response, HttpStatus.OK);
	// }

	/**
	 * [Exception] 이벤트 업데이트 실패 시 발생한 경우
	 *
	 * @param ex EventUpdateFailedException
	 * @return ResponseEntity<ErrorResponse>
	 */
	// @ExceptionHandler(EventUpdateFailedException.class)
	// protected ResponseEntity<ErrorResponse> handleEventUpdateFailedException(EventUpdateFailedException ex) {
	// 	log.error("EventUpdateFailedException", ex);
	// 	final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.EVENT_UPDATE_FAILED, ex.getMessage());
	// 	return new ResponseEntity<>(response, HttpStatus.OK);
	// }

	/**
	 * [Exception] 이벤트 삭제 실패 시 발생한 경우
	 *
	 * @param ex EventDeletionFailedException
	 * @return ResponseEntity<ErrorResponse>
	 */
	// @ExceptionHandler(EventDeletionFailedException.class)
	// protected ResponseEntity<ErrorResponse> handleEventDeletionFailedException(EventDeletionFailedException ex) {
	// 	log.error("EventDeletionFailedException", ex);
	// 	final ErrorResponse response = ErrorResponse.of(GlobalErrorCode.EVENT_DELETION_FAILED, ex.getMessage());
	// 	return new ResponseEntity<>(response, HttpStatus.OK);
	// }

	@ExceptionHandler(placeException.class)
	protected final ResponseEntity<ErrorResponse> handlePlaceNotFoundException(placeException ex){
		log.error("placeException", ex);
		ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), ex.getMessage());
		return new ResponseEntity<>(errorResponse, HttpStatus.OK);
	}



}