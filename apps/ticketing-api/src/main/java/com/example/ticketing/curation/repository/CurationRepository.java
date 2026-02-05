package com.example.ticketing.curation.repository;

import com.example.ticketing.curation.domain.Curation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurationRepository extends JpaRepository<Curation, Long> {

    /**
     * 카테고리 기반 추천 (JSON_CONTAINS 사용)
     * 사용자의 선호 카테고리와 일치하는 행사 중 랜덤 추천
     */
    @Query(value = """
        SELECT * FROM curation c
        WHERE c.deleted_at IS NULL
        AND JSON_OVERLAPS(c.category, :categories)
        ORDER BY RAND()
        """, nativeQuery = true)
    List<Curation> findByCategoriesRandomly(@Param("categories") String categoriesJson, Pageable pageable);
}