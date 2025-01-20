package com.adit.backend.infra.crawler.exception;

import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.BusinessException;

public class CrawlingException extends BusinessException {
	public CrawlingException(GlobalErrorCode errorCode) {
		super(errorCode);
	}
}
