package com.example.ticketing.curation.repository;

import com.example.ticketing.curation.domain.CurationLike;
import com.example.ticketing.curation.domain.CurationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CurationLike Repository (전시/팝업 통합)
 */
@Repository
public interface CurationLikeRepository extends JpaRepository<CurationLike, Long> {

    /**
     * 특정 사용자의 특정 큐레이션 찜 조회
     */
    Optional<CurationLike> findByUserIdAndCurationIdAndCurationType(
        Long userId,
        Long curationId,
        CurationType curationType
    );

    /**
     * 특정 사용자가 특정 큐레이션을 찜했는지 확인
     */
    boolean existsByUserIdAndCurationIdAndCurationType(
        Long userId,
        Long curationId,
        CurationType curationType
    );

    /**
     * 사용자가 찜한 큐레이션 ID 목록 조회 (배치 조회 최적화)
     * - 전시/팝업 목록에서 사용자가 찜한 항목 표시용
     */
    @Query("SELECT cl.curationId FROM CurationLike cl " +
           "WHERE cl.userId = :userId " +
           "AND cl.curationId IN :curationIds " +
           "AND cl.curationType = :curationType")
    List<Long> findCurationIdsByUserIdAndCurationIdInAndCurationType(
        @Param("userId") Long userId,
        @Param("curationIds") List<Long> curationIds,
        @Param("curationType") CurationType curationType
    );

    /**
     * 찜 삭제
     */
    void deleteByUserIdAndCurationIdAndCurationType(
        Long userId,
        Long curationId,
        CurationType curationType
    );

    /**
     * 사용자가 찜한 모든 큐레이션 조회 (마이페이지용)
     */
    List<CurationLike> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 특정 타입의 사용자 찜 목록 조회
     */
    List<CurationLike> findByUserIdAndCurationTypeOrderByCreatedAtDesc(
        Long userId,
        CurationType curationType
    );
}