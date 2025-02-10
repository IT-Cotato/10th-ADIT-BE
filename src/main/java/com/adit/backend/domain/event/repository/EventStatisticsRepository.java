package com.adit.backend.domain.event.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.adit.backend.domain.event.entity.EventStatistics;

@Repository
public interface EventStatisticsRepository extends JpaRepository<EventStatistics, Long> {
    Optional<EventStatistics> findByCommonEventId(Long commonEventId);

    // 인기 이벤트를 bookmarkCount 기준으로 내림차순 정렬하여 10개 가져오기
    List<EventStatistics> findTop10ByOrderByBookmarkCountDesc();
}
