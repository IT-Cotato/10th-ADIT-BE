package com.adit.backend.domain.event.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequestDto {
    @NotBlank
    private String name;

    @NotBlank
    private String category;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String memo;
    private Boolean visited;
}