package com.adit.backend.domain.event.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.adit.backend.domain.event.entity.EventStatistics;

@Repository
public interface EventStatisticsRepository extends JpaRepository<EventStatistics, Long> {
    Optional<EventStatistics> findByCommonEventId(Long commonEventId);

    // bookmarkCount가 0보다 큰 이벤트만 정렬해서 10개 가져오기
    List<EventStatistics> findTop10ByBookmarkCountGreaterThanOrderByBookmarkCountDesc(int minCount);
}

