package com.adit.backend.domain.place.dto.response;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.annotation.Nullable;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlaceResponseDto(@NotNull(message = "commonPlace ID must not be null") Long commonPlaceId,
							   @Nullable Long userPlaceId,
							   @Nullable String memo,
							   @Nullable Boolean visited, String placeName,
							   @Nullable BigDecimal latitude,
							   @Nullable BigDecimal longitude,
							   String subCategory,
							   String roadAddressName,
							   @Nullable String addressName,
							   @Nullable String url,
							   @Nullable List<String> imageUrlList,
							   @Nullable Long friendUserId,
							   @Nullable String profile) {

}
