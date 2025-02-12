package com.adit.backend.domain.notification.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@Repository
public class SseEmitterRepository {

	private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
	private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

	/**
	 * emitterID와 sseEmitter를 사용하여 SSE이벤트 전송 객체를 저장
	 */
	public SseEmitter save(String userEmail, SseEmitter sseEmitter) {
		emitters.put(userEmail, sseEmitter);
		return sseEmitter;
	}

	/**
	 * 이벤트캐시 아이디와 이벤트 객체를 받아 저장.
	 */
	public void saveEventCache(String eventCacheId, Object event) {
		eventCache.put(eventCacheId, event);
	}

	/**
	 * 주어진 userEmail로 시작하는 모든 Emitter를 가져옴
	 */
	public Map<String, SseEmitter> findAllEmitterStartWithByMemberId(String userEmail) {
		return emitters.entrySet().stream()
			.filter(entry -> entry.getKey().startsWith(userEmail))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 *
	 */
	public Map<String, Object> findAllEventCacheStartWithByMemberId(String userEmail) {
		return eventCache.entrySet().stream()
			.filter(entry -> entry.getKey().startsWith(userEmail))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * 삭제
	 */
	public Optional<SseEmitter> findById(String userEmail) {
		return Optional.ofNullable(emitters.get(userEmail));
	}

	/**
	 *  해당 회원과 관련된 모든 Emitter를 지움
	 */
	public void deleteByUserEmail(String userEmail) {
		emitters.remove(userEmail);
	}

	/**
	 * 해당 회원과 관련된 모든 Emitter를 지움
	 */
	public void deleteAllEmitterStartWithId(String userEmail) {
		emitters.forEach(
			(key, emitter) -> {
				if (key.startsWith(userEmail)) {
					emitters.remove(key);
				}
			}
		);
	}

	/**
	 * 해당 회원과 관련된 모든 이벤트를 지움
	 */
	public void deleteAllEventCacheStartWithId(String userEmail) {
		eventCache.forEach(
			(key, emitter) -> {
				if (key.startsWith(userEmail)) {
					eventCache.remove(key);
				}
			}
		);
	}
}
