package com.example.ticketing.user.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 소셜 계정 Repository
 * - 소셜 제공자와 제공자 ID로 계정 조회
 */
@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
  Optional<SocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId);

  void deleteByUserId(Long userId);
}
