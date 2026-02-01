package com.example.ticketing.curation.repository;

import com.example.ticketing.curation.domain.Curation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurationRepository extends JpaRepository<Curation, Long> {
}