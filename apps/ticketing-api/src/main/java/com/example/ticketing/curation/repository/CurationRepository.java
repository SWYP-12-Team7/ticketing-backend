package com.example.ticketing.curation.repository;

import com.example.ticketing.curation.domain.Curation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CurationRepository extends JpaRepository<Curation, Long> {

    /**
     * 유저 선호 지역/카테고리 기반 행사 조회
     * 정렬: 시작일 빠른순 -> 기간 짧은순 -> 제목 오름차순
     */
    @Query("SELECT c FROM Curation c WHERE " +
           "c.region IN :regions AND " +
           "EXISTS (SELECT 1 FROM c.category cat WHERE cat IN :categories) " +
           "ORDER BY c.startDate ASC, (c.endDate - c.startDate) ASC, c.title ASC")
    List<Curation> findByRegionsAndCategories(
            @Param("regions") List<String> regions,
            @Param("categories") List<String> categories
    );

    /**
     * 오픈예정 행사 (D-7 이내)
     * 정렬: 제목 오름차순
     */
    @Query("SELECT c FROM Curation c WHERE " +
           "c.startDate > :today AND c.startDate <= :sevenDaysLater " +
           "ORDER BY c.title ASC")
    List<Curation> findUpcomingWithin7Days(
            @Param("today") LocalDate today,
            @Param("sevenDaysLater") LocalDate sevenDaysLater
    );

    /**
     * 오늘 오픈한 행사
     * 정렬: 제목 오름차순
     */
    @Query("SELECT c FROM Curation c WHERE c.startDate = :today ORDER BY c.title ASC")
    List<Curation> findByStartDate(@Param("today") LocalDate today);

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

    @Query("SELECT c FROM Curation c WHERE c.id IN :ids AND c.region = :region")
    List<Curation> findByIdInAndRegion(@Param("ids") List<Long> ids, @Param("region") String region);

    /**
     * 지도뷰용 행사 조회 (해당 날짜에 진행 중인 행사)
     * 좌표가 있는 행사만 조회
     */
    @Query("SELECT c FROM Curation c WHERE " +
           "c.latitude IS NOT NULL AND c.longitude IS NOT NULL " +
           "AND c.startDate <= :date " +
           "AND (c.endDate IS NULL OR c.endDate >= :date) " +
           "ORDER BY c.likeCount DESC")
    List<Curation> findOngoingWithCoordinates(@Param("date") LocalDate date);

    /**
     * 캘린더뷰용 행사 조회 (해당 월과 겹치는 행사)
     */
    @Query("SELECT c FROM Curation c WHERE " +
           "c.startDate <= :endDate " +
           "AND (c.endDate IS NULL OR c.endDate >= :startDate)")
    List<Curation> findOverlappingWithPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 해당 날짜에 진행 중인 행사 (좌표 무관)
     */
    @Query("SELECT c FROM Curation c WHERE " +
           "c.startDate <= :date " +
           "AND (c.endDate IS NULL OR c.endDate >= :date) " +
           "ORDER BY c.likeCount DESC")
    List<Curation> findOngoingByDate(@Param("date") LocalDate date);

    /**
     * 통합 검색 - ID 목록 조회 (키워드, 타입, 카테고리)
     * 정렬: 시작일 기준 오늘과 가까운 순, 제목 가나다순
     */
    @Query(value = """
        SELECT c.id FROM curation c
        WHERE c.deleted_at IS NULL
        AND (:keyword IS NULL OR c.title LIKE CONCAT('%', :keyword, '%'))
        AND (:type IS NULL OR c.type = :type)
        AND (:category IS NULL OR JSON_CONTAINS(c.category, JSON_QUOTE(:category)))
        ORDER BY ABS(DATEDIFF(c.start_date, CURDATE())) ASC, c.title ASC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<Long> searchCurationIds(
            @Param("keyword") String keyword,
            @Param("type") String type,
            @Param("category") String category,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    /**
     * 통합 검색 카운트
     */
    @Query(value = """
        SELECT COUNT(*) FROM curation c
        WHERE c.deleted_at IS NULL
        AND (:keyword IS NULL OR c.title LIKE CONCAT('%', :keyword, '%'))
        AND (:type IS NULL OR c.type = :type)
        AND (:category IS NULL OR JSON_CONTAINS(c.category, JSON_QUOTE(:category)))
        """, nativeQuery = true)
    long countSearchCurations(
            @Param("keyword") String keyword,
            @Param("type") String type,
            @Param("category") String category
    );

    /**
     * 주변 행사 조회 (진행중, 좌표 있는 행사)
     * district 필터링은 Java에서 처리
     */
    @Query("SELECT c FROM Curation c WHERE " +
           "c.id != :excludeId " +
           "AND c.latitude IS NOT NULL AND c.longitude IS NOT NULL " +
           "AND c.startDate <= :today " +
           "AND (c.endDate IS NULL OR c.endDate >= :today) " +
           "ORDER BY c.likeCount DESC")
    List<Curation> findNearbyOngoing(
            @Param("excludeId") Long excludeId,
            @Param("today") LocalDate today);
}