package com.example.ticketing.user.domain;

import com.example.ticketing.curation.domain.CurationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    Optional<UserFavorite> findByUserIdAndCurationId(Long userId, Long curationId);

    List<UserFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    boolean existsByUserIdAndCurationId(Long userId, Long curationId);

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

    void deleteByUserIdAndCurationId(Long userId, Long curationId);
}