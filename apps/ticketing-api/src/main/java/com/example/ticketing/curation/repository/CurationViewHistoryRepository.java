package com.example.ticketing.curation.repository;

import com.example.ticketing.curation.domain.CurationViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurationViewHistoryRepository extends JpaRepository<CurationViewHistory, Long> {
    // 랭킹 집계용 쿼리는 추후 추가
}