package com.adit.backend.domain.event.dto.response;

import java.time.LocalDateTime;

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
public class EventResponseDto {
    private Long id;
    private String name;
    private String category;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String memo;
    private Boolean visited;
}