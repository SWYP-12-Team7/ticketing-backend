package com.example.ticketing.curation.repository;

import com.example.ticketing.curation.domain.CurationType;
import com.example.ticketing.curation.domain.CurationViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CurationViewHistoryRepository extends JpaRepository<CurationViewHistory, Long> {

    @Query("""
        SELECT h.curationId, COUNT(h) as viewCount
        FROM CurationViewHistory h
        WHERE h.curationType = :curationType
          AND h.viewedAt >= :since
        GROUP BY h.curationId
        ORDER BY viewCount DESC
        LIMIT :limit
        """)
    List<Object[]> findPopularCurationIds(
            @Param("curationType") CurationType curationType,
            @Param("since") LocalDateTime since,
            @Param("limit") int limit
    );
}