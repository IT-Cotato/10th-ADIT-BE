package com.adit.backend.infra.s3.exception;

import com.adit.backend.global.error.GlobalErrorCode;
import com.adit.backend.global.error.exception.BusinessException;

public class S3Exception extends BusinessException {

	public S3Exception(GlobalErrorCode errorCode) {
		super(errorCode);
	}
}
