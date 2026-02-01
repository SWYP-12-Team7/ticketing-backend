package com.example.ticketing.curation.repository;

import com.example.ticketing.curation.domain.ExhibitionLike;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
