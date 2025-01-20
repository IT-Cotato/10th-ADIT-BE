package com.adit.backend.domain.scraper.exception;

import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.BusinessException;

public class scraperException extends BusinessException {
	public scraperException(GlobalErrorCode errorCode) {
		super(errorCode);
	}
}
