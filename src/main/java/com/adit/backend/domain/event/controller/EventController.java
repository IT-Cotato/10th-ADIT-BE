package com.adit.backend.domain.event.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adit.backend.domain.event.dto.request.EventRequestDto;
import com.adit.backend.domain.event.dto.request.EventUpdateRequestDto;
import com.adit.backend.domain.event.dto.response.EventResponseDto;
import com.adit.backend.domain.event.service.command.EventCommandService;
import com.adit.backend.domain.event.service.query.EventQueryService;
import com.adit.backend.domain.user.entity.User;
import com.adit.backend.global.common.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Event API", description = "이벤트 데이터를 생성, 수정, 조회할 수 있는 API입니다.")
public class EventController {

    private final EventCommandService commandService;
    private final EventQueryService queryService;

    @Operation(summary = "모든 이벤트 조회", description = "모든 이벤트 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponseDto>>> getAllEvents() {
        List<EventResponseDto> events = queryService.getAllEvents();
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @Operation(summary = "ID로 이벤트 조회", description = "특정 ID에 해당하는 이벤트의 세부 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponseDto>> getEventById(@PathVariable Long id) {
        EventResponseDto event = queryService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @Operation(summary = "특정 날짜의 이벤트 조회", description = "특정 날짜에 해당하는 이벤트 목록을 조회합니다.")
    @GetMapping("/date")
    public ResponseEntity<ApiResponse<List<EventResponseDto>>> getEventsByDate(@RequestParam LocalDate date) {
        List<EventResponseDto> events = queryService.getEventsByDate(date);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @Operation(summary = "오늘의 이벤트 조회", description = "오늘 날짜에 해당하는 이벤트 목록을 조회합니다.")
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<EventResponseDto>>> getTodayEvents() {
        List<EventResponseDto> events = queryService.getTodayEvents();
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @Operation(summary = "날짜가 지정되지 않은 이벤트 조회", description = "특정 날짜가 지정되지 않은 이벤트 목록을 조회합니다.")
    @GetMapping("/no-date")
    public ResponseEntity<ApiResponse<List<EventResponseDto>>> getNoDateEvents() {
        List<EventResponseDto> events = queryService.getNoDateEvents();
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @Operation(summary = "인기 이벤트 조회", description = "방문 수를 기준으로 인기 있는 이벤트 목록을 조회합니다.")
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<EventResponseDto>>> getPopularEvents() {
        List<EventResponseDto> events = queryService.getPopularEvents();
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @Operation(summary = "새 이벤트 생성", description = "제공된 세부 정보를 기반으로 새 이벤트를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<EventResponseDto>> createEvent(@AuthenticationPrincipal(expression = "user") User user,
        @RequestBody EventRequestDto request) {
        EventResponseDto event = commandService.createEvent(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(event));
    }

    @Operation(summary = "이벤트 수정", description = "기존 이벤트의 세부 정보를 수정합니다.")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponseDto>> updateEvent(@PathVariable Long id, @RequestBody EventUpdateRequestDto request) {
        EventResponseDto event = commandService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success(event));
    }
}