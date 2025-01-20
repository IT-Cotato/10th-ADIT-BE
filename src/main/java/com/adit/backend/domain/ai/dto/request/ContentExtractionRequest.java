package com.adit.backend.domain.ai.dto.request;

import org.hibernate.validator.constraints.URL;

public record ContentExtractionRequest (@URL String url) {
}
