package com.adit.backend.domain.scraper.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record scraperResponse() {
	public record scarperInfoDto(@JsonProperty("caption") String caption, @JsonProperty("displayUrl") String displayUrl
	){ }
}