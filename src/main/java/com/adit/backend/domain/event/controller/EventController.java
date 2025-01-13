package com.adit.backend.domain.event.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adit.backend.domain.event.dto.request.EventUpdateRequestDto;
import com.adit.backend.domain.event.dto.response.EventResponseDto;
import com.adit.backend.domain.event.service.command.EventCommandService;
import com.adit.backend.domain.event.service.query.EventQueryService;
import com.adit.backend.global.common.ApiResponse;
import com.sun.jdi.request.EventRequest;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventCommandService commandService;
    private final EventQueryService queryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponseDto>>> getAllEvents() {
        List<EventResponseDto> events = queryService.getAllEvents();
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponseDto>> getEventById(@PathVariable Long id) {
        EventResponseDto event = queryService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @GetMapping("/date")
    public ResponseEntity<ApiResponse<List<EventResponseDto>>> getEventsByDate(@RequestParam LocalDate date) {
        List<EventResponseDto> events = queryService.getEventsByDate(date);
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<EventResponseDto>>> getTodayEvents() {
        List<EventResponseDto> events = queryService.getTodayEvents();
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/no-date")
    public ResponseEntity<ApiResponse<List<EventResponseDto>>> getNoDateEvents() {
        List<EventResponseDto> events = queryService.getNoDateEvents();
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<EventResponseDto>>> getPopularEvents() {
        List<EventResponseDto> events = queryService.getPopularEvents();
        return ResponseEntity.ok(ApiResponse.success(events));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponseDto>> createEvent(@RequestBody EventRequest request) {
        EventResponseDto event = commandService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(event));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponseDto>> updateEvent(@PathVariable Long id, @RequestBody EventUpdateRequestDto request) {
        EventResponseDto event = commandService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success(event));
    }
}