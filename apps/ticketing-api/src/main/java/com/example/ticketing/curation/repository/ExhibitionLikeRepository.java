package com.example.ticketing.curation.repository;

import com.example.ticketing.curation.domain.ExhibitionLike;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @deprecated CurationLikeRepository로 통합되었습니다.
 * V23 마이그레이션에서 exhibition_likes 테이블이 curation_likes로 통합됨.
 * 새로운 코드는 {@link CurationLikeRepository}를 사용하세요.
 */
@Deprecated
@Repository
public interface ExhibitionLikeRepository extends JpaRepository<ExhibitionLike, Long> {

    Optional<ExhibitionLike> findByUserIdAndExhibitionId(Long userId, Long exhibitionId);

    boolean existsByUserIdAndExhibitionId(Long userId, Long exhibitionId);

    @Query("SELECT el.exhibitionId FROM ExhibitionLike el WHERE el.userId = :userId AND el.exhibitionId IN :exhibitionIds")
    List<Long> findExhibitionIdsByUserIdAndExhibitionIdIn(
        @Param("userId") Long userId,
        @Param("exhibitionIds") List<Long> exhibitionIds
    );

    void deleteByUserIdAndExhibitionId(Long userId, Long exhibitionId);
}
