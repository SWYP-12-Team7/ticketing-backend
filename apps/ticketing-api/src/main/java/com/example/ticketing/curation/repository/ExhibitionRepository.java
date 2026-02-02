package com.example.ticketing.curation.repository;

import com.example.ticketing.curation.domain.CurationStatus;
import com.example.ticketing.curation.domain.Exhibition;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExhibitionRepository extends JpaRepository<Exhibition, Long> {

    @Query("""
        SELECT e FROM Exhibition e
        WHERE e.deletedAt IS NULL
        AND (:keyword IS NULL OR e.title LIKE %:keyword% OR e.subTitle LIKE %:keyword%)
        AND (:region IS NULL OR e.region = :region)
        AND (
            :#{#status} IS NULL
            OR (:#{#status?.name()} = 'ONGOING' AND e.startDate <= CURRENT_DATE AND e.endDate >= CURRENT_DATE)
            OR (:#{#status?.name()} = 'UPCOMING' AND e.startDate > CURRENT_DATE)
            OR (:#{#status?.name()} = 'ENDED' AND e.endDate < CURRENT_DATE)
        )
        ORDER BY e.createdAt DESC
        """)
    Page<Exhibition> findAllWithFilters(
        @Param("keyword") String keyword,
        @Param("region") String region,
        @Param("status") CurationStatus status,
        Pageable pageable
    );

    @Query("SELECT e FROM Exhibition e WHERE e.id = :id AND e.deletedAt IS NULL")
    Optional<Exhibition> findByIdAndNotDeleted(@Param("id") Long id);

    boolean existsByTitle(String title);
}
