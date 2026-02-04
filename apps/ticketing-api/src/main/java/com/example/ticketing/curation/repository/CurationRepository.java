package com.example.ticketing.curation.repository;

import com.example.ticketing.curation.domain.Curation;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
